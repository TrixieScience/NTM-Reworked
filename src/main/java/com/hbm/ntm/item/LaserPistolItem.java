package com.hbm.ntm.item;

import com.hbm.ntm.entity.LaserPistolBeamEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.EnergyAmmoType;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;

public final class LaserPistolItem extends SednaGunItem {
    public static final int DURABILITY = 500;
    public static final int CAPACITY = 30;
    public static final int DRAW_TICKS = 10;
    public static final int INSPECT_TICKS = 26;
    public static final int FIRE_DELAY = 5;
    public static final int DRY_TICKS = 5;
    public static final int RELOAD_TICKS = 45;
    public static final int JAM_TICKS = 37;
    public static final float BASE_DAMAGE = 25.0F;
    public static final float INNATE_SPREAD = 1.0F;
    public static final float HIP_SPREAD = 1.0F;
    public static final float MAX_WEAR_SPREAD = 0.125F;
    public static final int PEW_PEW_CAPACITY = 10;
    public static final int PEW_PEW_FIRE_DELAY = 10;
    public static final int PEW_PEW_ROUNDS_PER_CYCLE = 5;
    public static final float PEW_PEW_BASE_DAMAGE = 30.0F;
    public static final float PEW_PEW_INNATE_SPREAD = 0.25F;
    public static final float PEW_PEW_FIRE_PITCH = 0.8F;
    public static final int MORNING_GLORY_DURABILITY = 1500;
    public static final int MORNING_GLORY_CAPACITY = 20;
    public static final int MORNING_GLORY_FIRE_DELAY = 7;
    public static final float MORNING_GLORY_BASE_DAMAGE = 20.0F;
    public static final float MORNING_GLORY_INNATE_SPREAD = 0.0F;
    public static final float MORNING_GLORY_HIP_SPREAD = 0.5F;
    public static final float MORNING_GLORY_FIRE_PITCH = 1.1F;
    public static final float MORNING_GLORY_ARMOR_PIERCING = 0.5F;
    public static final float MORNING_GLORY_THRESHOLD_NEGATION = 10.0F;
    public static final float MORNING_GLORY_OVERCHARGE_THRESHOLD_NEGATION = 15.0F;

    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_0";
    private static final String TIMER = "timer_0";
    private static final String WEAR = "wear_0";
    private static final String MAG_COUNT = "magcount0";
    private static final String MAG_TYPE = "magtype0";
    private static final String MAG_PREV = "magprev0";
    private static final String MAG_AFTER = "magafter0";
    private static final String AIMING = "aiming";
    private static final String CANCEL_RELOAD = "cancel";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    private final Variant variant;

    public LaserPistolItem() {
        this(Variant.STANDARD);
    }

    public LaserPistolItem(Variant variant) {
        this.variant = variant;
    }

