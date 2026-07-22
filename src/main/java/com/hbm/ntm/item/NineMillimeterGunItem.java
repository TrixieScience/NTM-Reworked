package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.NineMillimeterAmmoType;
import com.hbm.ntm.weapon.WeaponModManager;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.SpentCasingEffects;
import com.hbm.ntm.weapon.SpentCasingPreset;
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

/** Shared greasy internals for the M3 and single Uzi. */
public final class NineMillimeterGunItem extends SednaGunItem {
    public static final int DURABILITY = 3_000;
    public static final int CAPACITY = 30;
    public static final float BASE_DAMAGE = 3.0F;
    public static final float HIP_SPREAD = 0.025F;
    public static final float MAX_WEAR_SPREAD = 0.125F;

    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_0";
    private static final String TIMER = "timer_0";
    private static final String WEAR = "wear_0";
    private static final String MAG_COUNT = "magcount0";
    private static final String MAG_TYPE = "magtype0";
    private static final String MAG_PREV = "magprev0";
    private static final String MAG_AFTER = "magafter0";
    private static final String AIMING = "aiming";
    private static final String PRIMARY_HELD = "primary0";
    private static final String CANCEL_RELOAD = "cancel";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    private final Variant variant;

    public NineMillimeterGunItem(Variant variant) {
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case PRIMARY_RELEASE -> releasePrimary(stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public boolean gunAutomatic() { return true; }
    @Override public SednaCrosshair gunCrosshair() {
        return variant == Variant.GREASE_GUN ? SednaCrosshair.L_CIRCLE : SednaCrosshair.CIRCLE;
    }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public float gunDurability(ItemStack stack) { return durability(stack); }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return loadedAmmo(stack).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public float recoilVertical() { return variant.recoilVertical; }
    @Override public float recoilHorizontalSigma() { return variant.recoilHorizontalSigma; }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;

        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);
        GunState previous = state(tag);

        if (!held) {
            if (previous != GunState.JAMMED) {
                tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
                tag.putInt(TIMER, variant.drawTicks);
            }
            tag.putInt(LAST_ANIM, GunAnimation.CYCLE.ordinal());
            tag.putBoolean(AIMING, false);
            tag.putBoolean(PRIMARY_HELD, false);
            tag.putBoolean(CANCEL_RELOAD, false);
            tag.putBoolean(EQUIPPED, false);
            save(stack, tag);
            return;
        }

        if (!tag.getBoolean(EQUIPPED)) playAnimation(tag, GunAnimation.EQUIP);
        tag.putBoolean(EQUIPPED, true);

        int animationTimer = tag.getInt(ANIM_TIMER);
        playOrchestra(level, living, stack, tag, animation(tag), animationTimer, variant);
        tag.putInt(ANIM_TIMER, animationTimer + 1);

        int timer = tag.getInt(TIMER);
        if (timer > 0) tag.putInt(TIMER, timer - 1);
        if (timer <= 1) decide(living, stack, tag, previous);
        save(stack, tag);
    }

    private static void decide(LivingEntity living, ItemStack stack, CompoundTag tag, GunState previous) {
        NineMillimeterGunItem gun = (NineMillimeterGunItem) stack.getItem();
        if (previous == GunState.DRAWING || previous == GunState.JAMMED) {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous == GunState.COOLDOWN) {
            if (tag.getBoolean(PRIMARY_HELD) && living instanceof Player player && rounds(tag) > 0) {
                gun.fire(player, stack, tag);
            } else {
                tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
                tag.putInt(TIMER, 0);
            }
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        reloadAction(player, tag);
        if (jamChance(tag.getFloat(WEAR), durability(stack)) > living.getRandom().nextFloat()) {
            tag.putByte(STATE, (byte) GunState.JAMMED.ordinal());
            tag.putInt(TIMER, gun.variant.jamTicks);
            playAnimation(tag, GunAnimation.JAMMED);
        } else {
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, 0);
            playAnimation(tag, GunAnimation.RELOAD_END);
        }
        tag.putBoolean(CANCEL_RELOAD, false);
        tag.putInt(MAG_AFTER, tag.getInt(MAG_COUNT));
    }

