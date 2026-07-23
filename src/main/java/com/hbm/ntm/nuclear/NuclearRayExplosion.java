package com.hbm.ntm.nuclear;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public final class NuclearRayExplosion {
    private static final int FLUID_SETTLE_PASSES = 10;
    private static final int RAYS_PER_TASK = 2_048;
    private final ServerLevel level;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final int strength;
    private final int length;
    private final int minBuildHeight;
    private final int maxBuildHeight;
    private final int rayCount;
    private final int maxSteps;
    private final List<Long> snapshotSections = new ArrayList<>();
    private final Long2ObjectOpenHashMap<PalettedContainer<BlockState>> snapshots =
            new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<ChunkPlan> perChunk = new Long2ObjectOpenHashMap<>();
    private final List<Long> orderedChunks = new ArrayList<>();
    private final CompletionService<RayBatchResult> completion =
            new ExecutorCompletionService<>(ExplosionWorkerPool.executor());
    private final Set<Future<RayBatchResult>> futures = new HashSet<>();
    private final int maxInFlight = Math.max(2, ExplosionWorkerPool.workers() * 2);
    private final BlockPos.MutableBlockPos tracePos = new BlockPos.MutableBlockPos();
    private int snapshotIndex;
    private int nextRay = 1;
    private int generatedRays;
    private int inFlight;
    private int nextChunk;
    private long activeChunkKey;
    private PrimitiveIterator.OfInt activeRemovalIterator;
    private BitSet activeTipBlocks;
    private final List<Long> fluidChunks = new ArrayList<>();
    private int fluidChunkIndex;
    private int fluidSectionIndex;
    private int fluidBlockIndex;
    private int fluidCleanupPasses;
    private double latitude = Math.PI;
    private double longitude;
    private boolean cacheComplete;
    private volatile boolean cancelled;

    public NuclearRayExplosion(ServerLevel level, int centerX, int centerY, int centerZ,
                               int strength, int speed, int length) {
        this.level = level;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.strength = strength;
        this.length = length;
        this.minBuildHeight = level.getMinBuildHeight();
        this.maxBuildHeight = level.getMaxBuildHeight();
        this.maxSteps = Math.min(Mth.ceil(strength), length);
        this.rayCount = (int) (2.5D * Math.PI * strength * strength);
        prepareSnapshotSections();
    }

    /** Predictable serving size for tests. */
    public void cacheTick() {
        while (!cacheComplete) {
            snapshotUntil(Long.MAX_VALUE);
            harvestCompleted();
            submitAvailable(Long.MAX_VALUE);
            if (inFlight > 0) harvestOneBlocking();
            finishCacheIfReady();
        }
    }

    public void cacheTick(int milliseconds) {
        if (cacheComplete || milliseconds <= 0) return;
        long deadline = System.nanoTime() + milliseconds * 1_000_000L;
        snapshotUntil(deadline);
        if (snapshotIndex < snapshotSections.size()) return;

        do {
            int harvested = harvestCompleted();
            int submitted = submitAvailable(deadline);
            finishCacheIfReady();
            if (cacheComplete || inFlight >= maxInFlight || harvested == 0 && submitted == 0) return;
        } while (System.nanoTime() < deadline);
    }

    private void prepareSnapshotSections() {
        if (maxSteps <= 0) return;
        int minSectionX = (centerX - maxSteps) >> 4;
        int maxSectionX = (centerX + maxSteps) >> 4;
        int minSectionY = Math.max(level.getMinSection(), (centerY - maxSteps) >> 4);
        int maxSectionY = Math.min(level.getMaxSection() - 1, (centerY + maxSteps) >> 4);
        int minSectionZ = (centerZ - maxSteps) >> 4;
        int maxSectionZ = (centerZ + maxSteps) >> 4;
        long radiusSquared = (long) maxSteps * maxSteps;

        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            long dx = distanceToSection(centerX, sectionX);
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                long dz = distanceToSection(centerZ, sectionZ);
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    long dy = distanceToSection(centerY, sectionY);
                    if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                        snapshotSections.add(SectionPos.asLong(sectionX, sectionY, sectionZ));
                    }
                }
            }
        }
        snapshotSections.sort(Comparator.comparingLong(this::sectionDistanceSquared));
    }

    private static long distanceToSection(int center, int section) {
        int min = section << 4;
        int max = min + 15;
        if (center < min) return (long) min - center;
        if (center > max) return (long) center - max;
        return 0L;
    }

    private long sectionDistanceSquared(long key) {
        long dx = distanceToSection(centerX, SectionPos.x(key));
        long dy = distanceToSection(centerY, SectionPos.y(key));
        long dz = distanceToSection(centerZ, SectionPos.z(key));
        return dx * dx + dy * dy + dz * dz;
    }

    private void snapshotUntil(long deadline) {
        while (snapshotIndex < snapshotSections.size() && System.nanoTime() < deadline) {
            long key = snapshotSections.get(snapshotIndex++);
            int sectionX = SectionPos.x(key);
            int sectionY = SectionPos.y(key);
            int sectionZ = SectionPos.z(key);
            LevelChunk chunk = level.getChunk(sectionX, sectionZ);
            int index = level.getSectionIndexFromSectionY(sectionY);
            if (index < 0 || index >= chunk.getSections().length) continue;
            LevelChunkSection section = chunk.getSections()[index];
            if (!section.hasOnlyAir()) snapshots.put(key, section.getStates().copy());
        }
    }

    private int submitAvailable(long deadline) {
        int submitted = 0;
        while (nextRay <= rayCount && inFlight < maxInFlight && System.nanoTime() < deadline) {
            int count = Math.min(RAYS_PER_TASK, rayCount - nextRay + 1);
            RayDirection[] directions = new RayDirection[count];
            for (int i = 0; i < count; i++) {
                directions[i] = new RayDirection(
                        Math.sin(latitude) * Math.cos(longitude),
                        Math.cos(latitude),
                        Math.sin(latitude) * Math.sin(longitude));
                advanceSpiral();
            }
            Future<RayBatchResult> future = completion.submit(() -> calculateBatch(directions));
            futures.add(future);
            inFlight++;
            submitted++;
        }
        return submitted;
    }

    private RayBatchResult calculateBatch(RayDirection[] directions) {
        Long2ObjectOpenHashMap<ChunkPlan> result = new Long2ObjectOpenHashMap<>();
        int processed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        LongOpenHashSet rayChunks = new LongOpenHashSet();

        for (RayDirection direction : directions) {
            if (cancelled || Thread.currentThread().isInterrupted()) break;
            float resistance = strength;
            Tip last = null;
            rayChunks.clear();

            for (int step = 0; step < maxSteps; step++) {
                float rayX = (float) (centerX + direction.x * step);
                float rayY = (float) (centerY + direction.y * step);
                float rayZ = (float) (centerZ + direction.z * step);
                int x = Mth.floor(rayX);
                int y = Mth.floor(rayY);
                int z = Mth.floor(rayZ);
                if (y < minBuildHeight && direction.y < 0.0D
                        || y >= maxBuildHeight && direction.y > 0.0D) break;
                pos.set(x, y, z);
                BlockState state = snapshotState(x, y, z);
                boolean air = state.isAir();
                if (!air && state.getFluidState().isEmpty()) {
                    double factor = 100.0D - (double) step / strength * 100.0D;
                    factor *= 0.07D;
                    resistance -= (float) Math.pow(masqueradeResistance(state), 7.5D - factor);
                }
                if (resistance > 0.0F && !air) {
                    last = new Tip(rayX, rayY, rayZ);
                    rayChunks.add(ChunkPos.asLong(x >> 4, z >> 4));
                }
                if (resistance <= 0.0F || step + 1 >= length || step == Mth.ceil(strength) - 1) break;
            }

            if (last != null) collectTipBlocks(last, rayChunks, result, pos);
            processed++;
        }
        return new RayBatchResult(result, processed);
    }

    private void collectTipBlocks(Tip tip, LongOpenHashSet rayChunks,
                                  Long2ObjectOpenHashMap<ChunkPlan> result,
                                  BlockPos.MutableBlockPos pos) {
        double vectorX = tip.x - centerX;
        double vectorY = tip.y - centerY;
        double vectorZ = tip.z - centerZ;
        double vectorLength = Math.sqrt(vectorX * vectorX + vectorY * vectorY + vectorZ * vectorZ);
        if (vectorLength == 0.0D) return;
        double directionX = vectorX / vectorLength;
        double directionY = vectorY / vectorLength;
        double directionZ = vectorZ / vectorLength;
        long tipBlock = BlockPos.asLong(Mth.floor(tip.x), Mth.floor(tip.y), Mth.floor(tip.z));

        for (int step = 0; step < vectorLength; step++) {
            int x = Mth.floor(centerX + directionX * step);
            int y = Mth.floor(centerY + directionY * step);
            int z = Mth.floor(centerZ + directionZ * step);
            long chunkKey = ChunkPos.asLong(x >> 4, z >> 4);
            if (!rayChunks.contains(chunkKey) || snapshotState(x, y, z).isAir()) continue;
            pos.set(x, y, z);
            ChunkPlan plan = result.computeIfAbsent(chunkKey, ignored -> new ChunkPlan());
            int packed = packChunkBlock(x, y, z);
            plan.blocks.set(packed);
            if (pos.asLong() == tipBlock) plan.tips.set(packed);
        }
    }

    private int packChunkBlock(int x, int y, int z) {
        return ((y - minBuildHeight) << 8) | ((x & 15) << 4) | (z & 15);
    }

    private BlockState snapshotState(int x, int y, int z) {
        PalettedContainer<BlockState> section =
                snapshots.get(SectionPos.asLong(x >> 4, y >> 4, z >> 4));
        if (section == null) return Blocks.AIR.defaultBlockState();
        return section.get(x & 15, y & 15, z & 15);
    }

    private void advanceSpiral() {
        if (nextRay < rayCount) {
            int k = nextRay + 1;
            double h = -1.0D + 2.0D * (k - 1.0D) / (rayCount - 1.0D);
            latitude = Math.acos(h);
            longitude = (longitude + 3.6D / Math.sqrt(rayCount) / Math.sqrt(1.0D - h * h))
                    % (Math.PI * 2.0D);
        } else {
            latitude = 0.0D;
            longitude = 0.0D;
        }
        nextRay++;
    }

    private int harvestCompleted() {
        int harvested = 0;
        Future<RayBatchResult> future;
        while ((future = completion.poll()) != null) {
            merge(future);
            harvested++;
        }
        return harvested;
    }

    private void harvestOneBlocking() {
        try {
            merge(completion.take());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            cancel();
        }
    }

    private void merge(Future<RayBatchResult> future) {
        try {
            futures.remove(future);
            RayBatchResult batch = future.get();
            generatedRays += batch.processed;
            batch.chunks.forEach((key, plan) -> {
                ChunkPlan destination = perChunk.computeIfAbsent(key.longValue(), ignored -> new ChunkPlan());
                destination.blocks.or(plan.blocks);
                destination.tips.or(plan.tips);
            });
        } catch (java.util.concurrent.CancellationException ignored) {
        } catch (java.util.concurrent.ExecutionException exception) {
            cancel();
            throw new IllegalStateException("MK5 explosion worker failed", exception.getCause());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            cancel();
        } finally {
            inFlight--;
        }
    }

    private void finishCacheIfReady() {
        if (cacheComplete || nextRay <= rayCount || inFlight > 0) return;
        orderedChunks.addAll(perChunk.keySet());
        int chunkX = centerX >> 4;
        int chunkZ = centerZ >> 4;
        orderedChunks.sort(Comparator.comparingInt(key ->
                Math.abs(chunkX - ChunkPos.getX(key)) + Math.abs(chunkZ - ChunkPos.getZ(key))));
        prepareFluidCleanupChunks();
        snapshots.clear();
        snapshotSections.clear();
        futures.clear();
        cacheComplete = true;
    }

    private static float masqueradeResistance(BlockState state) {
        if (state.is(Blocks.SANDSTONE)) return Blocks.STONE.getExplosionResistance();
        if (state.is(Blocks.OBSIDIAN)) return Blocks.STONE.getExplosionResistance() * 3.0F;
        return state.getBlock().getExplosionResistance();
    }

    public void destructionTick(int milliseconds) {
        if (!cacheComplete || milliseconds <= 0) return;
        long deadline = System.nanoTime() + milliseconds * 1_000_000L;
        while (nextChunk < orderedChunks.size() && System.nanoTime() < deadline) {
            if (activeRemovalIterator == null) beginChunk();
            if (activeRemovalIterator.hasNext()) {
                int packed = activeRemovalIterator.nextInt();
                int x = (ChunkPos.getX(activeChunkKey) << 4) + (packed >> 4 & 15);
                int y = minBuildHeight + (packed >> 8);
                int z = (ChunkPos.getZ(activeChunkKey) << 4) + (packed & 15);
                tracePos.set(x, y, z);
                level.setBlock(tracePos, Blocks.AIR.defaultBlockState(),
                        activeTipBlocks.get(packed) ? 3 : 2);
            } else {
                finishChunk();
            }
        }
        if (nextChunk >= orderedChunks.size() && System.nanoTime() < deadline) {
            cleanupFluids(deadline);
        }
    }

    private void beginChunk() {
        activeChunkKey = orderedChunks.get(nextChunk);
        ChunkPlan plan = perChunk.get(activeChunkKey);
        activeRemovalIterator = plan.blocks.stream().iterator();
        activeTipBlocks = plan.tips;
    }

    /** Rays miss sheets of water, so queue every wet chunk in the sphere. */
    private void prepareFluidCleanupChunks() {
        fluidChunks.clear();
        int radius = Math.max(0, length - 1);
        int minChunkX = (centerX - radius) >> 4;
        int maxChunkX = (centerX + radius) >> 4;
        int minChunkZ = (centerZ - radius) >> 4;
        int maxChunkZ = (centerZ + radius) >> 4;
        long radiusSquared = (long) radius * radius;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            int minX = chunkX << 4;
            int maxX = minX + 15;
            long dx = centerX < minX ? (long) minX - centerX : centerX > maxX ? (long) centerX - maxX : 0L;
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int minZ = chunkZ << 4;
                int maxZ = minZ + 15;
                long dz = centerZ < minZ ? (long) minZ - centerZ : centerZ > maxZ ? (long) centerZ - maxZ : 0L;
                if (dx * dx + dz * dz <= radiusSquared) fluidChunks.add(ChunkPos.asLong(chunkX, chunkZ));
            }
        }

        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        fluidChunks.sort(Comparator.comparingInt(key ->
                Math.abs(centerChunkX - ChunkPos.getX(key)) + Math.abs(centerChunkZ - ChunkPos.getZ(key))));
    }

    /** Ten passes, one per tick, until Minecraft stops putting the water back. */
    private void cleanupFluids(long deadline) {
        if (fluidCleanupPasses >= FLUID_SETTLE_PASSES) return;
        int radius = Math.max(0, length - 1);
        long radiusSquared = (long) radius * radius;

        while (fluidChunkIndex < fluidChunks.size()) {
            long chunkKey = fluidChunks.get(fluidChunkIndex);
            LevelChunk chunk = level.getChunk(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey));
            LevelChunkSection[] sections = chunk.getSections();

            while (fluidSectionIndex < sections.length) {
                LevelChunkSection section = sections[fluidSectionIndex];
                int sectionY = chunk.getSectionYFromSectionIndex(fluidSectionIndex) << 4;
                int minY = sectionY;
                int maxY = sectionY + 15;
                long dy = centerY < minY ? (long) minY - centerY : centerY > maxY ? (long) centerY - maxY : 0L;

                if (fluidBlockIndex == 0 && (dy * dy > radiusSquared
                        || !section.getStates().maybeHas(state -> !state.getFluidState().isEmpty()))) {
                    fluidSectionIndex++;
                    continue;
                }

                while (fluidBlockIndex < LevelChunkSection.SECTION_SIZE) {
                    int packed = fluidBlockIndex++;
                    int localX = packed & 15;
                    int localZ = packed >> 4 & 15;
                    int localY = packed >> 8 & 15;
                    int x = (ChunkPos.getX(chunkKey) << 4) + localX;
                    int y = sectionY + localY;
                    int z = (ChunkPos.getZ(chunkKey) << 4) + localZ;
                    long offsetX = (long) x - centerX;
                    long offsetY = (long) y - centerY;
                    long offsetZ = (long) z - centerZ;

                    if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= radiusSquared
                            && !section.getFluidState(localX, localY, localZ).isEmpty()) {
                        tracePos.set(x, y, z);
                        level.setBlock(tracePos, Blocks.AIR.defaultBlockState(),
                                Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
                    }
                    if ((fluidBlockIndex & 255) == 0 && System.nanoTime() >= deadline) return;
                }

                fluidBlockIndex = 0;
                fluidSectionIndex++;
            }

            fluidSectionIndex = 0;
            fluidChunkIndex++;
            if (System.nanoTime() >= deadline) return;
        }

        fluidChunkIndex = 0;
        fluidSectionIndex = 0;
        fluidBlockIndex = 0;
        fluidCleanupPasses++;
    }

    private void finishChunk() {
        perChunk.remove(activeChunkKey);
        nextChunk++;
        activeRemovalIterator = null;
        activeTipBlocks = null;
    }

    public boolean isComplete() {
        return cacheComplete && perChunk.isEmpty()
                && fluidCleanupPasses >= FLUID_SETTLE_PASSES;
    }

    public int cachedChunkCount() {
        return perChunk.size();
    }

    public int generatedRays() {
        return Math.min(generatedRays, rayCount);
    }

    public int totalRays() {
        return rayCount;
    }

    public int snapshotSectionCount() {
        return snapshotSections.size();
    }

    public void cancel() {
        cancelled = true;
        for (Future<RayBatchResult> future : futures) future.cancel(true);
        futures.clear();
        snapshots.clear();
        snapshotSections.clear();
        perChunk.clear();
        orderedChunks.clear();
        activeRemovalIterator = null;
        activeTipBlocks = null;
        fluidChunks.clear();
        fluidChunkIndex = 0;
        fluidSectionIndex = 0;
        fluidBlockIndex = 0;
        fluidCleanupPasses = FLUID_SETTLE_PASSES;
        cacheComplete = true;
    }

    private static final class ChunkPlan {
        private final BitSet blocks = new BitSet();
        private final BitSet tips = new BitSet();
    }

    private record RayDirection(double x, double y, double z) {
    }

    private record Tip(float x, float y, float z) {
    }

    private record RayBatchResult(Long2ObjectOpenHashMap<ChunkPlan> chunks, int processed) {
    }
}