    public Variant variant() { return variant; }
    public float baseDamage() { return variant.baseDamage; }
    public int roundsPerCycle() { return variant.roundsPerCycle; }
    public float armorPiercing() { return variant.emeraldBeam ? MORNING_GLORY_ARMOR_PIERCING : 0.0F; }
    public float armorThresholdNegation(EnergyAmmoType ammo) {
        if (!variant.emeraldBeam) return 0.0F;
        return ammo == EnergyAmmoType.OVERCHARGE
                ? MORNING_GLORY_OVERCHARGE_THRESHOLD_NEGATION
                : MORNING_GLORY_THRESHOLD_NEGATION;
    }

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
            default -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return variant.capacity; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return variant.durability; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return loadedAmmo(stack).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public float recoilVertical() { return 0.0F; }
    @Override public float recoilHorizontalSigma() { return 0.0F; }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;

        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);
        GunState previous = state(tag);
        if (!held) {
            if (previous != GunState.JAMMED) {
                setState(tag, GunState.DRAWING);
                tag.putInt(TIMER, DRAW_TICKS);
            }
            tag.putInt(LAST_ANIM, GunAnimation.CYCLE.ordinal());
            tag.putBoolean(AIMING, false);
            tag.putBoolean(CANCEL_RELOAD, false);
            tag.putBoolean(EQUIPPED, false);
            save(stack, tag);
            return;
        }

        if (!tag.getBoolean(EQUIPPED)) playAnimation(tag, GunAnimation.EQUIP);
        tag.putBoolean(EQUIPPED, true);
        int animationTimer = tag.getInt(ANIM_TIMER);
        playOrchestra(level, living, animation(tag), animationTimer);
        tag.putInt(ANIM_TIMER, animationTimer + 1);

        int timer = tag.getInt(TIMER);
        if (timer > 0) tag.putInt(TIMER, timer - 1);
        if (timer <= 1) decide(living, tag, previous);
        save(stack, tag);
    }

    private void decide(LivingEntity living, CompoundTag tag, GunState previous) {
        if (previous == GunState.DRAWING || previous == GunState.JAMMED
                || previous == GunState.COOLDOWN) {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        if (!tag.getBoolean(CANCEL_RELOAD)) reloadAction(player, tag);
        tag.putBoolean(CANCEL_RELOAD, false);
        tag.putInt(MAG_AFTER, roundsValue(tag));
        if (jamChance(tag.getFloat(WEAR), variant.durability) > living.getRandom().nextFloat()) {
            setState(tag, GunState.JAMMED);
            tag.putInt(TIMER, JAM_TICKS);
            playAnimation(tag, GunAnimation.JAMMED);
        } else {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
        }
    }

    private void pressPrimary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        GunState current = state(tag);
        if (current == GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, true);
            save(stack, tag);
            return;
        }
        if (current != GunState.IDLE) return;
        if (roundsValue(tag) <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, variant.fireDelay);
            save(stack, tag);
            return;
        }
        fire(player, tag);
        save(stack, tag);
    }

    private void fire(Player player, CompoundTag tag) {
        if (!(player.level() instanceof ServerLevel level)) return;
        EnergyAmmoType ammo = EnergyAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, variant.durability);
        boolean aiming = tag.getBoolean(AIMING);
        Vec3 origin = projectileOrigin(player);
        Vec3 heading = player.getLookAngle();
        int fired = Math.min(roundsValue(tag), variant.roundsPerCycle);

        for (int round = 0; round < fired; round++) {
            float damage = variant.baseDamage * wearDamageMultiplier(currentWear, variant.durability)
                    * ammo.damageMultiplier();
            float spread = variant.innateSpread + (aiming ? 0.0F : variant.hipSpread)
                    + wearSpread(currentWear, variant.durability);
            LaserPistolBeamEntity beam = new LaserPistolBeamEntity(level, player, ammo, damage,
                    spread, new Vec3(-0.1875D, -0.09375D, 0.75D), variant.emeraldBeam);
            beam.performHitscan();
            level.addFreshEntity(beam);
            currentWear = Math.min(currentWear + ammo.wear(), variant.durability);
        }
        play(level, player, ModSounds.GUN_LASER_PISTOL_FIRE.get(), 1.0F, variant.firePitch);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, Math.max(0, roundsValue(tag) - fired));
        tag.putFloat(WEAR, currentWear);
        setState(tag, GunState.COOLDOWN);
        tag.putInt(TIMER, variant.fireDelay);
        playAnimation(tag, GunAnimation.CYCLE);
    }

    private void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;
        tag.putBoolean(AIMING, false);
        if (canReload(player.getInventory(), tag)) {
            tag.putInt(MAG_PREV, roundsValue(tag));
            setState(tag, GunState.RELOADING);
            tag.putInt(TIMER, RELOAD_TICKS);
            playAnimation(tag, GunAnimation.RELOAD);
        } else {
            playAnimation(tag, GunAnimation.INSPECT);
        }
        save(stack, tag);
    }

    private static void toggleAim(ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(AIMING, !tag.getBoolean(AIMING));
        save(stack, tag);
    }

    private boolean canReload(Inventory inventory, CompoundTag gun) {
        int count = roundsValue(gun);
        if (count >= variant.capacity) return false;
        EnergyAmmoType required = count > 0
                ? EnergyAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE)) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = roundsValue(gun);
        EnergyAmmoType type = loaded > 0
                ? EnergyAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());
        for (int slot = 0; slot < inventory.getContainerSize() && loaded < variant.capacity; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get())
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            int consumed = Math.min(variant.capacity - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private static EnergyAmmoType findFirstAmmo(Inventory inventory, EnergyAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (!(StandardAmmoTypes.fromStack(candidate) instanceof EnergyAmmoType type)) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player) {
        Vec3 local = new Vec3(-0.1875D, -0.09375D, 0.75D);
        return player.getEyePosition().add(local.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD));
    }

    private static void playOrchestra(Level level, LivingEntity entity, GunAnimation animation, int timer) {
        if (animation == GunAnimation.CYCLE_DRY && timer == 0) {
            play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 1.0F, 1.5F);
        } else if (animation == GunAnimation.RELOAD) {
            if (timer == 0) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 1.0F, 1.0F);
            if (timer == 10) play(level, entity, ModSounds.GUN_MAG_SMALL_REMOVE.get(), 1.0F, 1.25F);
            if (timer == 34) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get(), 1.0F, 1.25F);
            if (timer == 40) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F, 1.25F);
        } else if (animation == GunAnimation.JAMMED) {
            if (timer == 10) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 1.0F, 1.0F);
            if (timer == 15) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F, 1.25F);
            if (timer == 30) play(level, entity, ModSounds.GUN_IMPACT.get(), 0.25F, 1.5F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, volume, pitch);
    }

    public static float jamChance(float wear) {
        return jamChance(wear, DURABILITY);
    }

    public static float jamChance(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.66F ? 0.0F : Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    public static float wearDamageMultiplier(float wear) {
        return wearDamageMultiplier(wear, DURABILITY);
    }

    public static float wearDamageMultiplier(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static float wearSpread(float wear) {
        return wearSpread(wear, DURABILITY);
    }

    public static float wearSpread(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F * MAX_WEAR_SPREAD;
    }

    public static int rounds(ItemStack stack) {
        return Mth.clamp(data(stack).getInt(MAG_COUNT), 0, variant(stack).capacity);
    }
    private int roundsValue(CompoundTag tag) {
        return Mth.clamp(tag.getInt(MAG_COUNT), 0, variant.capacity);
    }
    public static float wear(ItemStack stack) {
        return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, variant(stack).durability);
    }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static EnergyAmmoType loadedAmmo(ItemStack stack) {
        return EnergyAmmoType.fromLegacyBulletConfig(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    EnergyAmmoType ammo, float wear) {
        CompoundTag tag = data(stack);
        setState(tag, state);
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, variant(stack).capacity));
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, variant(stack).durability));
        tag.putBoolean(EQUIPPED, true);
        save(stack, tag);
    }

    private static GunState state(CompoundTag tag) {
        int ordinal = tag.getByte(STATE);
        return ordinal >= 0 && ordinal < GunState.values().length
                ? GunState.values()[ordinal] : GunState.DRAWING;
    }
    private static void setState(CompoundTag tag, GunState state) { tag.putByte(STATE, (byte) state.ordinal()); }
    private static GunAnimation animation(CompoundTag tag) {
        int ordinal = tag.getInt(LAST_ANIM);
        return ordinal >= 0 && ordinal < GunAnimation.values().length
                ? GunAnimation.values()[ordinal] : GunAnimation.CYCLE;
    }
    private static void playAnimation(CompoundTag tag, GunAnimation animation) {
        tag.putInt(LAST_ANIM, animation.ordinal());
        tag.putInt(ANIM_TIMER, 0);
    }
    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(INITIALIZED)) {
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(MAG_TYPE, variant(stack).defaultAmmo.legacyBulletConfig());
            tag.putInt(MAG_COUNT, 0);
        }
        return tag;
    }
    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        EnergyAmmoType ammo = loadedAmmo(stack);
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + variant.capacity).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trim(variant.baseDamage))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                .append(": " + trim(variant.baseDamage * ammo.damageMultiplier())).withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((variant.durability - wear(stack)) * 100.0F
                / variant.durability), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(variant.quality).withStyle(variant.qualityColor));
    }

    private static String trim(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) return Integer.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static Variant variant(ItemStack stack) {
        return stack.getItem() instanceof LaserPistolItem gun ? gun.variant : Variant.STANDARD;
    }

    public enum Variant {
        STANDARD(DURABILITY, CAPACITY, FIRE_DELAY, 1, BASE_DAMAGE, INNATE_SPREAD, HIP_SPREAD, 1.0F,
                EnergyAmmoType.STANDARD, "gui.weapon.quality.aside", ChatFormatting.YELLOW, false),
        PEW_PEW(DURABILITY, PEW_PEW_CAPACITY, PEW_PEW_FIRE_DELAY, PEW_PEW_ROUNDS_PER_CYCLE,
                PEW_PEW_BASE_DAMAGE, PEW_PEW_INNATE_SPREAD, HIP_SPREAD, PEW_PEW_FIRE_PITCH,
                EnergyAmmoType.OVERCHARGE, "gui.weapon.quality.bside", ChatFormatting.GOLD, false),
        MORNING_GLORY(MORNING_GLORY_DURABILITY, MORNING_GLORY_CAPACITY,
                MORNING_GLORY_FIRE_DELAY, 1, MORNING_GLORY_BASE_DAMAGE,
                MORNING_GLORY_INNATE_SPREAD, MORNING_GLORY_HIP_SPREAD,
                MORNING_GLORY_FIRE_PITCH, EnergyAmmoType.OVERCHARGE,
                "gui.weapon.quality.legendary", ChatFormatting.RED, true);

        private final int durability;
        private final int capacity;
        private final int fireDelay;
        private final int roundsPerCycle;
        private final float baseDamage;
        private final float innateSpread;
        private final float hipSpread;
        private final float firePitch;
        private final EnergyAmmoType defaultAmmo;
        private final String quality;
        private final ChatFormatting qualityColor;
        private final boolean emeraldBeam;

        Variant(int durability, int capacity, int fireDelay, int roundsPerCycle, float baseDamage,
                float innateSpread, float hipSpread, float firePitch, EnergyAmmoType defaultAmmo,
                String quality, ChatFormatting qualityColor, boolean emeraldBeam) {
            this.durability = durability;
            this.capacity = capacity;
            this.fireDelay = fireDelay;
            this.roundsPerCycle = roundsPerCycle;
            this.baseDamage = baseDamage;
            this.innateSpread = innateSpread;
            this.hipSpread = hipSpread;
            this.firePitch = firePitch;
            this.defaultAmmo = defaultAmmo;
            this.quality = quality;
            this.qualityColor = qualityColor;
            this.emeraldBeam = emeraldBeam;
        }

        public int durability() { return durability; }
        public int capacity() { return capacity; }
        public int fireDelay() { return fireDelay; }
        public int roundsPerCycle() { return roundsPerCycle; }
        public float baseDamage() { return baseDamage; }
        public float innateSpread() { return innateSpread; }
        public float hipSpread() { return hipSpread; }
        public float firePitch() { return firePitch; }
        public EnergyAmmoType defaultAmmo() { return defaultAmmo; }
        public boolean emeraldBeam() { return emeraldBeam; }
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    public enum GunAnimation { CYCLE, CYCLE_DRY, EQUIP, RELOAD, JAMMED, INSPECT }
}
