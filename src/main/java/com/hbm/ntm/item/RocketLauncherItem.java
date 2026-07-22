package com.hbm.ntm.item;

import com.hbm.ntm.entity.RocketProjectileEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.RocketAmmoType;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.WeaponModManager;
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

/** Source Panzerschreck receiver; later XFactoryRocket launchers build on the shared rocket entity. */
public class RocketLauncherItem extends SednaGunItem {
    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_0";
    private static final String TIMER = "timer_0";
    private static final String WEAR = "wear_0";
    private static final String MAG_COUNT = "magcount0";
    private static final String MAG_TYPE = "magtype0";
    private static final String MAG_PREV = "magprev0";
    private static final String AIMING = "aiming";
    private static final String PRIMARY_HELD = "primary0";
    private static final String CANCEL_RELOAD = "cancel";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    public static final int DURABILITY = 300;
    public static final int DRAW_TICKS = 7;
    public static final int INSPECT_TICKS = 40;
    public static final int FIRE_DELAY = 5;
    public static final int RELOAD_TICKS = 50;
    public static final int JAM_TICKS = 40;
    public static final int CAPACITY = 1;
    public static final float BASE_DAMAGE = 25.0F;

    protected int receiverDurability() { return DURABILITY; }
    protected int receiverDrawTicks() { return DRAW_TICKS; }
    protected int receiverDrawTicks(ItemStack stack) {
        return stack.is(ModItems.GUN_PANZERSCHRECK.get())
                && WeaponModManager.hasMod(stack, 0, WeaponModManager.NO_SHIELD)
                ? 5 : receiverDrawTicks();
    }
    protected int receiverFireDelay() { return FIRE_DELAY; }
    protected int receiverReloadTicks() { return RELOAD_TICKS; }
    protected int receiverJamTicks() { return JAM_TICKS; }
    protected float receiverBaseDamage() { return BASE_DAMAGE; }
    protected RocketAmmoType receiverDefaultAmmo() { return RocketAmmoType.HIGH_EXPLOSIVE; }
    protected RocketProjectileEntity.FlightMode receiverFlightMode() {
        return RocketProjectileEntity.FlightMode.STANDARD;
    }
    protected Entity acquireShotTarget(Player player, boolean aiming) { return null; }

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
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CIRCUMFLEX; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return receiverDurability(); }
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
                tag.putInt(TIMER, receiverDrawTicks(stack));
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
        if (previous == GunState.DRAWING || previous == GunState.JAMMED
                || previous == GunState.COOLDOWN) {
            setState(tag, GunState.IDLE);
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        if (!tag.getBoolean(CANCEL_RELOAD)) reloadOne(player, tag);
        tag.putBoolean(CANCEL_RELOAD, false);
        if (jamChance(tag.getFloat(WEAR), receiverDurability()) > living.getRandom().nextFloat()) {
            setState(tag, GunState.JAMMED);
            tag.putInt(TIMER, receiverJamTicks());
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
        if (current != GunState.IDLE) {
            save(stack, tag);
            return;
        }
        if (rounds(tag) <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            setState(tag, GunState.COOLDOWN);
            tag.putInt(TIMER, receiverFireDelay());
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

        RocketAmmoType ammo = RocketAmmoType.fromLegacyMetadata(tag.getInt(MAG_TYPE));
        int durability = receiverDurability();
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, durability);
        float damage = receiverBaseDamage() * wearDamageMultiplier(currentWear, durability)
                * ammo.damageMultiplier();
        float spread = (tag.getBoolean(AIMING) ? 0.0F : 0.025F)
                + wearSpread(currentWear, durability) * 0.125F;
        Vec3 origin = projectileOrigin(player, tag.getBoolean(AIMING));
        Vec3 heading = player.getLookAngle();
        Entity target = acquireShotTarget(player, tag.getBoolean(AIMING));
        level.addFreshEntity(new RocketProjectileEntity(level, player, ammo, damage, spread,
                origin, heading, target, receiverFlightMode()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUN_ROCKET_FIRE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, loaded - 1);
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), durability));
        setState(tag, GunState.COOLDOWN);
        tag.putInt(TIMER, receiverFireDelay());
        playAnimation(tag, GunAnimation.CYCLE);
        if (stack.is(ModItems.GUN_PANZERSCHRECK.get())
                && WeaponModManager.hasMod(stack, 0, WeaponModManager.NO_SHIELD)) {
            player.setRemainingFireTicks(player.getRemainingFireTicks() + 100);
            player.hurt(level.damageSources().onFire(), 4.0F);
        }
    }

    private void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;
        tag.putBoolean(AIMING, false);
        RocketAmmoType available = findAmmo(player.getInventory());
        if (rounds(tag) < CAPACITY && available != null) {
            tag.putInt(MAG_PREV, rounds(tag));
            tag.putInt(MAG_TYPE, available.legacyMetadata());
            setState(tag, GunState.RELOADING);
            tag.putInt(TIMER, receiverReloadTicks());
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
        RocketAmmoType required = RocketAmmoType.fromLegacyMetadata(tag.getInt(MAG_TYPE));
        if (!consumeAmmo(player.getInventory(), required)) return;
        tag.putInt(MAG_COUNT, 1);
    }

    private static RocketAmmoType findAmmo(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (StandardAmmoTypes.fromStack(candidate) instanceof RocketAmmoType type) return type;
        }
        return null;
    }

    private static boolean consumeAmmo(Inventory inventory, RocketAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (StandardAmmoTypes.fromStack(candidate) != required) continue;
            candidate.shrink(1);
            return true;
        }
        return false;
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.1875D, -0.09375D, 1.0D);
        return player.getEyePosition().add(local
                .xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD));
    }

    protected void playOrchestra(Level level, LivingEntity entity,
                                 GunAnimation animation, int timer) {
        if (animation == GunAnimation.RELOAD && timer == 30) {
            play(level, entity, ModSounds.GUN_CANISTER_INSERT.get());
        }
    }

    protected static void play(Level level, LivingEntity entity, SoundEvent sound) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static float jamChance(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.66F ? 0.0F : Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    public static float wearDamageMultiplier(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static float wearSpread(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F;
    }

    public static int rounds(ItemStack stack) { return rounds(data(stack)); }
    private static int rounds(CompoundTag tag) { return Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY); }
    public static float wear(ItemStack stack) {
        return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, durabilityFor(stack));
    }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static RocketAmmoType loadedAmmo(ItemStack stack) {
        return RocketAmmoType.fromLegacyMetadata(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    RocketAmmoType ammo, float wear) {
        setTestState(stack, state, timer, rounds, ammo, wear, false);
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    RocketAmmoType ammo, float wear, boolean aiming) {
        CompoundTag tag = data(stack);
        setState(tag, state);
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyMetadata());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, durabilityFor(stack)));
        tag.putBoolean(AIMING, aiming);
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
        if (!tag.getBoolean(INITIALIZED) && stack.getItem() instanceof RocketLauncherItem launcher) {
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(MAG_TYPE, launcher.receiverDefaultAmmo().legacyMetadata());
            tag.putInt(MAG_COUNT, 0);
        }
        return tag;
    }

    private static int durabilityFor(ItemStack stack) {
        return stack.getItem() instanceof RocketLauncherItem launcher
                ? launcher.receiverDurability() : DURABILITY;
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        RocketAmmoType ammo = loadedAmmo(stack);
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage")
                .append(": " + (int) receiverBaseDamage()).withStyle(ChatFormatting.GRAY));
        int durability = receiverDurability();
        int condition = Mth.clamp((int) ((durability - wear(stack)) * 100.0F / durability), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.quality.aside").withStyle(ChatFormatting.YELLOW));
        for (ItemStack mod : WeaponModManager.installedMods(stack, 0)) {
            tooltip.add(mod.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        }
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }
    public enum GunAnimation { EQUIP, CYCLE, CYCLE_DRY, RELOAD, JAMMED, INSPECT }
}
