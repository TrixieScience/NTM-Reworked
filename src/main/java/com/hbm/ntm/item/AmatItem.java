package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.FiftyCalAmmoType;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaAmmoType;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.WeaponModManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** Anti-Materiel Rifle. Seven rounds against the concept of matter. */
public class AmatItem extends SednaGunItem {
    public static final int DURABILITY = 350;
    public static final int CAPACITY = 7;
    public static final int DRAW_TICKS = 20;
    public static final int INSPECT_TICKS = 50;
    public static final int FIRE_DELAY = 25;
    public static final int DRY_TICKS = 25;
    public static final int RELOAD_TICKS = 51;
    public static final int JAM_TICKS = 43;
    public static final float BASE_DAMAGE = 30.0F;
    public static final float HIP_SPREAD = 0.05F;
    public static final float MAX_WEAR_SPREAD = 0.125F;

    public static final ResourceLocation SCOPE = ResourceLocation.fromNamespaceAndPath(
            "hbm", "textures/misc/scope_amat.png");

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

    public static final Profile STANDARD = new Profile(
            DURABILITY, BASE_DAMAGE, HIP_SPREAD, FiftyCalAmmoType.SOFT_POINT,
            List.of(FiftyCalAmmoType.SOFT_POINT, FiftyCalAmmoType.FULL_METAL_JACKET,
                    FiftyCalAmmoType.HOLLOW_POINT, FiftyCalAmmoType.ARMOR_PIERCING,
                    FiftyCalAmmoType.DEPLETED_URANIUM, FiftyCalAmmoType.STARMETAL,
                    FiftyCalAmmoType.HIGH_EXPLOSIVE),
            ModSounds.GUN_AMAT_FIRE, "gui.weapon.quality.aside");

    private final Profile profile;

    public AmatItem() {
        this(STANDARD);
    }

    /** Common plumbing for the increasingly ridiculous variants. */
    public AmatItem(Profile profile) {
        this.profile = profile;
    }

