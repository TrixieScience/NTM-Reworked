package com.hbm.ntm.item;

import com.hbm.ntm.entity.TauBeamEntity;
import com.hbm.ntm.radiation.ModDamageTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModParticles;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.TauAmmoType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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

import java.util.List;
import java.util.Locale;

public final class TauGunItem extends SednaGunItem {
    public static final int DURABILITY = 6_400;
    public static final int DRAW_TICKS = 10;
    public static final int INSPECT_TICKS = 10;
    public static final int FIRE_DELAY = 4;
    public static final int MAX_CHARGE_UNITS = 13;
    public static final int LAST_CHARGE_AMMO_TICK = 120;
    public static final int OVERCHARGE_TICK = 201;
    public static final float BASE_DAMAGE = 25.0F;
    public static final float CHARGE_DAMAGE_MULTIPLIER = 5.0F;
    public static final float MAX_WEAR_SPREAD = 0.125F;

    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_0";
    private static final String TIMER = "timer_0";
    private static final String WEAR = "wear_0";
    private static final String MAG_TYPE = "magtype0";
    private static final String MAG_COUNT = "magcount0";
    private static final String AIMING = "aiming";
    private static final String PRIMARY_HELD = "primary0";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case PRIMARY_RELEASE -> releasePrimary(player, stack);
            case SECONDARY -> pressSecondary(player, stack);
            case SECONDARY_RELEASE -> releaseSecondary(player, stack);
            case RELOAD -> inspect(stack);
            case TOGGLE_AIM -> toggleAim(stack);
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public boolean gunAutomatic() { return true; }
    @Override public boolean gunSecondaryAutomatic() { return true; }
    @Override public boolean gunBeltFed() { return true; }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    @Override public int gunRounds(ItemStack stack) { return beltCount(stack); }
    @Override public int gunCapacity() { return 0; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return TauAmmoType.DEPLETED_URANIUM.createStack(ModItems.AMMO_STANDARD.get(), 1);
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
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, DRAW_TICKS);
            tag.putInt(LAST_ANIM, GunAnimation.CYCLE.ordinal());
            tag.putBoolean(AIMING, false);
            tag.putBoolean(PRIMARY_HELD, false);
            tag.putBoolean(EQUIPPED, false);
            save(stack, tag);
            return;
        }

        if (living instanceof Player player) refreshBelt(player, tag);
        if (!tag.getBoolean(EQUIPPED)) playAnimation(tag, GunAnimation.EQUIP);
        tag.putBoolean(EQUIPPED, true);

        int animationTimer = tag.getInt(ANIM_TIMER);
        GunAnimation currentAnimation = animation(tag);
        playOrchestra(level, living, tag, currentAnimation, animationTimer);
        if (animation(tag) == currentAnimation) tag.putInt(ANIM_TIMER, animationTimer + 1);

        int timer = tag.getInt(TIMER);
        if (timer > 0) tag.putInt(TIMER, timer - 1);
        if (timer <= 1) decide(living, tag, previous);
        save(stack, tag);
    }

    private void pressPrimary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(PRIMARY_HELD, true);
        if (state(tag) != GunState.IDLE) {
            save(stack, tag);
            return;
        }
        if (beltAmount(player) > 0) {
            fire(player, tag, false, 1);
        } else {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, FIRE_DELAY);
        }
        save(stack, tag);
    }

    private static void releasePrimary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(PRIMARY_HELD, false);
        if (animation(tag) == GunAnimation.CYCLE) {
            play(player.level(), player, ModSounds.GUN_TAU_RELEASE.get(), 1.0F, 1.0F);
        }
        save(stack, tag);
    }

    private static void pressSecondary(Player player, ItemStack stack) {
        if (beltAmount(player) <= 0) return;
        CompoundTag tag = data(stack);
        playAnimation(tag, GunAnimation.SPINUP);
        tag.putInt(MAG_TYPE, TauAmmoType.DEPLETED_URANIUM.legacyBulletConfig());
        save(stack, tag);
    }

    private void releaseSecondary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        int chargeTicks = tag.getInt(ANIM_TIMER);
        if (chargeTicks >= 10 && animation(tag) == GunAnimation.SPINUP) {
            int unitsUsed = chargeUnits(chargeTicks);
            playAnimation(tag, GunAnimation.ALT_CYCLE);
            fire(player, tag, true, unitsUsed);
        } else {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
        }
        save(stack, tag);
    }

    private void fire(Player player, CompoundTag tag, boolean charged, int unitsUsed) {
        if (!(player.level() instanceof ServerLevel level)) return;
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear)
                * (charged ? unitsUsed * CHARGE_DAMAGE_MULTIPLIER : 1.0F);
        float spread = wearSpread(currentWear);
        TauBeamEntity beam = new TauBeamEntity(level, player, damage, spread,
                new Vec3(-0.25D, -0.15625D, 1.0D), charged);
        beam.performHitscan();
        level.addFreshEntity(beam);

        if (!charged) consumeBelt(player);
        tag.putInt(MAG_COUNT, beltAmount(player));
        tag.putFloat(WEAR, Math.min(currentWear + unitsUsed, DURABILITY));
        if (!charged) {
            setState(tag, GunState.COOLDOWN);
            tag.putInt(TIMER, FIRE_DELAY);
            playAnimation(tag, GunAnimation.CYCLE);
        }
    }

    private void decide(LivingEntity living, CompoundTag tag, GunState previous) {
        if (previous == GunState.DRAWING) {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.COOLDOWN) return;
        if (tag.getBoolean(PRIMARY_HELD) && living instanceof Player player && beltAmount(player) > 0) {
            fire(player, tag, false, 1);
        } else {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
        }
    }

    private static void inspect(ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) == GunState.IDLE) {
            tag.putBoolean(AIMING, false);
            playAnimation(tag, GunAnimation.INSPECT);
        }
        save(stack, tag);
    }

    private static void toggleAim(ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(AIMING, !tag.getBoolean(AIMING));
        save(stack, tag);
    }

    private static void playOrchestra(Level level, LivingEntity living, CompoundTag tag,
                                      GunAnimation animation, int timer) {
        if (animation == GunAnimation.CYCLE && timer == 0) {
            play(level, living, ModSounds.GUN_TAU_FIRE.get(), 0.5F,
                    0.9F + living.getRandom().nextFloat() * 0.2F);
            return;
        }
        if (animation == GunAnimation.ALT_CYCLE && timer == 0) {
            play(level, living, ModSounds.GUN_TAU_FIRE.get(), 0.5F,
                    0.7F + living.getRandom().nextFloat() * 0.2F);
            return;
        }
        if (animation != GunAnimation.SPINUP || !(living instanceof Player player)) return;

        if (timer % 10 == 0 && timer < 130) {
            if (beltAmount(player) <= 0) {
                playAnimation(tag, GunAnimation.CYCLE_DRY);
                return;
            }
            consumeBelt(player);
            tag.putInt(MAG_COUNT, beltAmount(player));
        }

        if (timer > 200) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            living.hurt(level.damageSources().source(ModDamageTypes.TAU_BLAST), 1_000.0F);
            tag.putFloat(WEAR, DURABILITY);
            play(level, living, ModSounds.TESLA_BLAST.get(), 5.0F, 0.9F);
            play(level, living, SoundEvents.FIREWORK_ROCKET_BLAST, 5.0F, 0.5F);
            if (level instanceof ServerLevel server) {
                double y = living.getEyeY();
                server.sendParticles(ModParticles.TAU_HADRON.get(), living.getX(), y, living.getZ(),
                        3, 0.08D, 0.08D, 0.08D, 0.0D);
                server.sendParticles(ModParticles.TAU_SPARK.get(), living.getX(), y, living.getZ(),
                        24, 0.5D, 0.5D, 0.5D, 0.12D);
            }
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, volume, pitch);
    }

    private static boolean isTauAmmo(ItemStack stack) {
        return stack.is(ModItems.AMMO_STANDARD.get())
                && StandardAmmoTypes.fromStack(stack) == TauAmmoType.DEPLETED_URANIUM;
    }

    private static int beltAmount(Player player) {
        int amount = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (isTauAmmo(candidate)) amount += candidate.getCount();
        }
        return amount;
    }

    private static void consumeBelt(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (isTauAmmo(candidate)) {
                candidate.shrink(1);
                return;
            }
        }
    }

    private static void refreshBelt(Player player, CompoundTag tag) {
        tag.putInt(MAG_TYPE, TauAmmoType.DEPLETED_URANIUM.legacyBulletConfig());
        tag.putInt(MAG_COUNT, beltAmount(player));
    }

    public static int chargeUnits(int animationTimer) {
        return 1 + Math.min(12, Math.max(0, animationTimer) / 10);
    }

    public static float wearDamageMultiplier(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static float wearSpread(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F * MAX_WEAR_SPREAD;
    }

    public static int beltCount(ItemStack stack) { return Math.max(0, data(stack).getInt(MAG_COUNT)); }
    public static float wear(ItemStack stack) { return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, DURABILITY); }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static boolean primaryHeld(ItemStack stack) { return data(stack).getBoolean(PRIMARY_HELD); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }

    public static void setTestState(ItemStack stack, GunState state, int timer, float wear,
                                    GunAnimation animation, int animationTimer) {
        CompoundTag tag = data(stack);
        setState(tag, state);
        tag.putInt(TIMER, timer);
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, DURABILITY));
        tag.putInt(MAG_TYPE, TauAmmoType.DEPLETED_URANIUM.legacyBulletConfig());
        tag.putBoolean(EQUIPPED, true);
        tag.putInt(LAST_ANIM, animation.ordinal());
        tag.putInt(ANIM_TIMER, animationTimer);
        save(stack, tag);
    }

    private static GunState state(CompoundTag tag) {
        int ordinal = tag.getByte(STATE);
        GunState[] values = GunState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunState.DRAWING;
    }

    private static void setState(CompoundTag tag, GunState state) {
        tag.putByte(STATE, (byte) state.ordinal());
    }

    private static GunAnimation animation(CompoundTag tag) {
        int ordinal = tag.getInt(LAST_ANIM);
        GunAnimation[] values = GunAnimation.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunAnimation.CYCLE;
    }

    private static void playAnimation(CompoundTag tag, GunAnimation animation) {
        tag.putInt(LAST_ANIM, animation.ordinal());
        tag.putInt(ANIM_TIMER, 0);
    }

    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(INITIALIZED)) {
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(MAG_TYPE, TauAmmoType.DEPLETED_URANIUM.legacyBulletConfig());
        }
        return tag;
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard.tau_uranium"))
                .append(" x" + beltCount(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trim(BASE_DAMAGE))
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.quality.aside").withStyle(ChatFormatting.YELLOW));
    }

    private static String trim(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) return Integer.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN }

    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
