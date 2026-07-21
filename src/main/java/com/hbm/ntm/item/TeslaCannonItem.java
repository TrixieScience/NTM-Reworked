package com.hbm.ntm.item;

import com.hbm.ntm.entity.TeslaBeamEntity;
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

import java.util.List;
import java.util.Locale;

/** Tesla Cannon: three flavors of capacitor and no respect for nearby electronics. */
public final class TeslaCannonItem extends SednaGunItem {
    public static final int DURABILITY = 1_000;
    public static final int DRAW_TICKS = 10;
    public static final int INSPECT_TICKS = 33;
    public static final int FIRE_DELAY = 20;
    public static final int JAM_TICKS = 19;
    public static final int DRY_TICKS = 10;
    public static final float BASE_DAMAGE = 35.0F;
    public static final float HIP_SPREAD = 1.5F;

    private static final String INITIALIZED = "hbm_initialized";
    private static final String STATE = "state_0";
    private static final String TIMER = "timer_0";
    private static final String WEAR = "wear_0";
    private static final String MAG_COUNT = "magcount0";
    private static final String CYCLE_COUNT = "cyclecount0";
    private static final String MAG_TYPE = "magtype0";
    private static final String AIMING = "aiming";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    @Override protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case RELOAD -> inspect(stack);
            case TOGGLE_AIM -> toggleAim(stack);
            default -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public boolean gunBeltFed() { return true; }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    @Override public int gunRounds(ItemStack stack) { return beltCount(stack); }
    @Override public int gunCapacity() { return 0; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return DURABILITY; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return loadedAmmo(stack).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public float recoilVertical() { return 0.0F; }
    @Override public float recoilHorizontalSigma() { return 0.0F; }

    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;
        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);
        GunState previous = state(tag);
        if (!held) {
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, DRAW_TICKS);
            tag.putInt(LAST_ANIM, GunAnimation.CYCLE.ordinal());
            tag.putBoolean(AIMING, false);
            tag.putBoolean(EQUIPPED, false);
            save(stack, tag);
            return;
        }

        if (living instanceof Player player) refreshBelt(player, tag);
        if (!tag.getBoolean(EQUIPPED)) playAnimation(tag, GunAnimation.EQUIP);
        tag.putBoolean(EQUIPPED, true);
        int animationTimer = tag.getInt(ANIM_TIMER);
        playOrchestra(level, living, animation(tag), animationTimer);
        tag.putInt(ANIM_TIMER, animationTimer + 1);

        int timer = tag.getInt(TIMER);
        if (timer > 0) tag.putInt(TIMER, timer - 1);
        if (timer <= 1 && (previous == GunState.DRAWING || previous == GunState.COOLDOWN)) {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
        }
        save(stack, tag);
    }

    private void pressPrimary(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;
        EnergyAmmoType ammo = firstAcceptedType(player, tag);
        if (beltAmount(player, ammo) <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
            tag.putInt(TIMER, DRY_TICKS);
            save(stack, tag);
            return;
        }
        fire(player, tag, ammo);
        save(stack, tag);
    }

    private void fire(Player player, CompoundTag tag, EnergyAmmoType ammo) {
        if (!(player.level() instanceof ServerLevel level)) return;
        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, DURABILITY);
        float damage = BASE_DAMAGE * wearDamageMultiplier(currentWear) * ammo.damageMultiplier();
        boolean aiming = tag.getBoolean(AIMING);
        // Sedna stores forward/up/side, the projectile wants side/up/forward.
        Vec3 offset = new Vec3(aiming ? -0.25D : -0.375D, 0.0D, 0.75D);
        TeslaBeamEntity beam = new TeslaBeamEntity(level, player, ammo, damage,
                aiming ? 0.0F : HIP_SPREAD, offset);
        beam.performHitscan();
        level.addFreshEntity(beam);
        play(level, player, ModSounds.GUN_TESLA_FIRE.get(), 1.0F, 1.0F);

        tag.putInt(CYCLE_COUNT, beltAmount(player, ammo));
        consumeBelt(player, ammo);
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putInt(MAG_COUNT, beltAmount(player, ammo));
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), DURABILITY));
        tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
        tag.putInt(TIMER, FIRE_DELAY);
        playAnimation(tag, GunAnimation.CYCLE);
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

    private static EnergyAmmoType firstAcceptedType(Player player, CompoundTag tag) {
        EnergyAmmoType found = scanFirst(player.getInventory());
        return found == null ? EnergyAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE)) : found;
    }

    private static EnergyAmmoType scanFirst(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.is(ModItems.AMMO_STANDARD.get())
                    && StandardAmmoTypes.fromStack(candidate) instanceof EnergyAmmoType type) return type;
        }
        return null;
    }

    private static int beltAmount(Player player, EnergyAmmoType type) {
        int amount = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(ModItems.AMMO_STANDARD.get()) && StandardAmmoTypes.fromStack(candidate) == type) {
                amount += candidate.getCount();
            }
        }
        return amount;
    }

    private static void consumeBelt(Player player, EnergyAmmoType type) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(ModItems.AMMO_STANDARD.get()) && StandardAmmoTypes.fromStack(candidate) == type) {
                candidate.shrink(1); return;
            }
        }
    }

    private static void refreshBelt(Player player, CompoundTag tag) {
        EnergyAmmoType type = firstAcceptedType(player, tag);
        tag.putInt(MAG_TYPE, type.legacyBulletConfig());
        tag.putInt(MAG_COUNT, beltAmount(player, type));
    }

    private static void playOrchestra(Level level, LivingEntity living, GunAnimation animation, int timer) {
        if ((animation == GunAnimation.CYCLE || animation == GunAnimation.CYCLE_DRY) && timer == 2) {
            play(level, living, ModSounds.GUN_SHREDDER_CYCLE.get(), 0.25F, 1.25F);
        }
        if (animation == GunAnimation.CYCLE_DRY && timer == 0) {
            play(level, living, ModSounds.GUN_DRY_FIRE.get(), 1.0F, 1.0F);
        }
        if (animation == GunAnimation.INSPECT && timer == 12) {
            play(level, living, ModSounds.TESLA_YOMI.get(), 0.25F, 1.0F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, volume, pitch);
    }

    private static float wearDamageMultiplier(float wear) {
        float percent = wear / DURABILITY;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static int beltCount(ItemStack stack) { return Math.max(0, data(stack).getInt(MAG_COUNT)); }
    public static int cycleCount(ItemStack stack) {
        return animation(stack) == GunAnimation.CYCLE
                ? Math.max(beltCount(stack), data(stack).getInt(CYCLE_COUNT))
                : beltCount(stack);
    }
    public static float wear(ItemStack stack) { return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, DURABILITY); }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static EnergyAmmoType loadedAmmo(ItemStack stack) {
        return EnergyAmmoType.fromLegacyBulletConfig(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, EnergyAmmoType ammo, int beltCount) {
        CompoundTag tag = data(stack);
        tag.putByte(STATE, (byte) state.ordinal()); tag.putInt(TIMER, 0);
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig()); tag.putInt(MAG_COUNT, beltCount);
        tag.putBoolean(EQUIPPED, true); save(stack, tag);
    }

    private static GunState state(CompoundTag tag) {
        int ordinal = tag.getByte(STATE);
        return ordinal >= 0 && ordinal < GunState.values().length ? GunState.values()[ordinal] : GunState.DRAWING;
    }
    private static GunAnimation animation(CompoundTag tag) {
        int ordinal = tag.getInt(LAST_ANIM);
        return ordinal >= 0 && ordinal < GunAnimation.values().length ? GunAnimation.values()[ordinal] : GunAnimation.CYCLE;
    }
    private static void playAnimation(CompoundTag tag, GunAnimation animation) {
        tag.putInt(LAST_ANIM, animation.ordinal()); tag.putInt(ANIM_TIMER, 0);
    }
    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(INITIALIZED)) {
            tag.putBoolean(INITIALIZED, true); tag.putInt(MAG_TYPE, EnergyAmmoType.STANDARD.legacyBulletConfig());
        }
        return tag;
    }
    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                                          List<Component> tooltip, TooltipFlag flag) {
        EnergyAmmoType ammo = loadedAmmo(stack);
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" x" + beltCount(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage").append(": " + trim(BASE_DAMAGE))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                .append(": " + trim(BASE_DAMAGE * ammo.damageMultiplier())).withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((DURABILITY - wear(stack)) * 100.0F / DURABILITY), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
    }

    private static String trim(float value) {
        if (Math.abs(value - Math.round(value)) < 0.0001F) return Integer.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN }
    public enum GunAnimation { CYCLE, CYCLE_DRY, EQUIP, INSPECT }
}
