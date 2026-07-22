package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.Shotgun12GaugeAmmoType;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
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

/** Four barrels, one trigger and absolutely no restraint. */
public final class LiberatorItem extends SednaGunItem {
    public static final int DURABILITY = 200;
    public static final int DRAW_TICKS = 20;
    public static final int INSPECT_TICKS = 21;
    public static final int FIRE_DELAY = 20;
    public static final int ROUNDS_PER_CYCLE = 4;
    public static final int RELOAD_BEGIN_TICKS = 25;
    public static final int RELOAD_CYCLE_TICKS = 15;
    public static final int RELOAD_END_TICKS = 7;
    public static final int JAM_TICKS = 45;
    public static final int CAPACITY = 4;
    public static final float BASE_DAMAGE = 16.0F;
    public static final float DEFAULT_HIP_SPREAD = 0.025F;
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

    public LiberatorItem() { }

    public float baseDamage() { return BASE_DAMAGE; }

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return loadedAmmo(stack).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CIRCLE; }
    @Override public float recoilVertical() { return 5.0F; }
    @Override public float recoilHorizontalSigma() { return 1.5F; }

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
        if (timer <= 1) decide(living, stack, tag, previous);
        save(stack, tag);
    }

    private static void decide(LivingEntity living, ItemStack stack, CompoundTag tag,
                               GunState previous) {
        if (previous == GunState.DRAWING || previous == GunState.COOLDOWN || previous == GunState.JAMMED) {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        boolean speedloader = WeaponModManager.hasMod(stack, 0, WeaponModManager.SPEEDLOADER);
        if (speedloader) reloadAll(player, tag);
        else reloadOne(player, tag);
        if (!speedloader && !tag.getBoolean(CANCEL_RELOAD) && canReload(player.getInventory(), tag)) {
            tag.putByte(STATE, (byte) GunState.RELOADING.ordinal());
            tag.putInt(TIMER, RELOAD_CYCLE_TICKS);
            playAnimation(tag, GunAnimation.RELOAD_CYCLE);
        } else if (jamChance(tag.getFloat(WEAR)) > living.getRandom().nextFloat()) {
            tag.putByte(STATE, (byte) GunState.JAMMED.ordinal());
            tag.putInt(TIMER, JAM_TICKS);
            playAnimation(tag, GunAnimation.JAMMED);
            tag.putBoolean(CANCEL_RELOAD, false);
        } else {
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, RELOAD_END_TICKS);
            playAnimation(tag, GunAnimation.RELOAD_END);
            tag.putBoolean(CANCEL_RELOAD, false);
        }
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

        if (!(player.level() instanceof ServerLevel level)) return;
        Shotgun12GaugeAmmoType ammo = Shotgun12GaugeAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
        boolean isAiming = tag.getBoolean(AIMING);
        Vec3 origin = projectileOrigin(player, isAiming);
        Vec3 heading = player.getLookAngle();

        // rounds(4) fires min(loaded, 4) shells in a single trigger pull. Each shell recomputes
        // Recompute damage and spread per shell, consume one round, add wear and stack the smoke.
        int shots = Math.min(loaded, ROUNDS_PER_CYCLE);
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        boolean online = player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected();
        for (int shot = 0; shot < shots; shot++) {
            float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear) * ammo.damageMultiplier();
            float spread = ammo.spread() + (isAiming ? 0.0F : DEFAULT_HIP_SPREAD) + wearSpread(currentWear);
            for (int projectile = 0; projectile < ammo.projectiles(); projectile++) {
                level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
            }
            currentWear = Math.min(currentWear + ammo.wear(), DURABILITY);
            if (online) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                        GunEffectPayload.fired(player.getId(), origin, heading, ammo.blackPowder()));
            }
        }
        // One fire sound for the entire burst. Ears have limits.
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.GUN_LIBERATOR_FIRE.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        tag.putInt(MAG_COUNT, loaded - shots);
        tag.putFloat(WEAR, currentWear);
        tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
        tag.putInt(TIMER, FIRE_DELAY);
        // One CYCLE animation persists for the burst (the last playAnimation of the tick wins).
        playAnimation(tag, GunAnimation.CYCLE);
        save(stack, tag);
    }

    private static void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;

        tag.putBoolean(AIMING, false);
        if (canReload(player.getInventory(), tag)) {
            tag.putInt(MAG_PREV, Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY));
            tag.putByte(STATE, (byte) GunState.RELOADING.ordinal());
            tag.putInt(TIMER, RELOAD_BEGIN_TICKS);
            playAnimation(tag, GunAnimation.RELOAD);
        } else {
            // inspectCancel defaults to true: INSPECT is cosmetic and leaves the gun IDLE.
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
        int count = Mth.clamp(gun.getInt(MAG_COUNT), 0, CAPACITY);
        if (count >= CAPACITY) return false;
        Shotgun12GaugeAmmoType required = count > 0
                ? Shotgun12GaugeAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE)) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private static void reloadOne(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = Mth.clamp(gun.getInt(MAG_COUNT), 0, CAPACITY);
        Shotgun12GaugeAmmoType type = loaded > 0
                ? Shotgun12GaugeAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null || loaded >= CAPACITY) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get()) || candidate.isEmpty()
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            candidate.shrink(1);
            gun.putInt(MAG_COUNT, loaded + 1);
            return;
        }
    }

    private static void reloadAll(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = Mth.clamp(gun.getInt(MAG_COUNT), 0, CAPACITY);
        Shotgun12GaugeAmmoType type = loaded > 0
                ? Shotgun12GaugeAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null || loaded >= CAPACITY) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());

        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get()) || candidate.isEmpty()
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private static Shotgun12GaugeAmmoType findFirstAmmo(Inventory inventory, Shotgun12GaugeAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get()) || candidate.isEmpty()) continue;
            if (!(StandardAmmoTypes.fromStack(candidate) instanceof Shotgun12GaugeAmmoType type)) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.1875D, -0.0625D, 0.75D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD).yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity, GunAnimation animation, int timer) {
        switch (animation) {
            case RELOAD -> {
                if (timer == 0) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.75F);
                if (timer == 15) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get(), 1.0F);
            }
            case RELOAD_CYCLE -> {
                if (timer == 5) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get(), 1.0F);
            }
            case RELOAD_END -> {
                if (timer == 2) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.9F);
            }
            case JAMMED -> {
                if (timer == 2) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.9F);
                if (timer == 12) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.75F);
                if (timer == 26) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.9F);
            }
            case CYCLE_DRY -> {
                if (timer == 0) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 1.0F);
            }
            case INSPECT -> {
                if (timer == 0) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.75F);
                if (timer == 20) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.9F);
            }
            default -> { }
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

    public static int rounds(ItemStack stack) { return Mth.clamp(data(stack).getInt(MAG_COUNT), 0, CAPACITY); }
    public static float wear(ItemStack stack) { return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, DURABILITY); }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static int amountAfterReload(ItemStack stack) { return data(stack).getInt(MAG_AFTER); }
    public static Shotgun12GaugeAmmoType loadedAmmo(ItemStack stack) {
        return Shotgun12GaugeAmmoType.fromLegacyBulletConfig(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    Shotgun12GaugeAmmoType ammo, float wear) {
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
        Shotgun12GaugeAmmoType ammo = loadedAmmo(stack);
        float pelletDamage = BASE_DAMAGE * ammo.damageMultiplier();
        float totalDamage = pelletDamage * ammo.projectiles();
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trimDamage(BASE_DAMAGE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo").append(": " + trimDamage(totalDamage)
                        + " (" + ammo.projectiles() + " x " + trimDamage(pelletDamage) + ")")
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
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

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    /** Enum order is animation protocol. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
