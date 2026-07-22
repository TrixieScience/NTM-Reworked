package com.hbm.ntm.item;

import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.network.GunEffectPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import com.hbm.ntm.weapon.GunInput;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** Source {@code gun_g3} and {@code gun_g3_zebra} automatic/semi-automatic 5.56 mm receivers. */
public final class G3Item extends SednaGunItem {
    public static final int DURABILITY = 3_000;
    public static final int CAPACITY = 30;
    public static final int DRAW_TICKS = 10;
    public static final int INSPECT_TICKS = 33;
    public static final int FIRE_DELAY = 2;
    public static final int DRY_TICKS = 15;
    public static final int RELOAD_TICKS = 50;
    public static final int JAM_TICKS = 47;
    public static final float BASE_DAMAGE = 5.0F;
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
    private static final String PRIMARY_HELD = "primary0";
    private static final String CANCEL_RELOAD = "cancel";
    private static final String MODE = "mode0";
    private static final String EQUIPPED = "eqipped";
    private static final String LAST_ANIM = "lastanim_0";
    private static final String ANIM_TIMER = "animtimer_0";

    private static final ResourceLocation ZEBRA_SCOPE = ResourceLocation.fromNamespaceAndPath(
            "hbm", "textures/misc/scope_bolt.png");

    private final Variant variant;

    public G3Item() {
        this(Variant.STANDARD);
    }

    public G3Item(Variant variant) {
        this.variant = variant;
    }

    public Variant variant() { return variant; }
    public float baseDamage() { return variant.baseDamage; }

    @Override
    protected void handleGunInput(Player player, ItemStack stack, GunInput input) {
        switch (input) {
            case PRIMARY -> pressPrimary(player, stack);
            case PRIMARY_RELEASE -> releasePrimary(stack);
            case SECONDARY -> switchMode(player, stack);
            case RELOAD -> pressReload(player, stack);
            case TOGGLE_AIM -> toggleAim(stack);
            case SECONDARY_RELEASE -> { }
        }
    }

