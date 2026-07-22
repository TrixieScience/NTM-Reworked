package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.Magnum44AmmoType;
import com.hbm.ntm.weapon.Equestrian44AmmoType;
import com.hbm.ntm.weapon.SecretAmmoTypes;
import com.hbm.ntm.weapon.SednaAmmoType;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.hbm.ntm.HbmNtm;
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

/** Little Macintosh and Protege share a cylinder, but disagree violently about delivery. */
public final class LegendaryHeavyRevolverItem extends SednaGunItem {
    public static final int DURABILITY = 31_000;
    public static final int DRAW_TICKS = 10;
    public static final int FIRE_DELAY = 14;
    public static final int RELOAD_TICKS = 46;
    public static final int JAM_TICKS = 23;
    public static final int CAPACITY = 6;
    public static final float BASE_DAMAGE = 30.0F;
    public static final float HIP_SPREAD = 0.025F;
    public static final float MAX_WEAR_SPREAD = 0.125F;

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

    public LegendaryHeavyRevolverItem(Variant variant) {
        this.variant = variant;
    }

    public Variant variant() { return variant; }

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CLASSIC; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        SednaAmmoType ammo = loadedAmmo(stack);
        return ammo.createStack(ammo instanceof Equestrian44AmmoType
                ? ModItems.AMMO_SECRET.get() : ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public float recoilVertical() { return 10.0F; }
    @Override public float recoilHorizontalSigma() { return 1.5F; }
    @Override public float gunAimFovMultiplier() { return variant == Variant.LITTLE_MACINTOSH ? 0.34F : 0.67F; }
    @Override public ResourceLocation gunScopeTexture() {
        return variant == Variant.LITTLE_MACINTOSH
                ? ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "textures/misc/scope_44.png") : null;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;

        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);
        GunState previous = state(tag);

        if (!held) {
            if (previous != GunState.JAMMED) {
                tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
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
        if (previous == GunState.DRAWING || previous == GunState.COOLDOWN || previous == GunState.JAMMED) {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        reloadAction(player, tag);
        if (jamChance(tag.getFloat(WEAR)) > living.getRandom().nextFloat()) {
            tag.putByte(STATE, (byte) GunState.JAMMED.ordinal());
            tag.putInt(TIMER, JAM_TICKS);
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
        GunState current = state(tag);
        if (current == GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, true);
            save(stack, tag);
            return;
        }
        if (current != GunState.IDLE) return;

        int loaded = Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY);
        if (loaded <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, FIRE_DELAY);
            save(stack, tag);
            return;
        }

        SednaAmmoType ammo = loadedAmmo(tag.getInt(MAG_TYPE), variant);
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear) * ammo.damageMultiplier();
        float spread = ammo.spread() + (tag.getBoolean(AIMING) ? 0.0F : HIP_SPREAD) + wearSpread(currentWear);
        Vec3 origin = projectileOrigin(player, tag.getBoolean(AIMING));
        Vec3 heading = player.getLookAngle();
        if (!(player.level() instanceof ServerLevel level)) return;

