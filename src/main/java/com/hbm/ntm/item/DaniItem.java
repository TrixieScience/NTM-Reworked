package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.Magnum357AmmoType;
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

/** Day and Night, with a cylinder apiece. */
public final class DaniItem extends SednaGunItem {
    public static final int RECEIVER_COUNT = 2;
    public static final int DURABILITY = 30_000;
    public static final int CAPACITY = 6;
    public static final int DRAW_TICKS = 20;
    public static final int FIRE_DELAY = 11;
    public static final int DRY_TICKS = 11;
    public static final int RELOAD_TICKS = 55;
    public static final int JAM_TICKS = 45;
    public static final float BASE_DAMAGE = 15.0F;
    public static final float INNATE_SPREAD = 0.0F;

    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_";
    private static final String TIMER = "timer_";
    private static final String WEAR = "wear_";
    private static final String MAG_COUNT = "magcount";
    private static final String MAG_TYPE = "magtype";
    private static final String MAG_PREV = "magprev";
    private static final String MAG_AFTER = "magafter";
    private static final String CANCEL_RELOAD = "cancel";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_";
    private static final String ANIM_TIMER = "animtimer_";

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressTrigger(player, stack, 0);
            case SECONDARY -> pressTrigger(player, stack, 1);
            case RELOAD -> pressReload(player, stack);
            case PRIMARY_RELEASE, SECONDARY_RELEASE, TOGGLE_AIM -> {
                // Both configs are semi-automatic and have no tertiary aim binding.
            }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return false; }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack, 1); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack, 1); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) { return ammoIcon(stack, 1); }
    @Override public boolean gunHasMirroredHud() { return true; }
    @Override public int gunMirroredRounds(ItemStack stack) { return rounds(stack, 0); }
    @Override public int gunMirroredCapacity() { return CAPACITY; }
    @Override public float gunMirroredWear(ItemStack stack) { return wear(stack, 0); }
    @Override public float gunMirroredDurability() { return DURABILITY; }
    @Override public ItemStack gunMirroredAmmoIcon(ItemStack stack) { return ammoIcon(stack, 0); }
    @Override public float recoilVertical() { return 5.0F; }
    @Override public float recoilHorizontalSigma() { return 0.75F; }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;
        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);

        if (!held) {
            for (int index = 0; index < RECEIVER_COUNT; index++) {
                if (state(tag, index) != GunState.JAMMED) {
                    setState(tag, index, GunState.DRAWING);
                    tag.putInt(timerKey(index), DRAW_TICKS);
                }
                tag.putInt(lastAnimationKey(index), GunAnimation.CYCLE.ordinal());
            }
            tag.putBoolean(CANCEL_RELOAD, false);
            tag.putBoolean(EQUIPPED, false);
            save(stack, tag);
            return;
        }

        if (!tag.getBoolean(EQUIPPED)) {
            for (int index = 0; index < RECEIVER_COUNT; index++) {
                playAnimation(tag, index, GunAnimation.EQUIP);
            }
        }
        tag.putBoolean(EQUIPPED, true);

        for (int index = 0; index < RECEIVER_COUNT; index++) {
            GunState previous = state(tag, index);
            int animationTimer = tag.getInt(animationTimerKey(index));
            playOrchestra(level, living, animation(tag, index), animationTimer);
            tag.putInt(animationTimerKey(index), animationTimer + 1);
            int timer = tag.getInt(timerKey(index));
            if (timer > 0) tag.putInt(timerKey(index), timer - 1);
            if (timer <= 1) decide(living, tag, previous, index);
        }
        if (state(tag, 0) != GunState.RELOADING && state(tag, 1) != GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, false);
        }
        save(stack, tag);
    }

    private static void decide(LivingEntity living, CompoundTag tag,
                               GunState previous, int index) {
        if (previous == GunState.DRAWING || previous == GunState.JAMMED) {
            setState(tag, index, GunState.IDLE);
            tag.putInt(timerKey(index), 0);
            return;
        }
        if (previous == GunState.COOLDOWN) {
            setState(tag, index, GunState.IDLE);
            tag.putInt(timerKey(index), 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        if (!tag.getBoolean(CANCEL_RELOAD)) reloadAction(player, tag, index);
        tag.putInt(magAfterKey(index), rounds(tag, index));
        if (jamChance(tag.getFloat(wearKey(index)))
                > living.getRandom().nextFloat()) {
            setState(tag, index, GunState.JAMMED);
            tag.putInt(timerKey(index), JAM_TICKS);
            playAnimation(tag, index, GunAnimation.JAMMED);
        } else {
            setState(tag, index, GunState.IDLE);
            tag.putInt(timerKey(index), 0);
        }
    }

    private void pressTrigger(Player player, ItemStack stack, int index) {
        CompoundTag tag = data(stack);
        GunState current = state(tag, index);
        if (current == GunState.RELOADING) {
            tag.putBoolean(CANCEL_RELOAD, true);
            save(stack, tag);
            return;
        }
        if (current != GunState.IDLE) return;
        if (rounds(tag, index) <= 0) {
            playAnimation(tag, index, GunAnimation.CYCLE_DRY);
            setState(tag, index, GunState.DRAWING);
            tag.putInt(timerKey(index), DRY_TICKS);
            save(stack, tag);
            return;
        }
        fire(player, stack, tag, index);
        save(stack, tag);
    }

    private void fire(Player player, ItemStack stack, CompoundTag tag, int index) {
        int loaded = rounds(tag, index);
        if (loaded <= 0 || !(player.level() instanceof ServerLevel level)) return;
        Magnum357AmmoType ammo = loadedAmmo(tag, index);
        float currentWear = Mth.clamp(tag.getFloat(wearKey(index)), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear)
                * ammo.damageMultiplier();
        float spread = INNATE_SPREAD + ammo.spread()
                + wearSpread(currentWear);
        Vec3 origin = projectileOrigin(player, index);
        Vec3 heading = player.getLookAngle();

        level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUN_PISTOL_FIRE.get(), SoundSource.PLAYERS, 1.0F, index == 0 ? 1.1F : 0.9F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false, index));
        }

        tag.putInt(magCountKey(index), loaded - 1);
        tag.putFloat(wearKey(index), Math.min(currentWear + ammo.wear(), DURABILITY));
        setState(tag, index, GunState.COOLDOWN);
        tag.putInt(timerKey(index), FIRE_DELAY);
        playAnimation(tag, index, GunAnimation.CYCLE);
    }

    private static void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        for (int index = 0; index < RECEIVER_COUNT; index++) {
            if (state(tag, index) != GunState.IDLE) continue;
            if (canReload(player.getInventory(), tag, index)) {
                tag.putInt(magPrevKey(index), rounds(tag, index));
                setState(tag, index, GunState.RELOADING);
                tag.putInt(timerKey(index), RELOAD_TICKS);
                playAnimation(tag, index, GunAnimation.RELOAD);
            } else {
                playAnimation(tag, index, GunAnimation.INSPECT);
            }
        }
        save(stack, tag);
    }

    private static boolean canReload(Inventory inventory, CompoundTag gun, int index) {
        int count = rounds(gun, index);
        if (count >= CAPACITY) return false;
        Magnum357AmmoType required = count > 0 ? loadedAmmo(gun, index) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private static void reloadAction(Player player, CompoundTag gun, int index) {
        Inventory inventory = player.getInventory();
        int loaded = rounds(gun, index);
        Magnum357AmmoType type = loaded > 0 ? loadedAmmo(gun, index) : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(magTypeKey(index), type.legacyBulletConfig());
        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get())
                    || StandardAmmoTypes.fromStack(candidate) != type) continue;
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(magCountKey(index), loaded);
    }

    private static Magnum357AmmoType findFirstAmmo(Inventory inventory, Magnum357AmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (!(StandardAmmoTypes.fromStack(candidate) instanceof Magnum357AmmoType type)) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player, int index) {
        Vec3 local = new Vec3(index == 0 ? 0.3125D : -0.3125D, -0.0625D, 0.75D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity,
                                      GunAnimation animation, int timer) {
        if (animation == GunAnimation.RELOAD) {
            if (timer == 2) play(level, entity, ModSounds.GUN_MAG_SMALL_REMOVE.get(), 1.0F);
            if (timer == 36) play(level, entity, ModSounds.GUN_MAG_SMALL_INSERT.get(), 1.0F);
            if (timer == 44) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F);
        } else if (animation == GunAnimation.CYCLE && timer == 5) {
            play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.9F);
        } else if (animation == GunAnimation.CYCLE_DRY) {
            if (timer == 2) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 1.0F);
            if (timer == 5) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.9F);
        } else if (animation == GunAnimation.INSPECT) {
            if (timer == 2) play(level, entity, ModSounds.GUN_MAG_SMALL_REMOVE.get(), 1.0F);
            if (timer == 24) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F);
        } else if (animation == GunAnimation.JAMMED) {
            if (timer == 12) play(level, entity, ModSounds.GUN_MAG_SMALL_REMOVE.get(), 1.0F);
            if (timer == 34) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static float jamChance(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.66F ? 0.0F : Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    private static float wearDamageMultiplier(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    private static float wearSpread(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 0.25F;
    }

    public static int rounds(ItemStack stack, int index) { return rounds(data(stack), index); }
    private static int rounds(CompoundTag tag, int index) {
        checkIndex(index);
        return Mth.clamp(tag.getInt(magCountKey(index)), 0, CAPACITY);
    }
    public static float wear(ItemStack stack, int index) {
        checkIndex(index);
        return Mth.clamp(data(stack).getFloat(wearKey(index)), 0.0F, DURABILITY);
    }
    public static GunState state(ItemStack stack, int index) { return state(data(stack), index); }
    public static int timer(ItemStack stack, int index) { return data(stack).getInt(timerKey(index)); }
    public static GunAnimation animation(ItemStack stack, int index) {
        return animation(data(stack), index);
    }
    public static int animationTimer(ItemStack stack, int index) {
        return data(stack).getInt(animationTimerKey(index));
    }
    public static int amountBeforeReload(ItemStack stack, int index) {
        return data(stack).getInt(magPrevKey(index));
    }
    public static Magnum357AmmoType loadedAmmo(ItemStack stack, int index) {
        return loadedAmmo(data(stack), index);
    }
    private static Magnum357AmmoType loadedAmmo(CompoundTag tag, int index) {
        checkIndex(index);
        return Magnum357AmmoType.fromLegacyBulletConfig(tag.getInt(magTypeKey(index)));
    }

    public static void setTestState(ItemStack stack, int index, GunState state, int timer,
                                    int rounds, Magnum357AmmoType ammo, float wear) {
        checkIndex(index);
        CompoundTag tag = data(stack);
        setState(tag, index, state);
        tag.putInt(timerKey(index), timer);
        tag.putInt(magCountKey(index), Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(magTypeKey(index), ammo.legacyBulletConfig());
        tag.putFloat(wearKey(index), Mth.clamp(wear, 0.0F, DURABILITY));
        tag.putBoolean(EQUIPPED, true);
        save(stack, tag);
    }

    private static GunState state(CompoundTag tag, int index) {
        checkIndex(index);
        int ordinal = tag.getByte(stateKey(index));
        GunState[] values = GunState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunState.DRAWING;
    }
    private static void setState(CompoundTag tag, int index, GunState state) {
        tag.putByte(stateKey(index), (byte) state.ordinal());
    }
    private static GunAnimation animation(CompoundTag tag, int index) {
        checkIndex(index);
        int ordinal = tag.getInt(lastAnimationKey(index));
        GunAnimation[] values = GunAnimation.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunAnimation.CYCLE;
    }
    private static void playAnimation(CompoundTag tag, int index, GunAnimation animation) {
        tag.putInt(lastAnimationKey(index), animation.ordinal());
        tag.putInt(animationTimerKey(index), 0);
    }

    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(INITIALIZED)) {
            tag.putBoolean(INITIALIZED, true);
            for (int index = 0; index < RECEIVER_COUNT; index++) {
                tag.putInt(magTypeKey(index), Magnum357AmmoType.EXPRESS.legacyBulletConfig());
            }
        }
        return tag;
    }
    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    private static ItemStack ammoIcon(ItemStack stack, int index) {
        return loadedAmmo(stack, index).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        for (int index = 0; index < RECEIVER_COUNT; index++) {
            Magnum357AmmoType ammo = loadedAmmo(stack, index);
            tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                    .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                    .append(" " + rounds(stack, index) + " / " + CAPACITY)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                    .append(": " + trimDamage(BASE_DAMAGE * ammo.damageMultiplier()))
                    .withStyle(ChatFormatting.GRAY));
            int condition = Mth.clamp((int) ((DURABILITY - wear(stack, index))
                    * 100.0F / DURABILITY), 0, 100);
            tooltip.add(Component.translatable("gui.weapon.condition")
                    .append(": " + condition + "%").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("gui.weapon.quality.legendary").withStyle(ChatFormatting.GOLD));
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) return Integer.toString(Math.round(damage));
        return String.format(Locale.ROOT, "%.3f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void checkIndex(int index) {
        if (index < 0 || index >= RECEIVER_COUNT) {
            throw new IllegalArgumentException("Day And Night receiver index must be 0 or 1");
        }
    }
    private static String stateKey(int index) { return STATE + index; }
    private static String timerKey(int index) { return TIMER + index; }
    private static String wearKey(int index) { return WEAR + index; }
    private static String magCountKey(int index) { return MAG_COUNT + index; }
    private static String magTypeKey(int index) { return MAG_TYPE + index; }
    private static String magPrevKey(int index) { return MAG_PREV + index; }
    private static String magAfterKey(int index) { return MAG_AFTER + index; }
    private static String lastAnimationKey(int index) { return LAST_ANIM + index; }
    private static String animationTimerKey(int index) { return ANIM_TIMER + index; }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }
    /** Enum order is animation protocol. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