    public Profile profile() { return profile; }
    public float baseDamage() { return profile.baseDamage; }

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
            case PRIMARY_RELEASE, SECONDARY, SECONDARY_RELEASE -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    @Override public float gunAimFovMultiplier() { return 0.2F; }
    @Override public ResourceLocation gunScopeTexture() { return SCOPE; }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return profile.durability; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        FiftyCalAmmoType ammo = loadedAmmo(stack);
        return ammo.createStack(ammoIconItem(ammo), 1);
    }
    @Override public float recoilVertical() { return 12.5F; }
    @Override public float recoilHorizontalSigma() { return 1.0F; }

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

    private void decide(LivingEntity living, ItemStack stack, CompoundTag tag, GunState previous) {
        if (previous == GunState.DRAWING || previous == GunState.JAMMED
                || previous == GunState.COOLDOWN) {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        if (!tag.getBoolean(CANCEL_RELOAD)) reloadAction(player, tag);
        tag.putBoolean(CANCEL_RELOAD, false);
        tag.putInt(MAG_AFTER, tag.getInt(MAG_COUNT));
        if (jamChance(tag.getFloat(WEAR), profile.durability) > living.getRandom().nextFloat()) {
            tag.putByte(STATE, (byte) GunState.JAMMED.ordinal());
            tag.putInt(TIMER, JAM_TICKS);
            playAnimation(tag, GunAnimation.JAMMED);
        } else {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
        }
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
        if (rounds(tag) <= 0) {
            playAnimation(tag, GunAnimation.CYCLE_DRY);
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, DRY_TICKS);
            save(stack, tag);
            return;
        }
        fire(player, stack, tag);
        save(stack, tag);
    }

    private void fire(Player player, ItemStack stack, CompoundTag tag) {
        FiftyCalAmmoType ammo = loadedAmmo(tag);
        if (!profile.supportedAmmo.contains(ammo) || rounds(tag) <= 0
                || !(player.level() instanceof ServerLevel level)) return;

        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, profile.durability);
        float damage = profile.baseDamage * wearDamageMultiplier(currentWear, profile.durability)
                * ammo.damageMultiplier();
        boolean aiming = tag.getBoolean(AIMING);
        float spread = ammo.spread() + (aiming ? 0.0F : profile.hipSpread)
                + wearSpread(currentWear, profile.durability);
        Vec3 origin = projectileOrigin(player, aiming);
        Vec3 heading = player.getLookAngle();
        level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading));
        SoundEvent fireSound = WeaponModManager.hasMod(stack, 0, WeaponModManager.SILENCER)
                ? ModSounds.GUN_AMAT_SILENCER.get() : profile.fireSound.get();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), fireSound,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, Math.max(0, rounds(tag) - 1));
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), profile.durability));
        tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
        tag.putInt(TIMER, FIRE_DELAY);
        playAnimation(tag, GunAnimation.CYCLE);
    }

    private void pressReload(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) != GunState.IDLE) return;
        tag.putBoolean(AIMING, false);
        if (canReload(player.getInventory(), tag)) {
            tag.putInt(MAG_PREV, rounds(tag));
            tag.putByte(STATE, (byte) GunState.RELOADING.ordinal());
            tag.putInt(TIMER, RELOAD_TICKS);
            playAnimation(tag, GunAnimation.RELOAD);
        } else {
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, INSPECT_TICKS);
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
        int count = rounds(gun);
        if (count >= CAPACITY) return false;
        FiftyCalAmmoType required = count > 0 ? loadedAmmo(gun) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = rounds(gun);
        FiftyCalAmmoType type = loaded > 0 ? loadedAmmo(gun) : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());
        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!isAmmoStack(candidate) || ammoFromStack(candidate) != type) continue;
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private FiftyCalAmmoType findFirstAmmo(Inventory inventory, FiftyCalAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !isAmmoStack(candidate)) continue;
            FiftyCalAmmoType type = ammoFromStack(candidate);
            if (type != null && profile.supportedAmmo.contains(type)
                    && (required == null || required == type)) return type;
        }
        return null;
    }

    /** Legendary rifles know about the ammunition under the counter. */
    protected boolean isAmmoStack(ItemStack stack) {
        return stack.is(ModItems.AMMO_STANDARD.get());
    }

    protected FiftyCalAmmoType ammoFromStack(ItemStack stack) {
        SednaAmmoType ammo = StandardAmmoTypes.fromStack(stack);
        return ammo instanceof FiftyCalAmmoType fifty ? fifty : null;
    }

    protected Item ammoIconItem(FiftyCalAmmoType ammo) {
        return ModItems.AMMO_STANDARD.get();
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.25D, -0.09375D, 1.0D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity, GunAnimation animation, int timer) {
        if (animation == GunAnimation.EQUIP) {
            if (timer == 10) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.5F, 1.25F);
            if (timer == 15) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.5F, 1.25F);
        } else if (animation == GunAnimation.CYCLE) {
            if (timer == 7) play(level, entity, ModSounds.GUN_BOLT_OPEN.get(), 0.5F, 1.0F);
            if (timer == 16) play(level, entity, ModSounds.GUN_BOLT_CLOSE.get(), 0.5F, 1.0F);
        } else if (animation == GunAnimation.CYCLE_DRY) {
            if (timer == 0) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 1.0F, 0.75F);
            if (timer == 7) play(level, entity, ModSounds.GUN_BOLT_OPEN.get(), 0.5F, 1.0F);
            if (timer == 16) play(level, entity, ModSounds.GUN_BOLT_CLOSE.get(), 0.5F, 1.0F);
        } else if (animation == GunAnimation.RELOAD) {
            if (timer == 2) play(level, entity, ModSounds.GUN_MAG_REMOVE.get(), 1.0F, 1.0F);
            if (timer == 20) play(level, entity, ModSounds.GUN_MAG_INSERT.get(), 1.0F, 1.0F);
            if (timer == 32) play(level, entity, ModSounds.GUN_BOLT_OPEN.get(), 0.5F, 1.0F);
            if (timer == 41) play(level, entity, ModSounds.GUN_BOLT_CLOSE.get(), 0.5F, 1.0F);
        } else if (animation == GunAnimation.JAMMED) {
            if (timer == 5 || timer == 16) play(level, entity, ModSounds.GUN_BOLT_OPEN.get(), 0.5F, 1.0F);
            if (timer == 12 || timer == 23) play(level, entity, ModSounds.GUN_BOLT_CLOSE.get(), 0.5F, 1.0F);
        } else if (animation == GunAnimation.INSPECT) {
            if (timer == 0) play(level, entity, ModSounds.GUN_REVOLVER_COCK.get(), 0.5F, 1.0F);
            if (timer == 45) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.5F, 1.0F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, volume, pitch);
    }

    public static float jamChance(float wear) { return jamChance(wear, DURABILITY); }
    private static float jamChance(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.66F ? 0.0F : Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    public static float wearDamageMultiplier(float wear) {
        return wearDamageMultiplier(wear, DURABILITY);
    }
    private static float wearDamageMultiplier(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.75F ? 1.0F : 1.0F - (percent - 0.75F) * 2.0F;
    }

    public static float wearSpread(float wear) { return wearSpread(wear, DURABILITY); }
    private static float wearSpread(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F * MAX_WEAR_SPREAD;
    }

    public static int rounds(ItemStack stack) { return rounds(data(stack)); }
    private static int rounds(CompoundTag tag) { return Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY); }
    public static float wear(ItemStack stack) {
        return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, profile(stack).durability);
    }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static FiftyCalAmmoType loadedAmmo(ItemStack stack) { return loadedAmmo(data(stack)); }
    private static FiftyCalAmmoType loadedAmmo(CompoundTag tag) {
        return FiftyCalAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    FiftyCalAmmoType ammo, float wear) {
        CompoundTag tag = data(stack);
        tag.putByte(STATE, (byte) state.ordinal());
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, profile(stack).durability));
        tag.putBoolean(EQUIPPED, true);
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
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : GunAnimation.CYCLE;
    }

    private static void playAnimation(CompoundTag tag, GunAnimation animation) {
        tag.putInt(LAST_ANIM, animation.ordinal());
        tag.putInt(ANIM_TIMER, 0);
    }

    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(INITIALIZED)) {
            Profile profile = profile(stack);
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(MAG_TYPE, profile.defaultAmmo.legacyBulletConfig());
            // Default identity, empty magazine. No starter pack.
            tag.putInt(MAG_COUNT, 0);
        }
        return tag;
    }

    private static Profile profile(ItemStack stack) {
        return stack.getItem() instanceof AmatItem gun ? gun.profile : STANDARD;
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        FiftyCalAmmoType ammo = loadedAmmo(stack);
        String family = ammo.secret() ? "ammo_secret." : "ammo_standard.";
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm." + family + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage")
                .append(": " + trimDamage(profile.baseDamage)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                .append(": " + trimDamage(profile.baseDamage * ammo.damageMultiplier()))
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((profile.durability - wear(stack)) * 100.0F
                / profile.durability), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        for (ItemStack mod : WeaponModManager.installedMods(stack, 0)) {
            tooltip.add(mod.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable(profile.quality).withStyle(
                "gui.weapon.quality.legendary".equals(profile.quality)
                        ? ChatFormatting.RED : ChatFormatting.YELLOW));
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) return Integer.toString(Math.round(damage));
        return String.format(Locale.ROOT, "%.3f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public record Profile(int durability, float baseDamage, float hipSpread,
                          FiftyCalAmmoType defaultAmmo, List<FiftyCalAmmoType> supportedAmmo,
                          Supplier<SoundEvent> fireSound, String quality) {
        public Profile {
            supportedAmmo = List.copyOf(supportedAmmo);
        }
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    /** Serialized ordinals. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
