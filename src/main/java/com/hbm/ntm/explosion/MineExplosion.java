package com.hbm.ntm.explosion;

import com.hbm.ntm.entity.PowerFistRubbleEntity;
import com.hbm.ntm.entity.ShrapnelEntity;
import com.hbm.ntm.radiation.ChunkRadiationData;
import com.hbm.ntm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/** ExplosionVNT, reduced to the parts that make landmines everyone else's problem. */
public final class MineExplosion {
    private MineExplosion() {
    }

    /** Seven visibility samples and enough arithmetic to ruin both legs evenly. */
    public static void blastEntities(ServerLevel level, double cx, double cy, double cz,
                                     float size, float fixedDamage, double nodeDist,
                                     float pierceDT, float pierceDR, float rangeMod) {
        blastEntities(level, cx, cy, cz, size, fixedDamage, nodeDist,
                pierceDT, pierceDR, rangeMod, null, null);
    }

    /** Same blast, now with someone to blame. */
    public static void blastEntities(ServerLevel level, double cx, double cy, double cz,
                                     float size, float fixedDamage, double nodeDist,
                                     float pierceDT, float pierceDR, float rangeMod,
                                     Entity directEntity, Entity causingEntity) {
        blastEntities(level, cx, cy, cz, size, fixedDamage, nodeDist, pierceDT, pierceDR,
                rangeMod, directEntity, causingEntity, false);
    }

    /** Same blast, half damage for its proud parent. */
    public static void blastEntities(ServerLevel level, double cx, double cy, double cz,
                                     float size, float fixedDamage, double nodeDist,
                                     float pierceDT, float pierceDR, float rangeMod,
                                     Entity directEntity, Entity causingEntity,
                                     boolean halfDamageToDirectEntity) {
        double range = size * 2.0D * rangeMod;
        DamageSource source = level.damageSources().explosion(directEntity, causingEntity);
        Vec3 center = new Vec3(cx, cy, cz);
        AABB area = new AABB(center, center).inflate(range + 1.0D);

        for (Entity target : level.getEntities((Entity) null, area, Entity::isAlive)) {
            AABB bounds = target.getBoundingBox();
            double dx = (bounds.minX <= cx && bounds.maxX >= cx) ? 0.0D
                    : Math.min(Math.abs(bounds.minX - cx), Math.abs(bounds.maxX - cx));
            double dy = (bounds.minY <= cy && bounds.maxY >= cy) ? 0.0D
                    : Math.min(Math.abs(bounds.minY - cy), Math.abs(bounds.maxY - cy));
            double dz = (bounds.minZ <= cz && bounds.maxZ >= cz) ? 0.0D
                    : Math.min(Math.abs(bounds.minZ - cz), Math.abs(bounds.maxZ - cz));
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double scaled = dist / range;
            if (scaled > 1.0D) {
                continue;
            }

            double deltaX = target.getX() - cx;
            double deltaY = target.getY() + target.getEyeHeight() - cy;
            double deltaZ = target.getZ() - cz;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (distance == 0.0D) {
                continue;
            }
            deltaX /= distance;
            deltaY /= distance;
            deltaZ /= distance;

            float density = exposure(center, target, nodeDist);
            double knockback = (1.0D - scaled) * density;
            target.setDeltaMovement(target.getDeltaMovement().add(
                    deltaX * knockback, deltaY * knockback, deltaZ * knockback));

            if (density < 0.125F) {
                continue;
            }
            float amount = (float) (fixedDamage * (1.0D - scaled));
            if (halfDamageToDirectEntity && target == directEntity) {
                amount *= 0.5F;
            }
            if (amount <= 0.0F) {
                continue;
            }
            if (target instanceof LivingEntity living) {
                amount = compensateForArmorPiercing(living, source, amount, pierceDT, pierceDR);
                living.invulnerableTime = 0;
            }
            target.hurt(source, amount);
        }
    }

    private static float exposure(Vec3 center, Entity target, double nodeDist) {
        Vec3[] nodes = {
                center,
                center.add(nodeDist, 0.0D, 0.0D), center.add(-nodeDist, 0.0D, 0.0D),
                center.add(0.0D, nodeDist, 0.0D), center.add(0.0D, -nodeDist, 0.0D),
                center.add(0.0D, 0.0D, nodeDist), center.add(0.0D, 0.0D, -nodeDist)
        };
        float density = 0.0F;
        for (Vec3 node : nodes) {
            density = Math.max(density, Explosion.getSeenPercent(node, target));
        }
        return density;
    }

