package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.CoilAmmoType;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public final class CoilgunItem extends SednaGunItem {
    public static final int DURABILITY = 400;
    public static final int DRAW_TICKS = 5;
    public static final int INSPECT_TICKS = 39;
    public static final int FIRE_DELAY = 5;
    public static final int RELOAD_TICKS = 20;
    public static final int JAM_TICKS = 33;
    public static final int CAPACITY = 1;
    public static final float BASE_DAMAGE = 35.0F;
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
            case PRIMARY -> pressPrimary(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
            default -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CIRCUMFLEX; }
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
        playOrchestra(level, living, animation(tag), animationTimer);
        tag.putInt(ANIM_TIMER, animationTimer + 1);

        int timer = tag.getInt(TIMER);
        if (timer > 0) tag.putInt(TIMER, timer - 1);
        if (timer <= 1) decide(living, tag, previous);
        save(stack, tag);
    }

    private static void decide(LivingEntity living, CompoundTag tag, GunState previous) {
        if (previous == GunState.DRAWING || previous == GunState.COOLDOWN
                || previous == GunState.JAMMED) {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        reloadOne(player, tag);
        tag.putInt(MAG_AFTER, rounds(tag));
        if (jamChance(tag.getFloat(WEAR)) > living.getRandom().nextFloat()) {
            setState(tag, GunState.JAMMED);
            tag.putInt(TIMER, JAM_TICKS);
            playAnimation(tag, GunAnimation.JAMMED);
        } else {
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, 0);
            playAnimation(tag, GunAnimation.RELOAD_END);
        }
        tag.putBoolean(CANCEL_RELOAD, false);
    }

    private static void pressPrimary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        GunState current = state(tag);
        if (current == GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, true);
            save(stack, tag);
            return;
        }
        if (current != GunState.IDLE) return;
        if (rounds(tag) <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            setState(tag, GunState.DRAWING);
            tag.putInt(TIMER, FIRE_DELAY);
            save(stack, tag);
            return;
        }

        CoilAmmoType ammo = loadedAmmo(tag);
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear);
        float spread = (tag.getBoolean(AIMING) ? 0.0F : HIP_SPREAD) + wearSpread(currentWear);
        Vec3 origin = projectileOrigin(player, tag.getBoolean(AIMING));
        Vec3 heading = player.getLookAngle();
        if (!(player.level() instanceof ServerLevel level)) return;

        level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUN_COIL_FIRE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, 0);
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), DURABILITY));
        setState(tag, GunState.COOLDOWN);
        tag.putInt(TIMER, FIRE_DELAY);
        playAnimation(tag, GunAnimation.CYCLE);
        save(stack, tag);
    }

    private static void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;
        tag.putBoolean(AIMING, false);
        CoilAmmoType ammo = findFirstAmmo(player.getInventory());
        if (rounds(tag) < CAPACITY && ammo != null) {
            tag.putInt(MAG_PREV, rounds(tag));
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

    private static void reloadOne(Player player, CompoundTag tag) {
        if (rounds(tag) >= CAPACITY) return;
        CoilAmmoType ammo = findFirstAmmo(player.getInventory());
        if (ammo == null) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get())
                    || StandardAmmoTypes.fromStack(candidate) != ammo) continue;
            candidate.shrink(1);
            tag.putInt(MAG_TYPE, ammo.legacyMetadata());
            tag.putInt(MAG_COUNT, 1);
            return;
        }
    }

    private static CoilAmmoType findFirstAmmo(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get()) || candidate.isEmpty()) continue;
            if (StandardAmmoTypes.fromStack(candidate) instanceof CoilAmmoType ammo) return ammo;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.1875D, -0.0625D, 0.75D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity living,
                                      GunAnimation animation, int timer) {
        if (animation == GunAnimation.RELOAD && timer == 0) {
            level.playSound(null, living.getX(), living.getY(), living.getZ(),
                    ModSounds.GUN_COIL_RELOAD.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
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
    public static CoilAmmoType loadedAmmo(ItemStack stack) { return loadedAmmo(data(stack)); }
    private static CoilAmmoType loadedAmmo(CompoundTag tag) {
        return CoilAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    CoilAmmoType ammo, float wear, GunAnimation animation) {
        CompoundTag tag = data(stack);
        setState(tag, state);
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyMetadata());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, DURABILITY));
        tag.putBoolean(EQUIPPED, true);
        tag.putInt(LAST_ANIM, animation.ordinal());
        tag.putInt(ANIM_TIMER, 0);
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
            tag.putInt(MAG_TYPE, CoilAmmoType.TUNGSTEN.legacyMetadata());
        }
        return tag;
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        CoilAmmoType ammo = loadedAmmo(stack);
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": 35")
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.quality.special").withStyle(ChatFormatting.AQUA));
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