        BulletEntity bullet = new BulletEntity(level, player, ammo, damage, spread, origin, heading);
        level.addFreshEntity(bullet);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUN_HEAVY_REVOLVER_FIRE.get(), SoundSource.PLAYERS, 1.0F, variant.firePitch);
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, ammo.blackPowder()));
        }

        tag.putInt(MAG_COUNT, loaded - 1);
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), DURABILITY));
        tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
        tag.putInt(TIMER, FIRE_DELAY);
        playAnimation(tag, GunAnimation.CYCLE);
        save(stack, tag);
    }

    private void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;

        tag.putBoolean(AIMING, false);
        if (canReload(player.getInventory(), tag)) {
            tag.putInt(MAG_PREV, Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY));
            tag.putByte(STATE, (byte) GunState.RELOADING.ordinal());
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
        int count = Mth.clamp(gun.getInt(MAG_COUNT), 0, CAPACITY);
        if (count >= CAPACITY) return false;
        SednaAmmoType required = count > 0 ? loadedAmmo(gun.getInt(MAG_TYPE), variant) : null;
        return findFirstAmmo(inventory, required, variant) != null;
    }

    private void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = Mth.clamp(gun.getInt(MAG_COUNT), 0, CAPACITY);
        SednaAmmoType type = loaded > 0 ? loadedAmmo(gun.getInt(MAG_TYPE), variant)
                : findFirstAmmo(inventory, null, variant);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());

        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || ammoFromStack(candidate, variant) != type) continue;
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private static SednaAmmoType findFirstAmmo(Inventory inventory, SednaAmmoType required, Variant variant) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty()) continue;
            SednaAmmoType type = ammoFromStack(candidate, variant);
            if (type == null) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static SednaAmmoType ammoFromStack(ItemStack stack, Variant variant) {
        if (stack.is(ModItems.AMMO_STANDARD.get())) {
            SednaAmmoType type = StandardAmmoTypes.fromStack(stack);
            return type instanceof Magnum44AmmoType ? type : null;
        }
        if (stack.is(ModItems.AMMO_SECRET.get())
                && SecretAmmoTypes.fromStack(stack) instanceof Equestrian44AmmoType) {
            return variant.specialAmmo;
        }
        return null;
    }

    private static SednaAmmoType loadedAmmo(int config, Variant variant) {
        return config == 102 || config == 103 ? variant.specialAmmo
                : Magnum44AmmoType.fromLegacyBulletConfig(config);
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.3125D, -0.0625D, 0.75D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD).yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity, GunAnimation animation, int timer) {
        switch (animation) {
            case RELOAD -> {
                if (timer == 3) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get());
                if (timer == 10) play(level, entity, ModSounds.GUN_MAG_SMALL_REMOVE.get());
                if (timer == 34) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get());
                if (timer == 40) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get());
            }
            case CYCLE -> {
                if (timer == 11) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get());
            }
            case CYCLE_DRY -> {
                if (timer == 2) play(level, entity, ModSounds.GUN_DRY_FIRE.get());
                if (timer == 11) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get());
            }
            case INSPECT -> {
                if (timer == 3) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get());
                if (timer == 16) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get());
            }
            default -> { }
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static float jamChance(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.66F ? 0.0F : Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    public static float wearDamageMultiplier(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static float wearSpread(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F * MAX_WEAR_SPREAD;
    }

    public static int rounds(ItemStack stack) { return Mth.clamp(data(stack).getInt(MAG_COUNT), 0, CAPACITY); }
    public static float wear(ItemStack stack) { return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, DURABILITY); }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public SednaAmmoType loadedAmmo(ItemStack stack) {
        return loadedAmmo(data(stack).getInt(MAG_TYPE), variant);
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    SednaAmmoType ammo, float wear) {
        CompoundTag tag = data(stack);
        tag.putByte(STATE, (byte) state.ordinal());
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, DURABILITY));
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
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        SednaAmmoType ammo = loadedAmmo(stack);
        float damage = BASE_DAMAGE * ammo.damageMultiplier();
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm."
                        + (ammo instanceof Equestrian44AmmoType ? "ammo_secret." : "ammo_standard.")
                        + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trimDamage(BASE_DAMAGE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo").append(": " + trimDamage(damage))
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.quality.legendary").withStyle(ChatFormatting.YELLOW));
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) return Integer.toString(Math.round(damage));
        return String.format(Locale.ROOT, "%.3f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    public enum Variant {
        LITTLE_MACINTOSH(1.0F, Equestrian44AmmoType.BOXCAR),
        PROTEGE(0.8F, Equestrian44AmmoType.TORPEDO);

        private final float firePitch;
        private final Equestrian44AmmoType specialAmmo;

        Variant(float firePitch, Equestrian44AmmoType specialAmmo) {
            this.firePitch = firePitch;
            this.specialAmmo = specialAmmo;
        }
    }

    /** Enum order is animation protocol. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