    /** Undo vanilla armor so the requested piercing survives vanilla armor. */
    public static float compensateForArmorPiercing(LivingEntity living, DamageSource source,
                                                   float intendedDamage, float thresholdNegation,
                                                   float armorPiercing) {
        if ((armorPiercing == 0.0F && thresholdNegation == 0.0F) || living.getArmorValue() <= 0) {
            return intendedDamage;
        }
        float armor = living.getArmorValue();
        float toughness = (float) living.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float effectiveArmor = Math.max(0.0F, armor - thresholdNegation)
                * Mth.clamp(1.0F - armorPiercing, 0.0F, 2.0F);
        float targetAfterArmor = CombatRules.getDamageAfterAbsorb(
                living, intendedDamage, source, effectiveArmor, toughness);
        float low = 0.0F;
        float high = Math.max(intendedDamage * 4.0F, intendedDamage + armor + 1.0F);
        while (CombatRules.getDamageAfterAbsorb(living, high, source, armor, toughness) < targetAfterArmor
                && high < 4096.0F) {
            high *= 2.0F;
        }
        for (int i = 0; i < 24; i++) {
            float mid = (low + high) * 0.5F;
            float result = CombatRules.getDamageAfterAbsorb(living, mid, source, armor, toughness);
            if (result < targetAfterArmor) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) * 0.5F;
    }

    /** Shell rays break blocks. Naval mode politely swims around liquids. */
    public static void blastBlocks(ServerLevel level, double cx, double cy, double cz,
                                   float size, int resolution, boolean skipLiquids) {
        blastBlocks(level, cx, cy, cz, size, resolution, skipLiquids, null);
    }

    /** Same block vandalism, now carrying identification. */
    public static void blastBlocks(ServerLevel level, double cx, double cy, double cz,
                                   float size, int resolution, boolean skipLiquids, Entity owner) {
        blastBlocks(level, cx, cy, cz, size, resolution, skipLiquids, owner, null);
    }

    public static void blastBlocksAndIgnite(ServerLevel level, double cx, double cy, double cz,
                                            float size, int resolution, Entity owner,
                                            BlockState fire) {
        blastBlocks(level, cx, cy, cz, size, resolution, false, owner, fire);
    }

