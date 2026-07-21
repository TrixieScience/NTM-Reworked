package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.LaserPistolItem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class LaserPistolItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/laser_pistol.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/laser_pistol.png");
    private static final ResourceLocation PEW_PEW_TEXTURE = id(
            "textures/models/weapons/laser_pistol_pew_pew.png");
    private static final ResourceLocation MORNING_GLORY_TEXTURE = id(
            "textures/models/weapons/laser_pistol_morning_glory.png");
    private static final ResourceLocation FLASH_TEXTURE = id("textures/models/weapons/laser_flash.png");
    private static final Set<String> GROUPS = Set.of("Gun", "Battery", "Latch", "Capacitors", "Tape");
    private static final RenderType FLASH_TYPE = RenderType.create(
            "hbm_laser_pistol_flash", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
            false, true, RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(FLASH_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private EnvsuitMesh mesh;

    public LaserPistolItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof LaserPistolItem pistol)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = first || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);

        poses.pushPose();
        setupContext(context, poses);
        if (first) renderFirstPerson(stack, pistol, poses, buffers, light, overlay);
        else renderStatic(pistol, poses, buffers, light, overlay);
        if (held && elapsed >= 0L && elapsed < 150L) {
            renderFlash(poses, buffers, elapsed / 150.0F,
                    pistol.variant() == LaserPistolItem.Variant.MORNING_GLORY);
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, LaserPistolItem pistol, PoseStack poses,
                                   MultiBufferSource buffers, int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float time = (LaserPistolItem.animationTimer(stack) + partial) * 50.0F;
        LaserPistolItem.GunAnimation animation = LaserPistolItem.animation(stack);

        poses.scale(0.375F, 0.375F, 0.375F);
        pivotX(poses, 0.0D, -1.0D, -6.0D, equipAngle(animation, time));
        pivotX(poses, 0.0D, 2.0D, -2.0D, liftAngle(animation, time));
        pivotX(poses, 0.0D, -1.0D, -1.0D, swirlAngle(animation, time));
        poses.translate(joltX(animation, time), 0.0D,
                recoilZ(animation, time) + joltZ(animation, time));

        renderGroup("Gun", pistol, poses, buffers, light, overlay);
        if (pistol.variant() == LaserPistolItem.Variant.PEW_PEW) {
            renderGroup("Capacitors", pistol, poses, buffers, light, overlay);
            renderGroup("Tape", pistol, poses, buffers, light, overlay);
        }
        poses.pushPose();
        pivotY(poses, 1.125D, 0.0D, -1.9125D, latchAngle(animation, time));
        renderGroup("Latch", pistol, poses, buffers, light, overlay);
        Vec battery = batteryOffset(animation, time);
        poses.translate(battery.x, battery.y, battery.z);
        renderGroup("Battery", pistol, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderStatic(LaserPistolItem pistol, PoseStack poses, MultiBufferSource buffers,
                              int light, int overlay) {
        renderGroup("Gun", pistol, poses, buffers, light, overlay);
        renderGroup("Latch", pistol, poses, buffers, light, overlay);
        if (pistol.variant() == LaserPistolItem.Variant.PEW_PEW) {
            renderGroup("Capacitors", pistol, poses, buffers, light, overlay);
            renderGroup("Tape", pistol, poses, buffers, light, overlay);
        }
    }

    private void renderFlash(PoseStack poses, MultiBufferSource buffers, float progress,
                             boolean emerald) {
        poses.pushPose();
        poses.translate(0.0D, 2.0D, 4.75D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        flashQuad(poses, buffers, progress, 1.5F,
                emerald ? 0.0F : 1.0F, emerald ? 0.5F : 0.0F, 0.0F);
        poses.translate(0.0D, 0.0D, -0.25D);
        flashQuad(poses, buffers, progress, 0.75F,
                emerald ? 0.5F : 1.0F, emerald ? 1.0F : 0.5F, 0.0F);
        poses.popPose();
    }

    private static void flashQuad(PoseStack poses, MultiBufferSource buffers, float progress,
                                  float scale, float red, float green, float blue) {
        float size = 4.0F * progress * scale;
        VertexConsumer out = buffers.getBuffer(FLASH_TYPE);
        PoseStack.Pose pose = poses.last();
        flashVertex(out, pose, 0, -size, -size, 1, 1, red, green, blue);
        flashVertex(out, pose, 0, size, -size, 0, 1, red, green, blue);
        flashVertex(out, pose, 0, size, size, 0, 0, red, green, blue);
        flashVertex(out, pose, 0, -size, size, 1, 0, red, green, blue);
    }

    private static void flashVertex(VertexConsumer out, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v,
                                    float red, float green, float blue) {
        out.addVertex(pose, x, y, z).setColor(red, green, blue, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 1.0F, 0.0F, 0.0F);
    }

    private void renderGroup(String group, LaserPistolItem pistol, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation texture = switch (pistol.variant()) {
            case PEW_PEW -> PEW_PEW_TEXTURE;
            case MORNING_GLORY -> MORNING_GLORY_TEXTURE;
            default -> TEXTURE;
        };
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Laser Pistol");
        }
        return mesh;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.75F / 16.0F, 1.75F / 16.0F, 1.75F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(0.0D, 0.5D, 0.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                poses.scale(1.25F, 1.25F, 1.25F);
                poses.translate(0.0D, -0.5D, 1.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 1.4D, 0.0D, aim),
                        lerp(-1.6D, -1.25D, aim), lerp(2.2D, 1.25D, aim));
            }
            case GROUND -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(ItemDisplayContext context, PoseStack poses) {
        float side = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? -1.0F : 1.0F;
        poses.translate(-side / 16.0D, -0.125D, 0.625D);
        poses.mulPose(Axis.YN.rotationDegrees(180));
        poses.mulPose(Axis.XP.rotationDegrees(90));
        poses.translate(-side / 16.0D, 0.4375D, 0.0625D);
        poses.translate(side * 0.25D, 0.1875D, -0.1875D);
        poses.scale(0.375F, 0.375F, 0.375F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 60));
        poses.mulPose(Axis.XN.rotationDegrees(90));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 20));
        poses.translate(0.0D, -0.3D, 0.0D);
        poses.scale(1.5F, 1.5F, 1.5F);
        poses.mulPose(Axis.YP.rotationDegrees(side * 50));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 335));
        poses.translate(-side * 0.9375D, -0.0625D, 0.0D);
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 15));
        poses.mulPose(Axis.YP.rotationDegrees(side * 12.5F));
        poses.mulPose(Axis.XP.rotationDegrees(15));
        poses.translate(side * 3.5D, 0.0D, 0.0D);
    }

    private static float equipAngle(LaserPistolItem.GunAnimation animation, float time) {
        if (animation == LaserPistolItem.GunAnimation.EQUIP) {
            return lerp(60.0F, 0.0F, sineDown(time / 500.0F));
        }
        if (animation == LaserPistolItem.GunAnimation.JAMMED && time >= 1500.0F) {
            if (time < 1600.0F) return lerp(0.0F, 7.5F, sineDown((time - 1500.0F) / 100.0F));
            if (time < 1850.0F) return lerp(7.5F, 0.0F, smooth((time - 1600.0F) / 250.0F));
        }
        return 0.0F;
    }

    private static float recoilZ(LaserPistolItem.GunAnimation animation, float time) {
        if (animation != LaserPistolItem.GunAnimation.CYCLE) return 0.0F;
        if (time < 50.0F) return lerp(0.0F, -0.5F, sineDown(time / 50.0F));
        if (time < 200.0F) return lerp(-0.5F, 0.0F, smooth((time - 50.0F) / 150.0F));
        return 0.0F;
    }

    private static float latchAngle(LaserPistolItem.GunAnimation animation, float time) {
        if (animation == LaserPistolItem.GunAnimation.RELOAD) {
            if (time < 100.0F) return lerp(0.0F, -20.0F, time / 100.0F);
            if (time < 2000.0F) return -20.0F;
            if (time < 2100.0F) return lerp(-20.0F, 0.0F, (time - 2000.0F) / 100.0F);
        }
        if (animation == LaserPistolItem.GunAnimation.JAMMED) {
            if (time < 500.0F) return 0.0F;
            if (time < 600.0F) return lerp(0.0F, -20.0F, (time - 500.0F) / 100.0F);
            if (time < 850.0F) return -20.0F;
            if (time < 950.0F) return lerp(-20.0F, 0.0F, (time - 850.0F) / 100.0F);
        }
        return 0.0F;
    }

    private static float liftAngle(LaserPistolItem.GunAnimation animation, float time) {
        if (animation != LaserPistolItem.GunAnimation.RELOAD || time < 100.0F) return 0.0F;
        if (time < 350.0F) return lerp(0.0F, -45.0F, smooth((time - 100.0F) / 250.0F));
        if (time < 850.0F) return -45.0F;
        if (time < 1350.0F) return lerp(-45.0F, 0.0F, smooth((time - 850.0F) / 500.0F));
        return 0.0F;
    }

    private static float swirlAngle(LaserPistolItem.GunAnimation animation, float time) {
        if (animation != LaserPistolItem.GunAnimation.INSPECT) return 0.0F;
        if (time < 750.0F) return lerp(0.0F, -720.0F, smooth(time / 750.0F));
        if (time < 1250.0F) return -720.0F;
        if (time < 2000.0F) return lerp(-720.0F, 0.0F, smooth((time - 1250.0F) / 750.0F));
        return 0.0F;
    }

    private static float joltX(LaserPistolItem.GunAnimation animation, float time) {
        if (animation == LaserPistolItem.GunAnimation.RELOAD) {
            if (time < 2100.0F) return 0.0F;
            if (time < 2150.0F) return lerp(0.0F, -0.0625F, sineUp((time - 2100.0F) / 50.0F));
            if (time < 2250.0F) return lerp(-0.0625F, 0.0F, smooth((time - 2150.0F) / 100.0F));
        }
        if (animation == LaserPistolItem.GunAnimation.JAMMED) {
            if (time < 950.0F) return 0.0F;
            if (time < 1000.0F) return lerp(0.0F, -0.0625F, sineUp((time - 950.0F) / 50.0F));
            if (time < 1100.0F) return lerp(-0.0625F, 0.0F, smooth((time - 1000.0F) / 100.0F));
        }
        return 0.0F;
    }

    private static float joltZ(LaserPistolItem.GunAnimation animation, float time) {
        if (animation != LaserPistolItem.GunAnimation.RELOAD || time < 350.0F) return 0.0F;
        if (time < 450.0F) return lerp(0.0F, 0.5F, smooth((time - 350.0F) / 100.0F));
        if (time < 550.0F) return lerp(0.5F, -1.5F, sineUp((time - 450.0F) / 100.0F));
        if (time < 700.0F) return lerp(-1.5F, 0.0F, smooth((time - 550.0F) / 150.0F));
        return 0.0F;
    }

    private static Vec batteryOffset(LaserPistolItem.GunAnimation animation, float time) {
        if (animation != LaserPistolItem.GunAnimation.RELOAD || time < 550.0F) return Vec.ZERO;
        if (time < 800.0F) return new Vec(0.0D, 0.0D, lerp(0.0F, 5.0F, (time - 550.0F) / 250.0F));
        if (time < 1350.0F) return new Vec(0.0D, 0.0D, 5.0D);
        if (time < 1600.0F) return new Vec(0.0D,
                lerp(-2.0F, 0.0F, smooth((time - 1350.0F) / 250.0F)), -2.0D);
        if (time < 1850.0F) {
            float progress = sineUp((time - 1600.0F) / 250.0F);
            return new Vec(0.0D, 0.0D, lerp(-2.0F, 0.0F, progress));
        }
        return Vec.ZERO;
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, float angle) {
        if (angle == 0.0F) return;
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees(angle));
        poses.translate(-x, -y, -z);
    }

    private static void pivotY(PoseStack poses, double x, double y, double z, float angle) {
        if (angle == 0.0F) return;
        poses.translate(x, y, z);
        poses.mulPose(Axis.YP.rotationDegrees(angle));
        poses.translate(-x, -y, -z);
    }

    private static float sineDown(float progress) {
        return (float) Math.sin(Math.PI * 0.5D * Mth.clamp(progress, 0.0F, 1.0F));
    }

    private static float sineUp(float progress) {
        return 1.0F - (float) Math.cos(Math.PI * 0.5D * Mth.clamp(progress, 0.0F, 1.0F));
    }

    private static float smooth(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);
        return (1.0F - (float) Math.cos(Math.PI * clamped)) * 0.5F;
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * Mth.clamp(progress, 0.0F, 1.0F);
    }

    private static double lerp(double from, double to, float progress) {
        return from + (to - from) * Mth.clamp(progress, 0.0F, 1.0F);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private record Vec(double x, double y, double z) {
        private static final Vec ZERO = new Vec(0.0D, 0.0D, 0.0D);
    }
}
