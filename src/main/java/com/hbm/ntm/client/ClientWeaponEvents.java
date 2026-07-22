package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.config.HbmConfig;
import com.hbm.ntm.item.LaserDetonatorItem;
import com.hbm.ntm.item.FlamerGunItem;
import com.hbm.ntm.item.FireExtinguisherItem;
import com.hbm.ntm.item.DrillItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.StingerLauncherItem;
import com.hbm.ntm.item.TauGunItem;
import com.hbm.ntm.client.sound.FlamerSoundInstance;
import com.hbm.ntm.client.sound.FireExtinguisherSoundInstance;
import com.hbm.ntm.client.sound.DrillSoundInstance;
import com.hbm.ntm.client.sound.StingerLockSoundInstance;
import com.hbm.ntm.client.sound.TauChargeSoundInstance;
import com.hbm.ntm.network.GunInputPayload;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ClientWeaponEvents {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/misc/overlay_misc.png");
    public static final KeyMapping RELOAD = new KeyMapping(
            "key.hbm.reload", GLFW.GLFW_KEY_R, "key.categories.hbm");
    public static final KeyMapping AIM = new KeyMapping(
            "key.hbm.aim", InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.hbm");

    private static final Map<Integer, Long> LAST_SHOT = new HashMap<>();
    private static final Map<ItemStack, long[]> LAST_SHOT_BY_STACK = new WeakHashMap<>();
    private static final Map<ItemStack, float[]> SHOT_RANDOM_BY_STACK = new WeakHashMap<>();
    private static float previousAimingProgress;
    private static float aimingProgress;
    private static float recoilVertical;
    private static float recoilHorizontal;
    private static float offsetVertical;
    private static float offsetHorizontal;
    private static boolean automaticTriggerHeld;
    private static boolean automaticSecondaryTriggerHeld;
    private static Component detonatorInfo;
    private static long detonatorInfoStart;
    private static int detonatorInfoDuration;

    private ClientWeaponEvents() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClientWeaponEvents::interaction);
        NeoForge.EVENT_BUS.addListener(ClientWeaponEvents::clientTick);
        NeoForge.EVENT_BUS.addListener(ClientWeaponEvents::fov);
        // Detector text survives other overlays canceling the crosshair layer.
        NeoForge.EVENT_BUS.addListener(true, ClientWeaponEvents::crosshair);
        NeoForge.EVENT_BUS.addListener(ClientWeaponEvents::scope);
        NeoForge.EVENT_BUS.addListener(ClientWeaponEvents::hotbar);
    }

    private static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !HbmConfig.ENABLE_GUNS.get()
                || !(minecraft.player.getMainHandItem().getItem() instanceof SednaGunItem gun)) return;

        if (event.isAttack()) {
            PacketDistributor.sendToServer(new GunInputPayload(GunInput.PRIMARY));
            if (gun.gunAutomatic()) automaticTriggerHeld = true;
            event.setSwingHand(false);
            event.setCanceled(true);
        } else if (event.isUseItem() && event.getHand() == InteractionHand.MAIN_HAND) {
            PacketDistributor.sendToServer(new GunInputPayload(GunInput.SECONDARY));
            if (gun.gunSecondaryAutomatic()) automaticSecondaryTriggerHeld = true;
            event.setSwingHand(false);
            event.setCanceled(true);
        } else if (event.isPickBlock()) {
            // Shared Pick Block/tertiary binding: consume aim after Minecraft records the click,
            // or the hotbar changes before the gun packet leaves.
            if (AIM.same(minecraft.options.keyPickItem)) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    private static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            automaticTriggerHeld = false;
            automaticSecondaryTriggerHeld = false;
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        SednaGunItem gun = held.getItem() instanceof SednaGunItem sedna ? sedna : null;
        if (held.getItem() instanceof StingerLauncherItem
                && StingerLauncherItem.lockProgress(held) > 0
                && !StingerLauncherItem.lockedOn(held)) {
            StingerLockSoundInstance.keepAlive(minecraft.player);
        }
        if (held.getItem() instanceof TauGunItem
                && TauGunItem.animation(held) == TauGunItem.GunAnimation.SPINUP
                && TauGunItem.animationTimer(held) < 300) {
            TauChargeSoundInstance.keepAlive(minecraft.player, TauGunItem.animationTimer(held));
        }

        boolean automatic = gun != null && gun.gunAutomatic() && HbmConfig.ENABLE_GUNS.get();
        boolean attackDown = minecraft.screen == null && minecraft.options.keyAttack.isDown();
        if (automatic && attackDown && !automaticTriggerHeld) {
            PacketDistributor.sendToServer(new GunInputPayload(GunInput.PRIMARY));
            automaticTriggerHeld = true;
        } else if (automaticTriggerHeld && (!automatic || !attackDown)) {
            if (gun != null && gun.gunAutomatic()) {
                PacketDistributor.sendToServer(new GunInputPayload(GunInput.PRIMARY_RELEASE));
            }
            automaticTriggerHeld = false;
        }

        boolean secondaryAutomatic = gun != null && gun.gunSecondaryAutomatic()
                && HbmConfig.ENABLE_GUNS.get();
        boolean useDown = minecraft.screen == null && minecraft.options.keyUse.isDown();
        if (secondaryAutomatic && useDown && !automaticSecondaryTriggerHeld) {
            PacketDistributor.sendToServer(new GunInputPayload(GunInput.SECONDARY));
            automaticSecondaryTriggerHeld = true;
        } else if (automaticSecondaryTriggerHeld && (!secondaryAutomatic || !useDown)) {
            if (gun != null && gun.gunSecondaryAutomatic()) {
                PacketDistributor.sendToServer(new GunInputPayload(GunInput.SECONDARY_RELEASE));
            }
            automaticSecondaryTriggerHeld = false;
        }

        while (RELOAD.consumeClick()) {
            if (gun != null && HbmConfig.ENABLE_GUNS.get()) {
                PacketDistributor.sendToServer(new GunInputPayload(GunInput.RELOAD));
            }
        }
        while (AIM.consumeClick()) {
            if (gun != null && minecraft.screen == null && HbmConfig.ENABLE_GUNS.get()) {
                PacketDistributor.sendToServer(new GunInputPayload(GunInput.TOGGLE_AIM));
            }
        }

        previousAimingProgress = aimingProgress;
        boolean aiming = gun != null && gun.gunAiming(held);
        if (aiming && aimingProgress < 1.0F) aimingProgress += 0.25F;
        if (!aiming && aimingProgress > 0.0F) aimingProgress -= 0.25F;
        aimingProgress = net.minecraft.util.Mth.clamp(aimingProgress, 0.0F, 1.0F);

        offsetVertical += recoilVertical;
        offsetHorizontal += recoilHorizontal;
        minecraft.player.setXRot(minecraft.player.getXRot() - recoilVertical);
        minecraft.player.setYRot(minecraft.player.getYRot() - recoilHorizontal);
        recoilVertical *= 0.75F;
        recoilHorizontal *= 0.75F;
        float reboundVertical = offsetVertical * 0.25F;
        float reboundHorizontal = offsetHorizontal * 0.25F;
        offsetVertical -= reboundVertical;
        offsetHorizontal -= reboundHorizontal;
        minecraft.player.setXRot(minecraft.player.getXRot() + reboundVertical);
        minecraft.player.setYRot(minecraft.player.getYRot() + reboundHorizontal);
    }

    private static void fov(ComputeFovModifierEvent event) {
        if (!(event.getPlayer().getMainHandItem().getItem() instanceof SednaGunItem gun)) return;
        event.setNewFovModifier(event.getNewFovModifier()
                * Mth.lerp(aimingProgress, 1.0F,
                        gun.gunAimFovMultiplier(event.getPlayer().getMainHandItem())));
    }

    private static void crosshair(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        renderDetonatorInfo(event.getGuiGraphics(), minecraft);
        ItemStack held = minecraft.player.getMainHandItem();
        boolean laserDetonator = held.getItem() instanceof LaserDetonatorItem;
        SednaGunItem gun = held.getItem() instanceof SednaGunItem sedna ? sedna : null;
        if (!laserDetonator && gun == null) return;
        event.setCanceled(true);
        if (!HbmConfig.ENABLE_CROSSHAIRS.get()
                || (gun != null && gun.gunCrosshairOnlyWhenAimed() && aimingProgress < 1.0F)
                || (gun != null && gun.gunHideCrosshairWhenAimed(held) && aimingProgress >= 1.0F)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        SednaCrosshair crosshair = laserDetonator ? SednaCrosshair.L_ARROWS : gun.gunCrosshair();
        if (crosshair == SednaCrosshair.NONE) return;
        int size = crosshair.size();
        float textureX = crosshair.textureX();
        float textureY = crosshair.textureY();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        graphics.blit(OVERLAY, graphics.guiWidth() / 2 - size / 2, graphics.guiHeight() / 2 - size / 2,
                textureX, textureY, size, size, 256, 256);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.disableBlend();
        if (held.getItem() instanceof StingerLauncherItem) {
            renderStingerLock(graphics, held);
        }
    }

    private static void renderStingerLock(GuiGraphics graphics, ItemStack stack) {
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2;
        int progress = Mth.clamp((int) (StingerLauncherItem.lockProgress(stack)
                * 28.0F / StingerLauncherItem.LOCK_TICKS), 0, 28);
        graphics.blit(OVERLAY, x - 15, y + 18, 146.0F, 18.0F,
                30, 10, 256, 256);
        if (progress > 0) {
            graphics.blit(OVERLAY, x - 14, y + 19, 147.0F, 29.0F,
                    progress, 8, 256, 256);
        }
    }

    /** Channel eight: the newest detonator reading eats the previous one. */
    public static void showDetonatorInfo(Component message, int durationMillis) {
        detonatorInfo = message;
        detonatorInfoStart = System.currentTimeMillis();
        detonatorInfoDuration = Math.max(1, durationMillis);
    }

    private static void renderDetonatorInfo(GuiGraphics graphics, Minecraft minecraft) {
        if (detonatorInfo == null) return;
        long elapsed = System.currentTimeMillis() - detonatorInfoStart;
        if (elapsed >= detonatorInfoDuration) {
            detonatorInfo = null;
            return;
        }

        int width = minecraft.font.width(detonatorInfo);
        int alpha = Math.max(Math.min((int) (510L * (detonatorInfoDuration - elapsed)
                / detonatorInfoDuration), 255), 5);
        int mode = HbmClientConfig.INFO_POSITION.get();
        int x = mode == 0 ? 15
                : mode == 1 ? graphics.guiWidth() - width - 15
                : mode == 2 ? graphics.guiWidth() / 2 + 7
                : graphics.guiWidth() / 2 - width - 6;
        int y = mode == 0 || mode == 1 ? 15 : graphics.guiHeight() / 2 + 7;
        x += HbmClientConfig.INFO_OFFSET_HORIZONTAL.get();
        y += HbmClientConfig.INFO_OFFSET_VERTICAL.get();

        graphics.fill(x - 5, y - 5, x + 5 + width, y + 12, 0x80404040);
        graphics.drawString(minecraft.font, detonatorInfo, x, y,
                (alpha << 24) | 0x00FFFFFF, false);
    }

    private static void hotbar(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof SednaGunItem gun)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2 + 98;
        int ammoY = graphics.guiHeight() - 23;
        graphics.renderItem(gun.gunAmmoIcon(stack), x, ammoY);
        if (gun.gunShowAmmoCounter()) {
            String ammoText = gun.gunBeltFed()
                    ? "x" + gun.gunRounds(stack)
                    : gun.gunRounds(stack) + " / " + gun.gunCapacity();
            graphics.drawString(minecraft.font, ammoText, x + 17, ammoY + 6, 0xFFFFFF, false);
        }

        int barY = graphics.guiHeight() - 5;
        // Broken Maresleg has zero durability. Do not draw a bar by dividing by zero.
        if (gun.gunShowDurability()) {
            int used = (int) (50.0F * gun.gunWear(stack) / gun.gunDurability(stack));
            graphics.blit(OVERLAY, x, barY, 94.0F, 0.0F, 52, 3, 256, 256);
            graphics.blit(OVERLAY, x + 1, barY, 95.0F, 3.0F, 50 - used, 3, 256, 256);
        }

        if (gun.gunHasMirroredHud()) {
            int mirroredX = graphics.guiWidth() / 2 - 150;
            graphics.renderItem(gun.gunMirroredAmmoIcon(stack), mirroredX, ammoY);
            String mirroredAmmo = gun.gunBeltFed()
                    ? "x" + gun.gunMirroredRounds(stack)
                    : gun.gunMirroredRounds(stack) + " / " + gun.gunMirroredCapacity();
            graphics.drawString(minecraft.font, mirroredAmmo,
                    mirroredX + 17, ammoY + 6, 0xFFFFFF, false);

            if (gun.gunShowMirroredDurability()) {
                int mirroredUsed = (int) (50.0F * gun.gunMirroredWear(stack)
                        / gun.gunMirroredDurability());
                graphics.blit(OVERLAY, mirroredX, barY, 94.0F, 0.0F, 52, 3, 256, 256);
                graphics.blit(OVERLAY, mirroredX + 1, barY, 95.0F, 3.0F,
                        50 - mirroredUsed, 3, 256, 256);
            }
        }
    }

    private static void scope(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR) || !fullyAimed()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !(minecraft.player.getMainHandItem().getItem() instanceof SednaGunItem gun)) return;
        ResourceLocation texture = gun.gunScopeTexture(minecraft.player.getMainHandItem());
        if (texture != null) renderScope(event.getGuiGraphics(), texture);
    }

    /** Crops the square texture back to the 16:9 sight hiding inside it. */
    private static void renderScope(GuiGraphics graphics, ResourceLocation texture) {
        double width = graphics.guiWidth();
        double height = graphics.guiHeight();
        double divisor = Math.min(width, height) / (9.0D / 16.0D);
        double smallest = 9.0D / 16.0D;
        double largest = Math.max(width, height) / divisor;
        double uMin = width < height ? 0.5D - smallest / 2.0D : 0.5D - largest / 2.0D;
        double uMax = width < height ? 0.5D + smallest / 2.0D : 0.5D + largest / 2.0D;
        double vMin = height < width ? 0.5D - smallest / 2.0D : 0.5D - largest / 2.0D;
        double vMax = height < width ? 0.5D + smallest / 2.0D : 0.5D + largest / 2.0D;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(texture, 0, 0, graphics.guiWidth(), graphics.guiHeight(),
                (float) (uMin * 320.0D), (float) (vMin * 320.0D),
                Math.max(1, (int) Math.round((uMax - uMin) * 320.0D)),
                Math.max(1, (int) Math.round((vMax - vMin) * 320.0D)), 320, 320);
        RenderSystem.disableBlend();
    }

    public static void onGunFired(int shooterId, int receiverIndex) {
        long now = System.currentTimeMillis();
        LAST_SHOT.put(shooterId, now);
        Minecraft minecraft = Minecraft.getInstance();
        float shotRandom = minecraft.level == null ? 0.0F : minecraft.level.random.nextFloat();
        if (minecraft.level != null && minecraft.level.getEntity(shooterId) instanceof LivingEntity shooter) {
            rememberGunStack(shooter.getMainHandItem(), receiverIndex, now, shotRandom);
            rememberGunStack(shooter.getOffhandItem(), receiverIndex, now, shotRandom);
            if (shooter.getMainHandItem().getItem() instanceof FlamerGunItem flamer
                    && flamer.variant() != FlamerGunItem.Variant.DAYBREAKER) {
                FlamerSoundInstance.keepAlive(shooter);
            }
            if (shooter.getMainHandItem().getItem() instanceof FireExtinguisherItem) {
                FireExtinguisherSoundInstance.keepAlive(shooter);
            }
            if (shooter.getMainHandItem().getItem() instanceof DrillItem) {
                DrillSoundInstance.keepAlive(shooter);
            }
        }
        if (minecraft.player != null && minecraft.player.getId() == shooterId
                && minecraft.player.getMainHandItem().getItem() instanceof SednaGunItem gun) {
            recoilVertical += gun.recoilVertical()
                    + (float) (minecraft.player.getRandom().nextGaussian() * gun.recoilVerticalSigma());
            recoilHorizontal += (float) (minecraft.player.getRandom().nextGaussian() * gun.recoilHorizontalSigma());
        }
    }

    public static long lastShot(int entityId) {
        return LAST_SHOT.getOrDefault(entityId, -1L);
    }

    public static long lastShot(ItemStack stack) {
        return lastShot(stack, 0);
    }

    public static long lastShot(ItemStack stack, int receiverIndex) {
        long[] shots = LAST_SHOT_BY_STACK.get(stack);
        return shots == null || receiverIndex < 0 || receiverIndex >= shots.length
                ? -1L : shots[receiverIndex];
    }

    public static float shotRandom(ItemStack stack) {
        return shotRandom(stack, 0);
    }

    public static float shotRandom(ItemStack stack, int receiverIndex) {
        float[] values = SHOT_RANDOM_BY_STACK.get(stack);
        return values == null || receiverIndex < 0 || receiverIndex >= values.length
                ? 0.0F : values[receiverIndex];
    }

    private static void rememberGunStack(ItemStack stack, int receiverIndex,
                                         long timestamp, float shotRandom) {
        if (stack.getItem() instanceof SednaGunItem) {
            int size = Math.max(2, receiverIndex + 1);
            long[] shots = LAST_SHOT_BY_STACK.computeIfAbsent(stack, ignored -> {
                long[] created = new long[size];
                java.util.Arrays.fill(created, -1L);
                return created;
            });
            float[] randoms = SHOT_RANDOM_BY_STACK.computeIfAbsent(stack, ignored -> new float[size]);
            if (receiverIndex >= shots.length) {
                long[] grown = java.util.Arrays.copyOf(shots, size);
                java.util.Arrays.fill(grown, shots.length, grown.length, -1L);
                shots = grown;
                LAST_SHOT_BY_STACK.put(stack, shots);
            }
            if (receiverIndex >= randoms.length) {
                randoms = java.util.Arrays.copyOf(randoms, size);
                SHOT_RANDOM_BY_STACK.put(stack, randoms);
            }
            shots[receiverIndex] = timestamp;
            randoms[receiverIndex] = shotRandom;
        }
    }

    public static float aimingProgress(float partialTick) {
        return previousAimingProgress + (aimingProgress - previousAimingProgress) * partialTick;
    }

    public static boolean fullyAimed() {
        return previousAimingProgress >= 1.0F && aimingProgress >= 1.0F;
    }
}