    private static void blastBlocks(ServerLevel level, double cx, double cy, double cz,
                                    float size, int resolution, boolean skipLiquids, Entity owner,
                                    BlockState fire) {
        Explosion explosion = new Explosion(level, owner, cx, cy, cz, size, false,
                Explosion.BlockInteraction.DESTROY);
        Set<BlockPos> affected = allocate(level, cx, cy, cz, size, resolution, skipLiquids);
        float dropChance = 1.0F / size;

        for (BlockPos pos : affected) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            if (state.canDropFromExplosion(level, pos, explosion) && level.random.nextFloat() < dropChance) {
                for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, null, ItemStack.EMPTY)) {
                    Block.popResource(level, pos, drop);
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            state.getBlock().wasExploded(level, pos, explosion);
        }
        if (fire == null) return;
        for (BlockPos pos : affected) {
            if (level.random.nextInt(3) == 0 && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(),
                    net.minecraft.core.Direction.UP)) {
                level.setBlock(pos, fire, Block.UPDATE_ALL);
            }
        }
    }

    private static Set<BlockPos> allocate(ServerLevel level, double x, double y, double z,
                                          float size, int resolution, boolean skipLiquids) {
        Set<BlockPos> affected = new HashSet<>();
        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                for (int k = 0; k < resolution; k++) {
                    if (i != 0 && i != resolution - 1 && j != 0 && j != resolution - 1
                            && k != 0 && k != resolution - 1) {
                        continue;
                    }
                    double dx = (double) i / (resolution - 1) * 2.0D - 1.0D;
                    double dy = (double) j / (resolution - 1) * 2.0D - 1.0D;
                    double dz = (double) k / (resolution - 1) * 2.0D - 1.0D;
                    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    dx /= length;
                    dy /= length;
                    dz /= length;

                    float power = size * (0.7F + level.random.nextFloat() * 0.6F);
                    double currentX = x;
                    double currentY = y;
                    double currentZ = z;
                    while (power > 0.0F) {
                        BlockPos current = BlockPos.containing(currentX, currentY, currentZ);
                        if (!level.isInWorldBounds(current)) {
                            break;
                        }
                        BlockState state = level.getBlockState(current);
                        boolean liquid = skipLiquids && state.liquid();
                        if (!state.isAir() && !liquid) {
                            power -= (state.getBlock().getExplosionResistance() + 0.3F) * 0.3F;
                        }
                        if (power > 0.0F && !liquid) {
                            affected.add(current.immutable());
                        }
                        currentX += dx * 0.3D;
                        currentY += dy * 0.3D;
                        currentZ += dz * 0.3D;
                        power -= 0.225F;
                    }
                }
            }
        }
        return affected;
    }

    /** Integer-divided shrapnel with a one-in-three chance of looking expensive. */
    public static void spawnShrapnels(ServerLevel level, double x, double y, double z, int count) {
        RandomSource rand = level.random;
        for (int i = 0; i < count; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(ModEntities.SHRAPNEL.get(), level);
            shrapnel.setPos(x, y, z);
            double motionY = ((rand.nextFloat() * 0.5D) + 0.5D) * (1 + (count / (15 + rand.nextInt(21))))
                    + (rand.nextFloat() / 50.0D * count);
            double motionX = rand.nextGaussian() * 1.0D * (1 + (count / 50));
            double motionZ = rand.nextGaussian() * 1.0D * (1 + (count / 50));
            shrapnel.setDeltaMovement(motionX, motionY, motionZ);
            shrapnel.setTrail(rand.nextInt(3) == 0);
            level.addFreshEntity(shrapnel);
        }
    }

    /** Gaussian metal rain. */
    public static void spawnShrapnelShower(ServerLevel level, double x, double y, double z,
                                           double baseMotionX, double baseMotionY, double baseMotionZ,
                                           int count, double deviation) {
        RandomSource rand = level.random;
        for (int i = 0; i < count; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(ModEntities.SHRAPNEL.get(), level);
            shrapnel.setPos(x, y, z);
            shrapnel.setDeltaMovement(
                    baseMotionX + rand.nextGaussian() * deviation,
                    baseMotionY + rand.nextGaussian() * deviation,
                    baseMotionZ + rand.nextGaussian() * deviation);
            shrapnel.setTrail(rand.nextInt(3) == 0);
            level.addFreshEntity(shrapnel);
        }
    }

    /** Throw integer-divided rocks at the witnesses. */
    public static void spawnRubble(ServerLevel level, double x, double y, double z, int count) {
        RandomSource rand = level.random;
        for (int i = 0; i < count; i++) {
            PowerFistRubbleEntity rubble = new PowerFistRubbleEntity(ModEntities.POWER_FIST_RUBBLE.get(), level);
            rubble.setPos(x, y, z);
            double motionY = 0.75D * (1 + ((count + rand.nextInt(count * 5)) / 25));
            double motionX = rand.nextGaussian() * 0.75D * (1 + (count / 50));
            double motionZ = rand.nextGaussian() * 0.75D * (1 + (count / 50));
            rubble.setBlockState(Blocks.STONE.defaultBlockState());
            rubble.setDeltaMovement(motionX, motionY, motionZ);
            level.addFreshEntity(rubble);
        }
    }

    /** Irradiate a small chunk diamond around the fat mine. */
    public static void incrementRad(ServerLevel level, int x, int y, int z, float mult) {
        ChunkRadiationData data = ChunkRadiationData.get(level);
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (Math.abs(i) + Math.abs(j) < 4) {
                    data.increment(new BlockPos(x + i * 16, y, z + j * 16),
                            50.0F / (Math.abs(i) + Math.abs(j) + 1) * mult);
                }
            }
        }
    }

    /** Check the nine blocks where foam might become relevant someday. */
    public static boolean isWaterAbove(ServerLevel level, BlockPos pos) {
        for (int xo = -1; xo <= 1; xo++) {
            for (int zo = -1; zo <= 1; zo++) {
                BlockPos above = pos.offset(xo, 1, zo);
                if (level.getBlockState(above).liquid()
                        && !level.getFluidState(above).is(net.minecraft.tags.FluidTags.LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }
}
