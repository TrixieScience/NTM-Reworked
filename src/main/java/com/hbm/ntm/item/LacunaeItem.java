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

/** A two-hundred-shot laser minigun powered by five very nervous capacitors. */
public final class LacunaeItem extends SednaGunItem {
    public static final int DURABILITY = 50_000;
    public static final int CAPACITY = 200;
    public static final int DRAW_TICKS = 20;
    public static final int FIRE_DELAY = 1;
    public static final int DRY_TICKS = 15;
    public static final int RELOAD_TICKS = 15;
    public static final int JAM_TICKS = 0;
    public static final float BASE_DAMAGE = 12.0F;
    public static final float INNATE_SPREAD = 0.01F;
    public static final float HIP_SPREAD = 0.025F;
    public static final float MAX_WEAR_SPREAD = 0.125F;
    public static final float AIM_FOV_MULTIPLIER = 0.67F;

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

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case PRIMARY_RELEASE -> releasePrimary(stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
            default -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public boolean gunAutomatic() { return true; }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CIRCLE; }
    @Override public float gunAimFovMultiplier() { return AIM_FOV_MULTIPLIER; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
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
            tag.putBoolean(PRIMARY_HELD, false);
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
        if (previous == GunState.COOLDOWN && tag.getBoolean(PRIMARY_HELD)
                && roundsValue(tag) > 0 && living instanceof Player player) {
            fire(player, tag);
            return;
        }
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
        if (JAM_TICKS > 0 && jamChance(tag.getFloat(WEAR)) > living.getRandom().nextFloat()) {
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
        tag.putBoolean(PRIMARY_HELD, true);
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
            tag.putInt(TIMER, DRY_TICKS);
            save(stack, tag);
            return;
        }
        fire(player, tag);
        save(stack, tag);
    }

    private static void releasePrimary(ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(PRIMARY_HELD, false);
        save(stack, tag);
    }

    private void fire(Player player, CompoundTag tag) {
        if (!(player.level() instanceof ServerLevel level)) return;
        EnergyAmmoType ammo = EnergyAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear) * ammo.damageMultiplier();
        float spread = INNATE_SPREAD + (tag.getBoolean(AIMING) ? 0.0F : HIP_SPREAD)
                + wearSpread(currentWear);
        Vec3 origin = projectileOrigin(player);
        Vec3 heading = player.getLookAngle();
        LaserPistolBeamEntity beam = new LaserPistolBeamEntity(level, player, ammo, damage,
                spread, new Vec3(-0.25D, -0.15625D, 1.0D));
        beam.performHitscan();
        level.addFreshEntity(beam);
        play(level, player, ModSounds.GUN_LASER_GATLING_FIRE.get(), 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, Math.max(0, roundsValue(tag) - 1));
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), DURABILITY));
        setState(tag, GunState.COOLDOWN);
        tag.putInt(TIMER, FIRE_DELAY);
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

    private static boolean canReload(Inventory inventory, CompoundTag gun) {
        int count = roundsValue(gun);
        if (count >= CAPACITY) return false;
        EnergyAmmoType required = count > 0
                ? EnergyAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE)) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private static void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = roundsValue(gun);
        EnergyAmmoType type = loaded > 0
                ? EnergyAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());
        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get())
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            int consumed = Math.min((CAPACITY - loaded + 39) / 40, candidate.getCount());
            candidate.shrink(consumed);
            loaded = Math.min(CAPACITY, loaded + consumed * 40);
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
        Vec3 local = new Vec3(-0.25D, -0.15625D, 1.0D);
        return player.getEyePosition().add(local.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD));
    }

    private static void playOrchestra(Level level, LivingEntity entity,
                                      GunAnimation animation, int timer) {
        if (animation == GunAnimation.CYCLE && timer == 1) {
            play(level, entity, ModSounds.GUN_REVOLVER_SPIN.get(), 1.0F, 0.75F);
        } else if (animation == GunAnimation.CYCLE_DRY) {
            if (timer == 0) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 1.0F, 0.75F);
            if (timer == 1) play(level, entity, ModSounds.GUN_REVOLVER_SPIN.get(), 1.0F, 0.75F);
        } else if ((animation == GunAnimation.RELOAD || animation == GunAnimation.INSPECT)
                && timer == 0) {
            play(level, entity, ModSounds.GUN_REVOLVER_SPIN.get(), 1.0F, 0.75F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound,
                             float volume, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, volume, pitch);
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

    public static int rounds(ItemStack stack) { return roundsValue(data(stack)); }
    private static int roundsValue(CompoundTag tag) {
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
    public static EnergyAmmoType loadedAmmo(ItemStack stack) {
        return EnergyAmmoType.fromLegacyBulletConfig(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    EnergyAmmoType ammo, float wear) {
        CompoundTag tag = data(stack);
        setState(tag, state);
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, DURABILITY));
        tag.putBoolean(EQUIPPED, true);
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
            tag.putInt(MAG_TYPE, EnergyAmmoType.STANDARD.legacyBulletConfig());
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
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trim(BASE_DAMAGE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                .append(": " + trim(BASE_DAMAGE * ammo.damageMultiplier())).withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.quality.legendary").withStyle(ChatFormatting.GOLD));
    }

    private static String trim(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) return Integer.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    public enum GunAnimation { CYCLE, CYCLE_DRY, EQUIP, RELOAD, JAMMED, INSPECT }
}