    private void pressPrimary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(PRIMARY_HELD, true);
        GunState current = state(tag);
        if (current == GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, true);
            save(stack, tag);
            return;
        }
        if (current != GunState.IDLE) {
            save(stack, tag);
            return;
        }

        if (rounds(tag) <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, variant.dryTicks);
            save(stack, tag);
            return;
        }

        fire(player, stack, tag);
        save(stack, tag);
    }

    private static void releasePrimary(ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(PRIMARY_HELD, false);
        save(stack, tag);
    }

    private void fire(Player player, ItemStack stack, CompoundTag tag) {
        int loaded = rounds(tag);
        if (loaded <= 0 || !(player.level() instanceof ServerLevel level)) return;

        NineMillimeterAmmoType ammo = NineMillimeterAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
        float durability = durability(stack);
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, durability);
        float damage = baseDamage(stack) * wearDamageMultiplier(currentWear, durability)
                * ammo.damageMultiplier();
        float spread = innateSpread(stack) + ammo.spread()
                + (tag.getBoolean(AIMING) ? 0.0F : HIP_SPREAD)
                + wearSpread(currentWear, durability);
        Vec3 origin = projectileOrigin(player, tag.getBoolean(AIMING));
        Vec3 heading = player.getLookAngle();

        level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
        SoundEvent fireSound = variant == Variant.UZI
                && WeaponModManager.hasMod(stack, 0, WeaponModManager.SILENCER)
                ? ModSounds.GUN_RIFLE_SILENCER.get() : variant.fireSound.get();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), fireSound,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, loaded - 1);
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), durability));
        tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
        tag.putInt(TIMER, fireDelay(stack));
        playAnimation(tag, GunAnimation.CYCLE);
    }

    private static void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;

        tag.putBoolean(AIMING, false);
        if (canReload(player.getInventory(), tag)) {
            tag.putInt(MAG_PREV, rounds(tag));
            tag.putByte(STATE, (byte) GunState.RELOADING.ordinal());
            tag.putInt(TIMER, ((NineMillimeterGunItem) stack.getItem()).variant.reloadTicks);
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

    private static boolean canReload(Inventory inventory, CompoundTag gun) {
        int count = rounds(gun);
        if (count >= CAPACITY) return false;
        NineMillimeterAmmoType required = count > 0
                ? NineMillimeterAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE)) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private static void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = rounds(gun);
        NineMillimeterAmmoType type = loaded > 0
                ? NineMillimeterAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());

        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get())
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private static NineMillimeterAmmoType findFirstAmmo(Inventory inventory, NineMillimeterAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get()) || candidate.isEmpty()) continue;
            if (!(StandardAmmoTypes.fromStack(candidate) instanceof NineMillimeterAmmoType type)) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.25D, -0.15625D, 1.0D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD).yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity, ItemStack stack, CompoundTag tag,
                                      GunAnimation animation, int timer, Variant variant) {
        if (animation == GunAnimation.EQUIP && timer == variant.equipSoundTick) {
            play(level, entity, ModSounds.GUN_LATCH_OPEN.get(), variant == Variant.UZI ? 1.25F : 1.0F);
        }
        if (animation == GunAnimation.CYCLE_DRY) {
            if (timer == 0) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), variant == Variant.UZI ? 1.0F : 0.8F);
            if (timer == variant.dryCockTick) {
                play(level, entity, ModSounds.GUN_PISTOL_COCK.get(), variant == Variant.UZI ? 1.0F : 0.8F);
            }
        }
        if (animation == GunAnimation.CYCLE && timer == casingTick(stack)) {
            boolean aiming = tag.getBoolean(AIMING);
            float momentumPitch = variant == Variant.GREASE_GUN
                    ? -7.5F + (float) entity.getRandom().nextGaussian() * 5.0F
                    : -2.5F + (float) entity.getRandom().nextGaussian() * 5.0F;
            float momentumYaw = variant == Variant.GREASE_GUN
                    ? 12.0F + (float) entity.getRandom().nextGaussian() * 5.0F
                    : 10.0F + entity.getRandom().nextFloat() * 15.0F;
            SpentCasingEffects.eject(entity,
                    SpentCasingPreset.forNineMillimeter(
                            NineMillimeterAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE))),
                    variant == Variant.GREASE_GUN ? 0.55D : 0.375D,
                    aiming ? 0.0D : -0.125D, aiming ? 0.0D : -0.25D,
                    0.0D, 0.18D, -0.12D, 0.01D, momentumPitch, momentumYaw);
        }
        if (animation == GunAnimation.RELOAD) {
            if (timer == variant.magRemoveTick) play(level, entity, ModSounds.GUN_MAG_REMOVE.get(), 1.0F);
            if (timer == variant.magInsertTick) play(level, entity, ModSounds.GUN_MAG_INSERT.get(), 1.0F);
            if (timer == 36) {
                play(level, entity, ModSounds.GUN_PISTOL_COCK.get(), variant == Variant.UZI ? 1.0F : 0.8F);
            }
        }
        if (animation == GunAnimation.INSPECT && variant == Variant.GREASE_GUN) {
            if (timer == 5) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.8F);
            if (timer == 26) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get(), 1.25F);
        }
        if (animation == GunAnimation.JAMMED) {
            if (timer == variant.jamCockFirst || timer == variant.jamCockSecond) {
                play(level, entity, ModSounds.GUN_PISTOL_COCK.get(), variant == Variant.UZI ? 1.0F : 0.8F);
            }
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, pitch);
    }

    public static float jamChance(float wear) {
        return jamChance(wear, DURABILITY);
    }

    private static float jamChance(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.66F ? 0.0F : Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    public static float wearDamageMultiplier(float wear) {
        return wearDamageMultiplier(wear, DURABILITY);
    }

    private static float wearDamageMultiplier(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static float wearSpread(float wear) {
        return wearSpread(wear, DURABILITY);
    }

    private static float wearSpread(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F * MAX_WEAR_SPREAD;
    }

    public static int rounds(ItemStack stack) { return rounds(data(stack)); }
    private static int rounds(CompoundTag tag) { return Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY); }
    public static float wear(ItemStack stack) {
        return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, durability(stack));
    }
    public static boolean isModernized(ItemStack stack) {
        return stack.getItem() instanceof NineMillimeterGunItem item
                && item.variant == Variant.GREASE_GUN
                && WeaponModManager.hasMod(stack, 0, WeaponModManager.GREASE_GUN_CLEAN);
    }
    public static float durability(ItemStack stack) { return isModernized(stack) ? DURABILITY * 3.0F : DURABILITY; }
    public static float baseDamage(ItemStack stack) { return isModernized(stack) ? BASE_DAMAGE + 2.0F : BASE_DAMAGE; }
    public static float innateSpread(ItemStack stack) {
        return isModernized(stack) ? 0.0F : ((NineMillimeterGunItem) stack.getItem()).variant.innateSpread;
    }
    public static int fireDelay(ItemStack stack) {
        int delay = ((NineMillimeterGunItem) stack.getItem()).variant.fireDelay;
        return isModernized(stack) ? delay / 2 : delay;
    }
    public static int casingTick(ItemStack stack) {
        if (isModernized(stack)) return 1;
        return ((NineMillimeterGunItem) stack.getItem()).variant == Variant.GREASE_GUN ? 2 : 1;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return isModernized(stack) ? "item.hbm.gun_greasegun_m3" : super.getDescriptionId(stack);
    }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static boolean primaryHeld(ItemStack stack) { return data(stack).getBoolean(PRIMARY_HELD); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static NineMillimeterAmmoType loadedAmmo(ItemStack stack) {
        return NineMillimeterAmmoType.fromLegacyBulletConfig(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    NineMillimeterAmmoType ammo, float wear, boolean primaryHeld) {
        CompoundTag tag = data(stack);
        tag.putByte(STATE, (byte) state.ordinal());
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, durability(stack)));
        tag.putBoolean(PRIMARY_HELD, primaryHeld);
        save(stack, tag);
    }

    private static GunState state(CompoundTag tag) {
        int ordinal = tag.getByte(STATE);
        GunState[] values = GunState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunState.DRAWING;
    }

    private static GunAnimation animation(CompoundTag tag) {
        int ordinal = tag.getInt(LAST_ANIM);
        GunAnimation[] values = GunAnimation.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunAnimation.RELOAD;
    }

    private static void playAnimation(CompoundTag tag, GunAnimation animation) {
        tag.putInt(LAST_ANIM, animation.ordinal());
        tag.putInt(ANIM_TIMER, 0);
    }

    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(INITIALIZED)) {
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(MAG_COUNT, CAPACITY);
            tag.putInt(MAG_TYPE, NineMillimeterAmmoType.SOFT_POINT.legacyBulletConfig());
        }
        return tag;
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NineMillimeterAmmoType ammo = loadedAmmo(stack);
        float gunDamage = baseDamage(stack);
        float damage = gunDamage * ammo.damageMultiplier();
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trimDamage(gunDamage))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo").append(": " + trimDamage(damage))
                .withStyle(ChatFormatting.GRAY));
        float durability = durability(stack);
        int condition = Mth.clamp((int) ((durability - wear(stack)) * 100.0F / durability), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        for (ItemStack mod : WeaponModManager.installedMods(stack, 0)) {
            tooltip.add(mod.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable("gui.weapon.quality.aside").withStyle(ChatFormatting.YELLOW));
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) return Integer.toString(Math.round(damage));
        return String.format(Locale.ROOT, "%.3f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum Variant {
        GREASE_GUN(20, 4, 40, 60, 55, 0.015F, 2.0F, 0.5F,
                5, 11, 2, 24, 11, 26, ModSounds.GUN_GREASEGUN_FIRE),
        UZI(15, 2, 25, 55, 50, 0.005F, 1.0F, 0.25F,
                8, 8, 4, 26, 17, 31, ModSounds.GUN_UZI_FIRE);

        private final int drawTicks;
        private final int fireDelay;
        private final int dryTicks;
        private final int reloadTicks;
        private final int jamTicks;
        private final float innateSpread;
        private final float recoilVertical;
        private final float recoilHorizontalSigma;
        private final int equipSoundTick;
        private final int dryCockTick;
        private final int magRemoveTick;
        private final int magInsertTick;
        private final int jamCockFirst;
        private final int jamCockSecond;
        private final net.neoforged.neoforge.registries.DeferredHolder<SoundEvent, SoundEvent> fireSound;

        Variant(int drawTicks, int fireDelay, int dryTicks, int reloadTicks, int jamTicks,
                float innateSpread, float recoilVertical, float recoilHorizontalSigma,
                int equipSoundTick, int dryCockTick, int magRemoveTick, int magInsertTick,
                int jamCockFirst, int jamCockSecond,
                net.neoforged.neoforge.registries.DeferredHolder<SoundEvent, SoundEvent> fireSound) {
            this.drawTicks = drawTicks;
            this.fireDelay = fireDelay;
            this.dryTicks = dryTicks;
            this.reloadTicks = reloadTicks;
            this.jamTicks = jamTicks;
            this.innateSpread = innateSpread;
            this.recoilVertical = recoilVertical;
            this.recoilHorizontalSigma = recoilHorizontalSigma;
            this.equipSoundTick = equipSoundTick;
            this.dryCockTick = dryCockTick;
            this.magRemoveTick = magRemoveTick;
            this.magInsertTick = magInsertTick;
            this.jamCockFirst = jamCockFirst;
            this.jamCockSecond = jamCockSecond;
            this.fireSound = fireSound;
        }

        public int drawTicks() { return drawTicks; }
        public int fireDelay() { return fireDelay; }
        public int dryTicks() { return dryTicks; }
        public int reloadTicks() { return reloadTicks; }
        public int jamTicks() { return jamTicks; }
        public float innateSpread() { return innateSpread; }
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    /** Enum order is animation protocol. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
