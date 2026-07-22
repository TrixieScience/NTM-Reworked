package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.SevenSixTwoAmmoType;
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

/** Two miniguns sharing one ammunition belt and none of the good judgment. */
public final class DualMinigunItem extends SednaGunItem {
    public static final int RECEIVER_COUNT = 2;
    public static final int DURABILITY = 50_000;
    public static final int CAPACITY = 0;
    public static final int DRAW_TICKS = 20;
    public static final int FIRE_DELAY = 1;
    public static final int DRY_TICKS = 15;
    public static final float BASE_DAMAGE = 6.0F;
    public static final float INNATE_SPREAD = 0.01F;

    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_";
    private static final String TIMER = "timer_";
    private static final String WEAR = "wear_";
    private static final String MAG_COUNT = "magcount";
    private static final String MAG_TYPE = "magtype";
    private static final String AIMING = "aiming";
    private static final String PRIMARY_HELD = "primary0";
    private static final String SECONDARY_HELD = "secondary0";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_";
    private static final String ANIM_TIMER = "animtimer_";

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressTrigger(player, stack, 0, PRIMARY_HELD);
            case SECONDARY -> pressTrigger(player, stack, 1, SECONDARY_HELD);
            case PRIMARY_RELEASE -> releaseTrigger(stack, PRIMARY_HELD);
            case SECONDARY_RELEASE -> releaseTrigger(stack, SECONDARY_HELD);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    @Override public boolean gunAutomatic() { return true; }
    @Override public boolean gunSecondaryAutomatic() { return true; }
    @Override public boolean gunBeltFed() { return true; }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.L_CIRCLE; }
    @Override public int gunRounds(ItemStack stack) { return beltCount(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack, 1); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) { return ammoIcon(stack, 1); }
    @Override public boolean gunHasMirroredHud() { return true; }
    @Override public int gunMirroredRounds(ItemStack stack) { return beltCount(stack); }
    @Override public int gunMirroredCapacity() { return CAPACITY; }
    @Override public float gunMirroredWear(ItemStack stack) { return wear(stack, 0); }
    @Override public float gunMirroredDurability() { return DURABILITY; }
    @Override public ItemStack gunMirroredAmmoIcon(ItemStack stack) { return ammoIcon(stack, 0); }
    @Override public float recoilVertical() { return 0.0F; }
    @Override public float recoilVerticalSigma() { return 0.5F; }
    @Override public float recoilHorizontalSigma() { return 0.5F; }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;
        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);

        if (living instanceof Player player) {
            SevenSixTwoAmmoType belt = firstAmmo(player);
            int count = beltCount(player);
            for (int index = 0; index < RECEIVER_COUNT; index++) {
                tag.putInt(magCountKey(index), count);
                if (belt != null) tag.putInt(magTypeKey(index), belt.legacyBulletConfig());
            }
        }

        if (!held) {
            for (int index = 0; index < RECEIVER_COUNT; index++) {
                if (state(tag, index) != GunState.JAMMED) {
                    setState(tag, index, GunState.DRAWING);
                    tag.putInt(timerKey(index), DRAW_TICKS);
                }
                tag.putInt(lastAnimationKey(index), GunAnimation.CYCLE.ordinal());
            }
            tag.putBoolean(AIMING, false);
            tag.putBoolean(PRIMARY_HELD, false);
            tag.putBoolean(SECONDARY_HELD, false);
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
            String heldKey = index == 0 ? PRIMARY_HELD : SECONDARY_HELD;
            if (tag.getBoolean(heldKey) && living instanceof Player player && beltCount(player) > 0) {
                ((DualMinigunItem) player.getMainHandItem().getItem()).fire(player,
                        player.getMainHandItem(), tag, index);
            } else {
                setState(tag, index, GunState.IDLE);
                tag.putInt(timerKey(index), 0);
            }
            return;
        }
    }

    private void pressTrigger(Player player, ItemStack stack, int index, String heldKey) {
        CompoundTag tag = data(stack);
        tag.putBoolean(heldKey, true);
        GunState current = state(tag, index);
        if (current != GunState.IDLE) return;
        if (beltCount(player) <= 0) {
            playAnimation(tag, index, GunAnimation.CYCLE_DRY);
            setState(tag, index, GunState.DRAWING);
            tag.putInt(timerKey(index), DRY_TICKS);
            save(stack, tag);
            return;
        }
        fire(player, stack, tag, index);
        save(stack, tag);
    }

    private static void releaseTrigger(ItemStack stack, String heldKey) {
        CompoundTag tag = data(stack);
        tag.putBoolean(heldKey, false);
        save(stack, tag);
    }

    private static void toggleAim(ItemStack stack) {
        CompoundTag tag = data(stack);
        tag.putBoolean(AIMING, !tag.getBoolean(AIMING));
        save(stack, tag);
    }

    private void fire(Player player, ItemStack stack, CompoundTag tag, int index) {
        SevenSixTwoAmmoType ammo = firstAmmo(player);
        if (ammo == null || !(player.level() instanceof ServerLevel level)) return;
        float currentWear = Mth.clamp(tag.getFloat(wearKey(index)), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * SevenSixTwoGunItem.wearDamageMultiplier(currentWear, DURABILITY)
                * ammo.damageMultiplier();
        float spread = INNATE_SPREAD + ammo.spread()
                + (tag.getBoolean(AIMING) ? 0.0F : SevenSixTwoGunItem.HIP_SPREAD)
                + SevenSixTwoGunItem.wearSpread(currentWear, DURABILITY);
        Vec3 origin = projectileOrigin(player, index);
        Vec3 heading = player.getLookAngle();

        level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.GUN_MINIGUN_FIRE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false, index));
        }

        consumeAmmo(player, ammo);
        tag.putInt(magTypeKey(index), ammo.legacyBulletConfig());
        SevenSixTwoAmmoType next = firstAmmo(player);
        int remaining = beltCount(player);
        for (int receiver = 0; receiver < RECEIVER_COUNT; receiver++) {
            tag.putInt(magCountKey(receiver), remaining);
            if (next != null) tag.putInt(magTypeKey(receiver), next.legacyBulletConfig());
        }
        tag.putFloat(wearKey(index), Math.min(currentWear + ammo.wear(), DURABILITY));
        setState(tag, index, GunState.COOLDOWN);
        tag.putInt(timerKey(index), FIRE_DELAY);
        playAnimation(tag, index, GunAnimation.CYCLE);
    }

    private static void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        for (int index = 0; index < RECEIVER_COUNT; index++) {
            if (state(tag, index) != GunState.IDLE) continue;
            playAnimation(tag, index, GunAnimation.INSPECT);
        }
        save(stack, tag);
    }

    private static SevenSixTwoAmmoType firstAmmo(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (StandardAmmoTypes.fromStack(candidate) instanceof SevenSixTwoAmmoType type) return type;
        }
        return null;
    }

    private static int beltCount(Player player) {
        SevenSixTwoAmmoType type = firstAmmo(player);
        if (type == null) return 0;
        int total = 0;
        for (ItemStack candidate : player.getInventory().items) {
            if (candidate.is(ModItems.AMMO_STANDARD.get())
                    && StandardAmmoTypes.fromStack(candidate) == type) total += candidate.getCount();
        }
        return total;
    }

    private static int beltCount(ItemStack gun) {
        return Math.max(0, data(gun).getInt(magCountKey(0)));
    }

    private static void consumeAmmo(Player player, SevenSixTwoAmmoType type) {
        for (ItemStack candidate : player.getInventory().items) {
            if (candidate.is(ModItems.AMMO_STANDARD.get())
                    && StandardAmmoTypes.fromStack(candidate) == type) {
                candidate.shrink(1);
                return;
            }
        }
    }

    private static Vec3 projectileOrigin(Player player, int index) {
        Vec3 local = new Vec3(index == 0 ? -0.25D : 0.25D, -0.15625D, 1.0D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity,
                                      GunAnimation animation, int timer) {
        if (animation == GunAnimation.CYCLE && timer == 1) {
            play(level, entity, ModSounds.GUN_REVOLVER_SPIN.get(), 0.75F);
        } else if (animation == GunAnimation.CYCLE_DRY) {
            if (timer == 0) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 0.75F);
            if (timer == 1) play(level, entity, ModSounds.GUN_REVOLVER_SPIN.get(), 0.75F);
        } else if (animation == GunAnimation.INSPECT && timer == 0) {
            play(level, entity, ModSounds.GUN_REVOLVER_SPIN.get(), 0.75F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, pitch);
    }

    public static int rounds(ItemStack stack, int index) { return beltCount(stack); }
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
    public static SevenSixTwoAmmoType loadedAmmo(ItemStack stack, int index) {
        return loadedAmmo(data(stack), index);
    }
    private static SevenSixTwoAmmoType loadedAmmo(CompoundTag tag, int index) {
        checkIndex(index);
        return SevenSixTwoAmmoType.fromLegacyBulletConfig(tag.getInt(magTypeKey(index)));
    }

    public static void setTestState(ItemStack stack, int index, GunState state, int timer,
                                    int rounds, SevenSixTwoAmmoType ammo, float wear) {
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
                tag.putInt(magTypeKey(index), SevenSixTwoAmmoType.SOFT_POINT.legacyBulletConfig());
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
            SevenSixTwoAmmoType ammo = loadedAmmo(stack, index);
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
        tooltip.add(Component.translatable("gui.weapon.quality.debug").withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) return Integer.toString(Math.round(damage));
        return String.format(Locale.ROOT, "%.3f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void checkIndex(int index) {
        if (index < 0 || index >= RECEIVER_COUNT) {
            throw new IllegalArgumentException("Dual Minigun receiver index must be 0 or 1");
        }
    }
    private static String stateKey(int index) { return STATE + index; }
    private static String timerKey(int index) { return TIMER + index; }
    private static String wearKey(int index) { return WEAR + index; }
    private static String magCountKey(int index) { return MAG_COUNT + index; }
    private static String magTypeKey(int index) { return MAG_TYPE + index; }
    private static String lastAnimationKey(int index) { return LAST_ANIM + index; }
    private static String animationTimerKey(int index) { return ANIM_TIMER + index; }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }
    /** Enum order is animation protocol. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
