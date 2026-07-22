package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.Shotgun10GaugeAmmoType;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.SpentCasingEffects;
import com.hbm.ntm.weapon.SpentCasingPreset;
import com.hbm.ntm.weapon.WeaponModManager;
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

public final class DoubleBarrelItem extends SednaGunItem {
    public static final int DURABILITY = 1_000;
    public static final int CAPACITY = 2;
    public static final int DRAW_TICKS = 10;
    public static final int INSPECT_TICKS = 39;
    public static final int FIRE_DELAY = 10;
    public static final int RELOAD_TICKS = 41;
    public static final float BASE_DAMAGE = 30.0F;
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
    private static final String CANCEL_RELOAD = "cancel";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY, SECONDARY -> pressFire(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
            default -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CIRCLE; }
    @Override public float gunAimFovMultiplier() { return 0.67F; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return loadedAmmo(stack).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public float recoilVertical() { return 10.0F; }
    @Override public float recoilHorizontalSigma() { return 1.5F; }

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
        playOrchestra(level, living, tag, animation(tag), animationTimer);
        tag.putInt(ANIM_TIMER, animationTimer + 1);

        int timer = tag.getInt(TIMER);
        if (timer > 0) tag.putInt(TIMER, timer - 1);
        if (timer <= 1) decide(living, tag, previous);
        save(stack, tag);
    }

    private static void decide(LivingEntity living, CompoundTag tag, GunState previous) {
        if (previous == GunState.DRAWING || previous == GunState.JAMMED) {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous == GunState.COOLDOWN) {
            if (rounds(tag) <= 0 && living instanceof Player player && canReload(player.getInventory(), tag)) {
                startReload(tag);
            } else {
                setState(tag, GunState.IDLE);
                tag.putInt(TIMER, 0);
            }
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        reloadAction(player, tag);
        tag.putBoolean(CANCEL_RELOAD, false);
        tag.putInt(MAG_AFTER, rounds(tag));
        if (jamChance(tag.getFloat(WEAR)) > living.getRandom().nextFloat()) {
            setState(tag, GunState.JAMMED);
            tag.putInt(TIMER, 0);
            playAnimation(tag, GunAnimation.JAMMED);
        } else {
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, 0);
            playAnimation(tag, GunAnimation.RELOAD_END);
        }
    }