    @Override public boolean gunAiming(ItemStack stack) { return aiming(stack); }
    @Override public boolean gunAutomatic() { return true; }
    @Override public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    @Override public float gunAimFovMultiplier() { return variant.aimFovMultiplier; }
    @Override public float gunAimFovMultiplier(ItemStack stack) {
        return isScoped(stack) ? 0.34F : variant.aimFovMultiplier;
    }
    @Override public ResourceLocation gunScopeTexture() {
        return variant == Variant.ZEBRA ? ZEBRA_SCOPE : null;
    }
    @Override public ResourceLocation gunScopeTexture(ItemStack stack) {
        return isScoped(stack) ? ZEBRA_SCOPE : null;
    }
    @Override public int gunRounds(ItemStack stack) { return rounds(stack); }
    @Override public int gunCapacity() { return CAPACITY; }
    @Override public float gunWear(ItemStack stack) { return wear(stack); }
    @Override public float gunDurability() { return variant.durability; }
    @Override public ItemStack gunAmmoIcon(ItemStack stack) {
        return loadedAmmo(stack).createStack(ModItems.AMMO_STANDARD.get(), 1);
    }
    @Override public float recoilVertical() { return 0.0F; }
    @Override public float recoilVerticalSigma() { return variant.recoilSigma; }
    @Override public float recoilHorizontalSigma() { return variant.recoilSigma; }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof LivingEntity living) || level.isClientSide) return;

        boolean held = selected && living.getMainHandItem() == stack;
        CompoundTag tag = data(stack);
        GunState previous = state(tag);
        if (!held) {
            if (previous != GunState.JAMMED) {
                tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
                tag.putInt(TIMER, drawTicks(stack));
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
        if (timer <= 1) decide(living, stack, tag, previous);
        save(stack, tag);
    }

    private static void decide(LivingEntity living, ItemStack stack, CompoundTag tag, GunState previous) {
        if (previous == GunState.DRAWING || previous == GunState.JAMMED) {
            tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
            tag.putInt(TIMER, 0);
            return;
        }
        if (previous == GunState.COOLDOWN) {
            if (tag.getByte(MODE) == 0 && tag.getBoolean(PRIMARY_HELD)
                    && living instanceof Player player && rounds(tag) > 0) {
                ((G3Item) stack.getItem()).fire(player, stack, tag);
            } else {
                tag.putByte(STATE, (byte) GunState.IDLE.ordinal());
                tag.putInt(TIMER, 0);
            }
            return;
        }
        if (previous != GunState.RELOADING || !(living instanceof Player player)) return;

        reloadAction(player, tag);
        tag.putBoolean(CANCEL_RELOAD, false);
        tag.putInt(MAG_AFTER, tag.getInt(MAG_COUNT));
        G3Item gun = (G3Item) stack.getItem();
        if (jamChance(tag.getFloat(WEAR), gun.variant.durability) > living.getRandom().nextFloat()) {
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
            tag.putByte(STATE, (byte) GunState.DRAWING.ordinal());
            tag.putInt(TIMER, DRY_TICKS);
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

    private static void switchMode(Player player, ItemStack stack) {
        CompoundTag tag = data(stack);
        if (state(tag) == GunState.IDLE) {
            int oldMode = tag.getByte(MODE) == 0 ? 0 : 1;
            tag.putByte(MODE, (byte) (1 - oldMode));
            SoundEvent sound = oldMode == 0 ? ModSounds.GUN_SWITCH_MODE_1.get() : ModSounds.GUN_SWITCH_MODE_2.get();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        save(stack, tag);
    }

    private void fire(Player player, ItemStack stack, CompoundTag tag) {
        FiveFiveSixAmmoType ammo = FiveFiveSixAmmoType.fromLegacyBulletConfig(tag.getInt(MAG_TYPE));
        if (rounds(tag) <= 0 || !(player.level() instanceof ServerLevel level)) return;

        float currentWear = Mth.clamp(tag.getFloat(WEAR), 0.0F, variant.durability);
        float damage = variant.baseDamage
                * wearDamageMultiplier(currentWear, variant.durability) * ammo.damageMultiplier();
        boolean aiming = tag.getBoolean(AIMING);
        float spread = ammo.spread() + (aiming ? 0.0F : variant.hipSpread)
                + wearSpread(currentWear, variant.durability);
        Vec3 origin = projectileOrigin(player, aiming);
        Vec3 heading = player.getLookAngle();
        level.addFreshEntity(new BulletEntity(level, player, ammo, damage, spread, origin, heading,
                variant.incendiary));
        SoundEvent fireSound = WeaponModManager.hasMod(stack, 0, WeaponModManager.SILENCER)
                ? ModSounds.GUN_RIFLE_SILENCER.get() : variant.fireSound.get();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), fireSound,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection.getConnection().isConnected()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    GunEffectPayload.fired(player.getId(), origin, heading, false));
        }

        tag.putInt(MAG_COUNT, Math.max(0, rounds(tag) - 1));
        tag.putFloat(WEAR, Math.min(currentWear + ammo.wear(), variant.durability));
        tag.putByte(STATE, (byte) GunState.COOLDOWN.ordinal());
        tag.putInt(TIMER, FIRE_DELAY);
        playAnimation(tag, GunAnimation.CYCLE);
    }

    private static void pressReload(Player player, ItemStack stack) {
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

    private static boolean canReload(Inventory inventory, CompoundTag gun) {
        int count = rounds(gun);
        if (count >= CAPACITY) return false;
        FiveFiveSixAmmoType required = count > 0
                ? FiveFiveSixAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE)) : null;
        return findFirstAmmo(inventory, required) != null;
    }

    private static void reloadAction(Player player, CompoundTag gun) {
        Inventory inventory = player.getInventory();
        int loaded = rounds(gun);
        FiveFiveSixAmmoType type = loaded > 0
                ? FiveFiveSixAmmoType.fromLegacyBulletConfig(gun.getInt(MAG_TYPE))
                : findFirstAmmo(inventory, null);
        if (type == null) return;
        if (loaded == 0) gun.putInt(MAG_TYPE, type.legacyBulletConfig());
        for (int slot = 0; slot < inventory.getContainerSize() && loaded < CAPACITY; slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (!candidate.is(ModItems.AMMO_STANDARD.get()) || StandardAmmoTypes.fromStack(candidate) != type) {
                continue;
            }
            int consumed = Math.min(CAPACITY - loaded, candidate.getCount());
            candidate.shrink(consumed);
            loaded += consumed;
        }
        gun.putInt(MAG_COUNT, loaded);
    }

    private static FiveFiveSixAmmoType findFirstAmmo(Inventory inventory, FiveFiveSixAmmoType required) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !candidate.is(ModItems.AMMO_STANDARD.get())) continue;
            if (!(StandardAmmoTypes.fromStack(candidate) instanceof FiveFiveSixAmmoType type)) continue;
            if (required == null || required == type) return type;
        }
        return null;
    }

    private static Vec3 projectileOrigin(Player player, boolean aiming) {
        Vec3 local = new Vec3(aiming ? 0.0D : -0.25D, -0.15625D, 1.0D);
        Vec3 offset = local.xRot(-player.getXRot() * Mth.DEG_TO_RAD).yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        return player.getEyePosition().add(offset);
    }

    private static void playOrchestra(Level level, LivingEntity entity, GunAnimation animation, int timer) {
        if (animation == GunAnimation.CYCLE_DRY) {
            if (timer == 0) play(level, entity, ModSounds.GUN_DRY_FIRE.get(), 0.8F);
            if (timer == 5) play(level, entity, ModSounds.GUN_PISTOL_COCK.get(), 0.9F);
        } else if (animation == GunAnimation.RELOAD) {
            if (timer == 2) play(level, entity, ModSounds.GUN_MAG_REMOVE.get(), 1.0F);
            if (timer == 4) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.9F);
            if (timer == 32) play(level, entity, ModSounds.GUN_MAG_INSERT.get(), 1.0F);
            if (timer == 36) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F);
        } else if (animation == GunAnimation.INSPECT) {
            if (timer == 2) play(level, entity, ModSounds.GUN_MAG_REMOVE.get(), 1.0F);
            if (timer == 28) play(level, entity, ModSounds.GUN_MAG_INSERT.get(), 1.0F);
        } else if (animation == GunAnimation.JAMMED) {
            if (timer == 16 || timer == 26) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 0.9F);
            if (timer == 20 || timer == 30) play(level, entity, ModSounds.GUN_REVOLVER_CLOSE.get(), 1.0F);
        }
    }

    private static void play(Level level, LivingEntity entity, SoundEvent sound, float pitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1.0F, pitch);
    }

    public static float jamChance(float wear) {
        return jamChance(wear, DURABILITY);
    }

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

    public static float wearSpread(float wear) {
        return wearSpread(wear, DURABILITY);
    }

    public static boolean hasStock(ItemStack stack) {
        return !WeaponModManager.hasMod(stack, 0, WeaponModManager.NO_STOCK);
    }

    public static int drawTicks(ItemStack stack) { return hasStock(stack) ? DRAW_TICKS : 5; }

    private static float wearSpread(float wear, float durability) {
        float percent = wear / durability;
        return percent < 0.5F ? 0.0F : (percent - 0.5F) * 2.0F * MAX_WEAR_SPREAD;
    }

    public static int rounds(ItemStack stack) { return rounds(data(stack)); }
    private static int rounds(CompoundTag tag) { return Mth.clamp(tag.getInt(MAG_COUNT), 0, CAPACITY); }
    public static float wear(ItemStack stack) {
        return Mth.clamp(data(stack).getFloat(WEAR), 0.0F, variant(stack).durability);
    }
    public static boolean aiming(ItemStack stack) { return data(stack).getBoolean(AIMING); }
    public static boolean primaryHeld(ItemStack stack) { return data(stack).getBoolean(PRIMARY_HELD); }
    public static int mode(ItemStack stack) { return data(stack).getByte(MODE) == 0 ? 0 : 1; }
    public static GunState state(ItemStack stack) { return state(data(stack)); }
    public static int timer(ItemStack stack) { return data(stack).getInt(TIMER); }
    public static GunAnimation animation(ItemStack stack) { return animation(data(stack)); }
    public static int animationTimer(ItemStack stack) { return data(stack).getInt(ANIM_TIMER); }
    public static int amountBeforeReload(ItemStack stack) { return data(stack).getInt(MAG_PREV); }
    public static FiveFiveSixAmmoType loadedAmmo(ItemStack stack) {
        return FiveFiveSixAmmoType.fromLegacyBulletConfig(data(stack).getInt(MAG_TYPE));
    }

    public static void setTestState(ItemStack stack, GunState state, int timer, int rounds,
                                    FiveFiveSixAmmoType ammo, float wear, boolean primaryHeld, int mode) {
        CompoundTag tag = data(stack);
        tag.putByte(STATE, (byte) state.ordinal());
        tag.putInt(TIMER, timer);
        tag.putInt(MAG_COUNT, Mth.clamp(rounds, 0, CAPACITY));
        tag.putInt(MAG_TYPE, ammo.legacyBulletConfig());
        tag.putFloat(WEAR, Mth.clamp(wear, 0.0F, variant(stack).durability));
        tag.putBoolean(PRIMARY_HELD, primaryHeld);
        tag.putByte(MODE, (byte) (mode == 0 ? 0 : 1));
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
        if (!tag.getBoolean(INITIALIZED) && stack.getItem() instanceof G3Item gun) {
            tag.putBoolean(INITIALIZED, true);
            tag.putInt(MAG_TYPE, gun.variant.defaultAmmo.legacyBulletConfig());
            // setDefaultAmmo describes the loose-ammo container. Source magazines still spawn empty.
            tag.putInt(MAG_COUNT, 0);
            tag.putByte(MODE, (byte) 0);
        }
        return tag;
    }

    private static void save(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FiveFiveSixAmmoType ammo = loadedAmmo(stack);
        tooltip.add(Component.translatable("gui.weapon.ammo").append(": ")
                .append(Component.translatable("item.hbm.ammo_standard." + ammo.serializedName()))
                .append(" " + rounds(stack) + " / " + CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.baseDamage")
                .append(": " + trimDamage(variant.baseDamage))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.weapon.damageWithAmmo")
                .append(": " + trimDamage(variant.baseDamage * ammo.damageMultiplier()))
                .withStyle(ChatFormatting.GRAY));
        int condition = Mth.clamp((int) ((variant.durability - wear(stack)) * 100.0F
                / variant.durability), 0, 100);
        tooltip.add(Component.translatable("gui.weapon.condition").append(": " + condition + "%")
                .withStyle(ChatFormatting.GRAY));
        for (ItemStack mod : WeaponModManager.installedMods(stack, 0)) {
            tooltip.add(mod.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable(variant.quality).withStyle(ChatFormatting.YELLOW));
    }

    private static String trimDamage(float damage) {
        if (Math.abs(damage - Math.round(damage)) < 0.0001F) return Integer.toString(Math.round(damage));
        return String.format(Locale.ROOT, "%.3f", damage).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public boolean isScoped(ItemStack stack) {
        return variant == Variant.ZEBRA || WeaponModManager.hasMod(stack, 0, WeaponModManager.SCOPE);
    }

    private static Variant variant(ItemStack stack) {
        return stack.getItem() instanceof G3Item gun ? gun.variant : Variant.STANDARD;
    }

    public enum Variant {
        STANDARD(3_000, 5.0F, 0.025F, 0.25F, 0.67F,
                FiveFiveSixAmmoType.SOFT_POINT, false, ModSounds.GUN_ASSAULT_FIRE,
                "gui.weapon.quality.aside"),
        ZEBRA(6_000, 7.5F, 0.01F, 0.125F, 0.34F,
                FiveFiveSixAmmoType.HOLLOW_POINT, true, ModSounds.GUN_RIFLE_SILENCER,
                "gui.weapon.quality.bside");

        private final int durability;
        private final float baseDamage;
        private final float hipSpread;
        private final float recoilSigma;
        private final float aimFovMultiplier;
        private final FiveFiveSixAmmoType defaultAmmo;
        private final boolean incendiary;
        private final Supplier<SoundEvent> fireSound;
        private final String quality;

        Variant(int durability, float baseDamage, float hipSpread, float recoilSigma,
                float aimFovMultiplier, FiveFiveSixAmmoType defaultAmmo, boolean incendiary,
                Supplier<SoundEvent> fireSound, String quality) {
            this.durability = durability;
            this.baseDamage = baseDamage;
            this.hipSpread = hipSpread;
            this.recoilSigma = recoilSigma;
            this.aimFovMultiplier = aimFovMultiplier;
            this.defaultAmmo = defaultAmmo;
            this.incendiary = incendiary;
            this.fireSound = fireSound;
            this.quality = quality;
        }

        public int durability() { return durability; }
        public float baseDamage() { return baseDamage; }
        public float hipSpread() { return hipSpread; }
        public float recoilSigma() { return recoilSigma; }
        public float aimFovMultiplier() { return aimFovMultiplier; }
        public FiveFiveSixAmmoType defaultAmmo() { return defaultAmmo; }
        public boolean incendiary() { return incendiary; }
    }

    public enum GunState { DRAWING, IDLE, COOLDOWN, RELOADING, JAMMED }

    /** Enum order is animation protocol. Do not alphabetize. */
    public enum GunAnimation {
        RELOAD, RELOAD_CYCLE, RELOAD_END, CYCLE, CYCLE_EMPTY, CYCLE_DRY,
        ALT_CYCLE, SPINUP, SPINDOWN, EQUIP, INSPECT, JAMMED
    }
}
