package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HeReceiver;
import com.hbm.ntm.block.TurretFriendlyBlock;
import com.hbm.ntm.compat.TurretTargetingFrame;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.entity.TauBeamEntity;
import com.hbm.ntm.inventory.TurretFriendlyMenu;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.item.TurretChipItem;
import com.hbm.ntm.network.SpentCasingPayload;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import com.hbm.ntm.weapon.FiftyCalAmmoType;
import com.hbm.ntm.weapon.SpentCasingPreset;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.TauAmmoType;
import com.hbm.ntm.weapon.TurretShellAmmoType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/** Shared guts for the first four source NT turrets. */
public final class TurretFriendlyBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider, HeReceiver, TurretTargetingFrame {
    public static final int SLOT_COUNT = 11;
    public static final int CHIP = 0;
    public static final int BATTERY = 10;
    public static final long MAX_POWER = 10_000L;
    public static final long CONSUMPTION = 100L;
    public static final double RANGE = 32.0D;
    private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> (int) power;
                case 1 -> isOn ? 1 : 0;
                case 2 -> targetPlayers ? 1 : 0;
                case 3 -> targetAnimals ? 1 : 0;
                case 4 -> targetMobs ? 1 : 0;
                case 5 -> targetMachines ? 1 : 0;
                case 6 -> stattrak;
                case 7 -> variant.ordinal();
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> power = value;
                case 1 -> isOn = value != 0;
                case 2 -> targetPlayers = value != 0;
                case 3 -> targetAnimals = value != 0;
                case 4 -> targetMobs = value != 0;
                case 5 -> targetMachines = value != 0;
                case 6 -> stattrak = value;
                default -> { }
            }
        }
        @Override public int getCount() { return 8; }
    };

    private long power;
    private boolean isOn;
    private boolean targetPlayers;
    private boolean targetAnimals;
    private boolean targetMobs = true;
    private boolean targetMachines = true;
    private float yaw;
    private float oldYaw;
    private float pitch;
    private float oldPitch;
    private float spin;
    private float oldSpin;
    private float acceleration;
    private int firingTimer;
    private int reloadTimer;
    private int casingTimer;
    private int targetId = -1;
    private int stattrak;
    private Entity lastFiredTarget;
    private final TurretVariant variant;

    public TurretFriendlyBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TURRET_FRIENDLY.get(), pos, state, TurretVariant.FRIENDLY);
    }

    private TurretFriendlyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                      TurretVariant variant) {
        super(type, pos, state);
        this.variant = variant;
    }

    public static TurretFriendlyBlockEntity create(TurretVariant variant, BlockPos pos, BlockState state) {
        BlockEntityType<?> type = switch (variant) {
            case CHEKHOV -> ModBlockEntities.TURRET_CHEKHOV.get();
            case FRIENDLY -> ModBlockEntities.TURRET_FRIENDLY.get();
            case JEREMY -> ModBlockEntities.TURRET_JEREMY.get();
            case TAUON -> ModBlockEntities.TURRET_TAUON.get();
        };
        return new TurretFriendlyBlockEntity(type, pos, state, variant);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TurretFriendlyBlockEntity turret) {
        if (level.isClientSide) turret.clientTick();
        else turret.serverTick((ServerLevel) level, pos, state);
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (reloadTimer > 0 && --reloadTimer == 1 && variant == TurretVariant.JEREMY) {
            level.playSound(null, BlockPos.containing(worldPivot()), ModSounds.TURRET_JEREMY_RELOAD.get(),
                    SoundSource.BLOCKS, 2F, 1F);
        }
        if (casingTimer > 0 && --casingTimer == 0) ejectJeremyCasing(level);
        if (lastFiredTarget != null && (!lastFiredTarget.isAlive() || lastFiredTarget.isRemoved())) {
            stattrak++;
            lastFiredTarget = null;
        }
        if (level.getGameTime() % 40L == 0L) subscribeAround(level, pos, state);
        dischargeBattery();

        Entity target = targetId < 0 ? null : level.getEntity(targetId);
        if (level.getGameTime() % 10L == 0L || !validTarget(target)) {
            target = findTarget(level);
            targetId = target == null ? -1 : target.getId();
        }

        oldYaw = yaw;
        oldPitch = pitch;
        if (isOn && power >= variant.consumption()) {
            power -= variant.consumption();
            if (target != null) {
                aimAt(target);
                firingTimer++;
                if (readyToFire() && aligned(target)) fire(level, target);
            } else {
                firingTimer = Mth.clamp(firingTimer - 1, 0, 20);
            }
        } else {
            firingTimer = Mth.clamp(firingTimer - 1, 0, 20);
        }
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private void clientTick() {
        oldSpin = spin;
        if (variant == TurretVariant.TAUON) {
            if (targetId >= 0 && isOn) spin += 45F;
        } else if (variant == TurretVariant.CHEKHOV || variant == TurretVariant.FRIENDLY) {
            acceleration = targetId >= 0 && isOn ? Math.min(45F, acceleration + 2F)
                    : Math.max(0F, acceleration - 2F);
            spin += acceleration;
        }
        if (spin >= 360F) { spin -= 360F; oldSpin -= 360F; }
    }

    private void subscribeAround(ServerLevel level, BlockPos core, BlockState state) {
        BlockPos[] parts = TurretFriendlyBlock.parts(core, state.getValue(TurretFriendlyBlock.FACING));
        for (BlockPos part : parts) for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos target = part.relative(direction);
            boolean inside = false;
            for (BlockPos other : parts) if (other.equals(target)) { inside = true; break; }
            if (!inside) trySubscribe(level, target, direction);
        }
    }

    private void dischargeBattery() {
        ItemStack stack = items.get(BATTERY);
        if (!(stack.getItem() instanceof HeBatteryItem battery)) return;
        long amount = Math.min(Math.min(variant.maxPower() - power, battery.getDischargeRate(stack)), battery.getCharge(stack));
        if (amount > 0) { battery.discharge(stack, amount); power += amount; }
    }

    @Nullable private Entity findTarget(ServerLevel level) {
        Vec3 pivot = worldPivot();
        List<Entity> candidates = level.getEntitiesOfClass(Entity.class,
                new AABB(pivot, pivot).inflate(variant.range()), this::validTarget);
        return candidates.stream().filter(entity -> hasLineOfSight(level, pivot, hbm$entityEyePosition(entity)))
                .min(Comparator.comparingDouble(entity -> hbm$entityPosition(entity).distanceToSqr(pivot)))
                .orElse(null);
    }

    private boolean validTarget(@Nullable Entity entity) {
        if (entity == null || !entity.isAlive() || entity.isRemoved()) return false;
        if (hbm$entityPosition(entity).distanceToSqr(worldPivot()) < variant.grace() * variant.grace()) return false;
        List<String> whitelist = TurretChipItem.names(items.get(CHIP));
        if (entity instanceof Player player) {
            return targetPlayers && !player.isSpectator() && !player.getAbilities().instabuild
                    && !whitelist.contains(player.getDisplayName().getString());
        }
        if (entity instanceof LivingEntity living && living.hasCustomName()
                && whitelist.contains(living.getCustomName().getString())) return false;
        if (targetAnimals && (entity instanceof Animal || entity instanceof net.minecraft.world.entity.npc.Npc)) {
            return true;
        }
        if (targetMobs && entity instanceof Monster && !(entity instanceof EnderDragon)) return true;
        return targetMachines && entity instanceof AbstractMinecart;
    }

    private static boolean hasLineOfSight(ServerLevel level, Vec3 start, Vec3 end) {
        return level.clip(new net.minecraft.world.level.ClipContext(start, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                net.minecraft.world.phys.shapes.CollisionContext.empty())).getType()
                == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    private void aimAt(Entity target) {
        Aim wanted = wantedAim(target);
        yaw = stepAngle(yaw, wanted.yaw(), variant.yawSpeed());
        pitch = Mth.clamp(stepAngle(pitch, wanted.pitch(), variant.pitchSpeed()),
                variant.minPitch(), variant.maxPitch());
    }

    private boolean aligned(Entity target) {
        Aim wanted = wantedAim(target);
        if (Math.abs(Mth.wrapDegrees(wanted.yaw() - yaw)) > 15F
                || Math.abs(Mth.wrapDegrees(wanted.pitch() - pitch)) > 15F) return false;

        Vec3 localHeading = barrelDirection(pitch, yaw);
        Vec3 localMuzzle = localPivot().add(localHeading.scale(variant.barrelLength()));
        Vec3 muzzle = hbm$localPositionToWorld(localMuzzle);
        Vec3 velocity = hbm$localVectorToWorld(localHeading).normalize().scale(10D)
                .add(hbm$velocityAt(localMuzzle));
        return variant == TurretVariant.TAUON
                || shotIntersects(target.getBoundingBox().inflate(0.3D), muzzle, velocity, variant.range());
    }

    public static boolean shotIntersects(AABB targetBounds, Vec3 muzzle, Vec3 velocity, double range) {
        if (velocity.lengthSqr() < 1.0E-9D) return false;
        return targetBounds.clip(muzzle, muzzle.add(velocity.normalize().scale(range))).isPresent();
    }

    public static boolean shotIntersects(AABB targetBounds, Vec3 muzzle, Vec3 velocity) {
        return shotIntersects(targetBounds, muzzle, velocity, RANGE);
    }

    private Aim wantedAim(Entity target) {
        Vec3 localMuzzle = localPivot().add(barrelDirection(pitch, yaw).scale(variant.barrelLength()));
        Vec3 delta = hbm$entityEyePosition(target).subtract(hbm$localPositionToWorld(localMuzzle));
        Vec3 worldDirection = compensatedDirection(delta, hbm$velocityAt(localMuzzle), 10D);
        Vec3 localDirection = hbm$worldVectorToLocal(worldDirection).normalize();
        float wantedYaw = (float) (-Mth.atan2(localDirection.x, localDirection.z) * Mth.RAD_TO_DEG);
        float wantedPitch = (float) (-Mth.atan2(localDirection.y,
                Math.sqrt(localDirection.x * localDirection.x + localDirection.z * localDirection.z))
                * Mth.RAD_TO_DEG);
        return new Aim(wantedYaw, wantedPitch);
    }

    public static Vec3 compensatedDirection(Vec3 targetDelta, Vec3 inheritedVelocity, double speed) {
        double a = inheritedVelocity.lengthSqr() - speed * speed;
        double b = -2D * targetDelta.dot(inheritedVelocity);
        double c = targetDelta.lengthSqr();
        double time;
        if (Math.abs(a) < 1.0E-9D) {
            time = Math.abs(b) < 1.0E-9D ? -1D : -c / b;
        } else {
            double discriminant = b * b - 4D * a * c;
            if (discriminant < 0D) return targetDelta.normalize();
            double root = Math.sqrt(discriminant);
            double first = (-b - root) / (2D * a);
            double second = (-b + root) / (2D * a);
            time = first > 0D && second > 0D ? Math.min(first, second) : Math.max(first, second);
        }
        if (time <= 0D || !Double.isFinite(time)) return targetDelta.normalize();
        return targetDelta.subtract(inheritedVelocity.scale(time)).normalize();
    }

    private static float stepAngle(float value, float target, float max) {
        return value + Mth.clamp(Mth.wrapDegrees(target - value), -max, max);
    }

    private boolean readyToFire() {
        return switch (variant) {
            case CHEKHOV -> firingTimer > 20 && firingTimer % 2 == 0;
            case FRIENDLY -> firingTimer > 20 && firingTimer % 5 == 0;
            case TAUON -> firingTimer % 5 == 0;
            case JEREMY -> firingTimer % 40 == 0;
        };
    }

    private void fire(ServerLevel level, Entity target) {
        int slot = firstAmmoSlot();
        if (slot < 0) return;
        if (variant == TurretVariant.TAUON) {
            fireTauon(level, target, slot);
            return;
        }

        var ammo = variant == TurretVariant.JEREMY
                ? TurretShellAmmoType.fromStack(items.get(slot))
                : StandardAmmoTypes.fromStack(items.get(slot));
        float baseDamage = switch (variant) {
            case CHEKHOV, FRIENDLY -> 10F;
            case JEREMY -> 50F;
            case TAUON -> 0F;
        };
        Vec3 localHeading = barrelDirection(pitch, yaw);
        Vec3 heading = hbm$localVectorToWorld(localHeading).normalize();
        Vec3 localMuzzle = localPivot().add(localHeading.scale(variant.barrelLength()));
        Vec3 muzzle = hbm$localPositionToWorld(localMuzzle);
        BulletEntity bullet = new BulletEntity(level, null, ammo, baseDamage * ammo.damageMultiplier(),
                ammo.spread(), muzzle, heading);
        bullet.setDeltaMovement(bullet.getDeltaMovement().add(hbm$velocityAt(localMuzzle)));
        level.addFreshEntity(bullet);
        lastFiredTarget = target;
        consume(slot);
        if (variant == TurretVariant.JEREMY) {
            level.playSound(null, BlockPos.containing(worldPivot()), ModSounds.TURRET_JEREMY_FIRE.get(),
                    SoundSource.BLOCKS, 4F, 1F);
            level.sendParticles(ParticleTypes.EXPLOSION, muzzle.x, muzzle.y, muzzle.z,
                    5, 0.1D, 0.1D, 0.1D, 0D);
            reloadTimer = 20;
            casingTimer = 22;
        } else {
            level.playSound(null, BlockPos.containing(worldPivot()), ModSounds.GUN_M2_FIRE.get(),
                    SoundSource.BLOCKS, 2F, 1F);
            level.sendParticles(ParticleTypes.EXPLOSION, muzzle.x, muzzle.y, muzzle.z, 1, 0, 0, 0, 0);
            ejectSmallCasing(level, ammo);
        }
    }

    private void fireTauon(ServerLevel level, Entity target, int slot) {
        Vec3 localHeading = barrelDirection(pitch, yaw);
        Vec3 localMuzzle = localPivot().add(localHeading.scale(variant.barrelLength()));
        Vec3 muzzle = hbm$localPositionToWorld(localMuzzle);
        Vec3 targetEye = hbm$entityEyePosition(target);
        float damage = 30F + level.random.nextInt(11);
        target.invulnerableTime = 0;
        target.hurt(level.damageSources().source(com.hbm.ntm.radiation.ModDamageTypes.ELECTRIC), damage);
        TauBeamEntity beam = TauBeamEntity.visual(level, muzzle, targetEye.subtract(muzzle));
        level.addFreshEntity(beam);
        consume(slot);
        lastFiredTarget = target;
        level.playSound(null, BlockPos.containing(worldPivot()), ModSounds.TURRET_TAU_FIRE.get(),
                SoundSource.BLOCKS, 4F, 0.9F + level.random.nextFloat() * 0.3F);
        for (int i = 0; i < 5; i++) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, muzzle.x, muzzle.y, muzzle.z,
                    1, 0.15D, 0.15D, 0.15D, 0.05D);
        }
    }

    private void consume(int slot) {
        items.get(slot).shrink(1);
        if (items.get(slot).isEmpty()) items.set(slot, ItemStack.EMPTY);
    }

    private void ejectSmallCasing(ServerLevel level, com.hbm.ntm.weapon.SednaAmmoType ammo) {
        Vec3 localMotion = new Vec3(-0.3D, 0.6D, 0D)
                .xRot(pitch * Mth.DEG_TO_RAD)
                .yRot(-yaw * Mth.DEG_TO_RAD);
        Vec3 localSpawn = localPivot().add(new Vec3(-1.125D, 0.125D, 0.25D)
                .zRot(pitch * Mth.DEG_TO_RAD)
                .yRot(-(yaw * Mth.DEG_TO_RAD + Mth.HALF_PI)));
        Vec3 spawn = hbm$localPositionToWorld(localSpawn);
        Vec3 side = hbm$localVectorToWorld(localMotion).add(hbm$velocityAt(localSpawn));
        SpentCasingPreset preset = ammo instanceof FiftyCalAmmoType fifty
                && (fifty == FiftyCalAmmoType.ARMOR_PIERCING
                || fifty == FiftyCalAmmoType.DEPLETED_URANIUM
                || fifty == FiftyCalAmmoType.HIGH_EXPLOSIVE)
                ? SpentCasingPreset.RIFLE_STEEL : SpentCasingPreset.RIFLE_BRASS;
        SpentCasingPayload payload = new SpentCasingPayload(preset.ordinal(),
                spawn.x, spawn.y, spawn.z, side.x, side.y, side.z,
                yaw, pitch, 0.02F + level.random.nextFloat() * 0.03F,
                0.02F + level.random.nextFloat() * 0.03F);
        PacketDistributor.sendToPlayersNear(level, null, spawn.x, spawn.y, spawn.z, 50, payload);
    }

    private void ejectJeremyCasing(ServerLevel level) {
        Vec3 localSpawn = localPivot().add(barrelDirection(pitch, yaw).scale(-2D));
        Vec3 spawn = hbm$localPositionToWorld(localSpawn);
        Vec3 localMotion = new Vec3(-0.2D, -0.2D, 0D)
                .xRot(pitch * Mth.DEG_TO_RAD).yRot(-yaw * Mth.DEG_TO_RAD);
        Vec3 motion = hbm$localVectorToWorld(localMotion).add(hbm$velocityAt(localSpawn));
        SpentCasingPayload payload = new SpentCasingPayload(SpentCasingPreset.SHELL_240MM.ordinal(),
                spawn.x, spawn.y, spawn.z, motion.x, motion.y, motion.z,
                yaw, pitch, 0.01F, -5F);
        PacketDistributor.sendToPlayersNear(level, null, spawn.x, spawn.y, spawn.z, 100, payload);
    }

    private int firstAmmoSlot() {
        for (int slot = 1; slot <= 9; slot++) {
            if (variant.accepts(items.get(slot))) return slot;
        }
        return -1;
    }

    public static Vec3 barrelDirection(float pitch, float yaw) {
        return Vec3.directionFromRotation(pitch, yaw);
    }

    private Vec3 localPivot() {
        Direction facing = getBlockState().getValue(TurretFriendlyBlock.FACING);
        Vec3 offset = TurretFriendlyBlock.horizontalOffset(facing);
        return Vec3.atLowerCornerOf(worldPosition).add(offset.x, 1.5D, offset.z);
    }

    private Vec3 worldPivot() { return hbm$localPositionToWorld(localPivot()); }

    private record Aim(float yaw, float pitch) { }

    public boolean clickButton(int id) {
        switch (id) {
            case 0 -> isOn = !isOn;
            case 1 -> targetPlayers = !targetPlayers;
            case 2 -> targetAnimals = !targetAnimals;
            case 3 -> targetMobs = !targetMobs;
            case 4 -> targetMachines = !targetMachines;
            default -> { return false; }
        }
        setChanged();
        return true;
    }

    public void addWhitelistName(String name) {
        ItemStack chip = items.get(CHIP);
        String clean = name.strip();
        if (clean.isEmpty() || clean.length() > 25 || !(chip.getItem() instanceof TurretChipItem)
                || TurretChipItem.contains(chip, clean)) return;
        TurretChipItem.add(chip, clean);
        setChanged();
    }

    public void removeWhitelistName(int index) {
        ItemStack chip = items.get(CHIP);
        if (!(chip.getItem() instanceof TurretChipItem)) return;
        TurretChipItem.remove(chip, index);
        setChanged();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putLong("power", power);
        tag.putBoolean("isOn", isOn);
        tag.putBoolean("targetPlayers", targetPlayers);
        tag.putBoolean("targetAnimals", targetAnimals);
        tag.putBoolean("targetMobs", targetMobs);
        tag.putBoolean("targetMachines", targetMachines);
        tag.putFloat("yaw", yaw); tag.putFloat("pitch", pitch); tag.putFloat("spin", spin);
        tag.putInt("targetId", targetId);
        tag.putInt("stattrak", stattrak);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        power = tag.getLong("power"); isOn = tag.getBoolean("isOn");
        targetPlayers = tag.getBoolean("targetPlayers"); targetAnimals = tag.getBoolean("targetAnimals");
        targetMobs = tag.getBoolean("targetMobs"); targetMachines = tag.getBoolean("targetMachines");
        yaw = oldYaw = tag.getFloat("yaw"); pitch = oldPitch = tag.getFloat("pitch");
        spin = oldSpin = tag.getFloat("spin"); targetId = tag.getInt("targetId");
        stattrak = tag.getInt("stattrak");
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag(); saveAdditional(tag, registries); return tag;
    }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public Component getDisplayName() { return Component.translatable(variant.titleKey()); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TurretFriendlyMenu(id, inventory, this, data);
    }

    public float yaw(float partial) { return Mth.lerp(partial, oldYaw, yaw); }
    public float pitch(float partial) { return Mth.lerp(partial, oldPitch, pitch); }
    public float spin(float partial) { return Mth.lerp(partial, oldSpin, spin); }
    public TurretVariant variant() { return variant; }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = Mth.clamp(power, 0L, variant.maxPower()); }
    @Override public long getMaxPower() { return variant.maxPower(); }
    @Override public boolean isHeLoaded() { return hasLevel() && !isRemoved(); }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(items, slot, amount); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); setChanged(); }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64D;
    }
    @Override public void clearContent() { items.clear(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == CHIP) return stack.getItem() instanceof TurretChipItem;
        if (slot == BATTERY) return stack.getItem() instanceof HeBatteryItem;
        return variant.accepts(stack);
    }
    @Override public int[] getSlotsForFace(Direction side) { return ALL_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return true; }
}