    private static void pressFire(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        GunState current = state(tag);
        if (current == GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, true);
            save(stack, tag);
            return;
        }
        if (current != GunState.IDLE) return;
        if (rounds(tag) <= 0) {
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, FIRE_DELAY);
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            save(stack, tag);
            return;
        }
        fire(player, stack, tag);
        save(stack, tag);
    }

    private static void fire(Player player, ItemStack stack, CompoundTag tag) {
        if (rounds(tag) <= 0 || !(player.level() instanceof ServerLevel level)) return;
        Shotgun10GaugeAmmoType ammo = Shotgun10GaugeAmmoType.fromLegacyMetadata(tag.getInt(MAG_TYPE));
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        float damage = baseDamage(stack) * wearDamageMultiplier(currentWear) * ammo.damageMultiplier();
        boolean aiming = tag.getBoolean(AIMING);
        float spread = innateSpread(stack) + ammo.spread() * ammoSpreadMultiplier(stack)
                + (aiming ? 0.0F : HIP_SPREAD) + wearSpread(currentWear);
        Vec3 origin = projectileOrigin(player, aiming);
        Vec3 heading = player.getLookAngle();
        for (int projectile = 0; projectile < ammo.projectiles(); projectile++) {
            level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUN_SHOTGUN_FIRE.get(), SoundSource.PLAYERS, 1.0F, 0.9F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, Math.max(0, rounds(tag) - 1));
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), DURABILITY));
        setState(tag, GunState.COOLDOWN);
        tag.putInt(TIMER, FIRE_DELAY);
        playAnimation(tag, GunAnimation.CYCLE);
    }

    private static void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;
        tag.putBoolean(AIMING, false);
        if (canReload(player.getInventory(), tag)) {
            startReload(tag);
        } else {
            playAnimation(tag, GunAnimation.INSPECT);
        }
        save(stack, tag);
    }

    private static void startReload(CompoundTag tag) {
        tag.putBoolean(AIMING, false);
        tag.putInt(MAG_PREV, rounds(tag));
        setState(tag, GunState.RELOADING);
        tag.putInt(TIMER, RELOAD_TICKS);
        playAnimation(tag, GunAnimation.RELOAD);
    }

    private static void toggleAim(ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(AIMING, !tag.getBoolean(AIMING));
        save(stack, tag);
    }

    private static boolean canReload(Inventory inventory, CompoundTag gun) {
        int loaded = rounds(gun);
        if (loaded >= CAPACITY) return false;
        Shotgun10GaugeAmmoType required = loaded > 0
                ? Shotgun10GaugeAmmoType.fromLegacyMetadata(gun.getInt(MAG_TYPE)) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private static void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = rounds(gun);
        Shotgun10GaugeAmmoType type = loaded > 0
                ? Shotgun10GaugeAmmoType.fromLegacyMetadata(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyMetadata());
        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private static Shotgun10GaugeAmmoType findFirstAmmo(Inventory inventory,
                                                         Shotgun10GaugeAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (!(StandardAmmoTypes.fromStack(candidate) instanceof Shotgun10GaugeAmmoType type)) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.1875D, -0.0625D, 0.75D);
        return player.getEyePosition().add(local
                .xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD));
    }

    private static void playOrchestra(Level level, LivingEntity entity, CompoundTag tag,
                                      GunAnimation animation, int timer) {
        if (animation == GunAnimation.RELOAD) {
            if (timer == 5) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.75F);
            if (timer == 19) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get(), 0.9F);
            if (timer == 29) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.8F);
            if (timer == 12) {
                int count = CAPACITY - Mth.clamp(tag.getInt(MAG_PREV), 0, CAPACITY);
                for (int index = 0; index < count; index++) {
                    SpentCasingEffects.eject(entity,
                            SpentCasingPreset.forTenGauge(
                                    Shotgun10GaugeAmmoType.fromLegacyMetadata(tag.getInt(MAG_TYPE))),
                            0.0D, -0.1875D, -0.375D, -0.24D, 0.18D, 0.0D, 0.01D,
                            -20.0F + (float) entity.getRandom().nextGaussian() * 5.0F,
                            (float) entity.getRandom().nextGaussian() * 2.5F);
                }
            }
        } else if (animation == GunAnimation.INSPECT) {
            if (timer == 5) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.75F);
            if (timer == 19) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.8F);
        } else if (animation == GunAnimation.CYCLE_DRY && timer == 2) {
            play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 1.0F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, pitch);
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

    public static boolean isSawedOff(ItemStack stack) {
        return WeaponModManager.hasMod(stack, 0, WeaponModManager.SAWED_OFF);
    }

    public static float baseDamage(ItemStack stack) { return isSawedOff(stack) ? BASE_DAMAGE * 1.35F : BASE_DAMAGE; }
    public static float innateSpread(ItemStack stack) { return isSawedOff(stack) ? 0.025F : 0.0F; }
    public static float ammoSpreadMultiplier(ItemStack stack) { return isSawedOff(stack) ? 1.5F : 1.0F; }

    public static int rounds(ItemStack stack) { return rounds(data(stack)); }
    private static int rounds(CompoundTag tag) {
        return Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY);
    }
    public static float wear(ItemStack stack) {
        return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, DURABILITY);
    }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static int amountAfterReload(ItemStack stack) { return data(stack).getInt(MAG_AFTER); }
    public static Shotgun10GaugeAmmoType loadedAmmo(ItemStack stack) {
        return Shotgun10GaugeAmmoType.fromLegacyMetadata(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    Shotgun10GaugeAmmoType ammo, float wear, boolean aiming,
                                    GunAnimation animation) {
        CompoundTag tag = data(stack);
        setState(tag, state);
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyMetadata());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, DURABILITY));
        tag.putBoolean(AIMING, aiming);
        tag.putBoolean(EQUIPPED, true);
        playAnimation(tag, animation);
        save(stack, tag);
    }

    private static GunState state(CompoundTag tag) {
        int ordinal = tag.getByte(STATE);
        return ordinal >= 0 && ordinal < GunState.values().length
                ? GunState.values()[ordinal] : GunState.DRAWING;
    }

    private static void setState(CompoundTag tag, GunState state) {
        tag.putByte(STATE, (byte) state.ordinal());
    }

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
            tag.putInt(MAG_TYPE, Shotgun10GaugeAmmoType.BUCKSHOT.legacyMetadata());
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
        Shotgun10GaugeAmmoType ammo = loadedAmmo(stack);
        float gunDamage = baseDamage(stack);
        float pelletDamage = gunDamage * ammo.damageMultiplier();
        float totalDamage = pelletDamage * ammo.projectiles();
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage")
                .append(": " + trimDamage(gunDamage)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                .append(": " + trimDamage(totalDamage) + " (" + ammo.projectiles()
                        + " x " + trimDamage(pelletDamage) + ")")
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.quality.special")
                .withStyle(ChatFormatting.YELLOW));
        for (ItemStack mod : WeaponModManager.installedMods(stack, 0)) {
            tooltip.add(mod.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        }
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) {
            return Integer.toString(Math.round(damage));
        }
        return String.format(Locale.ROOT, "%.3f", damage)
                .replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }
    public enum GunAnimation { EQUIP, CYCLE, CYCLE_DRY, RELOAD, RELOAD_END, JAMMED, INSPECT }
}
