package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.LasrifleItem;
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

public final class LasrifleItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/lasrifle.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/lasrifle.png");
    private static final ResourceLocation FLASH_TEXTURE = id("textures/models/weapons/laser_flash.png");
    private static final Set<String> GROUPS = Set.of(
            "Gun", "Stock", "Scope", "Lever", "Battery", "Barrel");
    private static final RenderType FLASH_TYPE = RenderType.create(
            "hbm_lasrifle_flash", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
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

    public LasrifleItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof LasrifleItem)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = first || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        boolean hiddenByScope = first && ClientWeaponEvents.fullyAimed();
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);

        poses.pushPose();
        setupContext(context, poses);
        if (!hiddenByScope) {
            if (first) renderFirstPerson(stack, poses, buffers, light, overlay);
            else renderStatic(poses, buffers, light, overlay);
            if (held && elapsed >= 0L && elapsed < 150L) {
                renderFlash(poses, buffers, elapsed / 150.0F);
            }
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float time = (LasrifleItem.animationTimer(stack) + partial) * 50.0F;
        LasrifleItem.GunAnimation animation = LasrifleItem.animation(stack);

        poses.scale(0.3125F, 0.3125F, 0.3125F);
        pivotX(poses, 0.0D, -1.0D, -6.0D, equipAngle(animation, time));
        poses.translate(0.0D, 0.0D, recoilZ(animation, time));

        render("Gun", poses, buffers, light, overlay);
        render("Stock", poses, buffers, light, overlay);
        render("Scope", poses, buffers, light, overlay);

        poses.pushPose();
        pivotX(poses, 0.0D, -0.375D, 2.375D, leverAngle(animation, time));
        render("Lever", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        Vec mag = magazineOffset(animation, time);
        poses.translate(mag.x, mag.y, mag.z);
        render("Battery", poses, buffers, light, overlay);
        poses.popPose();

        render("Barrel", poses, buffers, light, overlay);
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        render("Gun", poses, buffers, light, overlay);
        render("Stock", poses, buffers, light, overlay);
        render("Scope", poses, buffers, light, overlay);
        render("Lever", poses, buffers, light, overlay);
        render("Battery", poses, buffers, light, overlay);
        render("Barrel", poses, buffers, light, overlay);
    }

    private static void renderFlash(PoseStack poses, MultiBufferSource buffers, float progress) {
        poses.pushPose();
        poses.translate(0.0D, 1.5D, 12.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        flashQuad(poses, buffers, progress, 1.5F, 1.0F, 0.0F, 0.0F);
        poses.translate(0.0D, 0.0D, -0.25D);
        flashQuad(poses, buffers, progress, 0.75F, 1.0F, 0.5F, 0.0F);
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

    private void render(String group, PoseStack poses, MultiBufferSource buffers,
                        int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Lasrifle");
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
                poses.scale(1.03125F / 16.0F, 1.03125F / 16.0F, 1.03125F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(0.75D, 0.0D, 0.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 1.2D, 0.0D, aim),
                        lerp(-1.2D, -0.921875D, aim), lerp(2.0D, 0.75D, aim));
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
        poses.mulPose(Axis.YN.rotationDegrees(180.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F));
        poses.translate(-side / 16.0D, 0.4375D, 0.0625D);
        poses.translate(side * 0.25D, 0.1875D, -0.1875D);
        poses.scale(0.375F, 0.375F, 0.375F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 60.0F));
        poses.mulPose(Axis.XN.rotationDegrees(90.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 20.0F));
        poses.translate(0.0D, -0.3D, 0.0D);
        poses.scale(1.5F, 1.5F, 1.5F);
        poses.mulPose(Axis.YP.rotationDegrees(side * 50.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 335.0F));
        poses.translate(-side * 0.9375D, -0.0625D, 0.0D);
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 15.0F));
        poses.mulPose(Axis.YP.rotationDegrees(side * 12.5F));
        poses.mulPose(Axis.XP.rotationDegrees(15.0F));
        poses.translate(side * 3.5D, 0.0D, 0.0D);
        poses.scale(1.25F, 1.25F, 1.25F);
        poses.translate(0.0D, 0.0D, 4.0D);
    }

    private static float equipAngle(LasrifleItem.GunAnimation animation, float time) {
        if (animation == LasrifleItem.GunAnimation.EQUIP) {
            return lerp(60.0F, 0.0F, sineDown(time / 500.0F));
        }
        if (animation == LasrifleItem.GunAnimation.RELOAD) {
            if (time < 1_700.0F) return 0.0F;
            if (time < 1_800.0F) return lerp(0.0F, -2.0F, sineDown((time - 1_700.0F) / 100.0F));
            if (time < 1_900.0F) return lerp(-2.0F, 0.0F, smooth((time - 1_800.0F) / 100.0F));
        } else if (animation == LasrifleItem.GunAnimation.JAMMED) {
            if (time < 1_300.0F) return 0.0F;
            if (time < 1_400.0F) return lerp(0.0F, -2.0F, sineDown((time - 1_300.0F) / 100.0F));
            if (time < 1_500.0F) return lerp(-2.0F, 0.0F, smooth((time - 1_400.0F) / 100.0F));
        } else if (animation == LasrifleItem.GunAnimation.INSPECT) {
            if (time < 800.0F) return 0.0F;
            if (time < 900.0F) return lerp(0.0F, -2.0F, sineDown((time - 800.0F) / 100.0F));
            if (time < 1_000.0F) return lerp(-2.0F, 0.0F, smooth((time - 900.0F) / 100.0F));
        }
        return 0.0F;
    }

    private static float recoilZ(LasrifleItem.GunAnimation animation, float time) {
        if (animation != LasrifleItem.GunAnimation.CYCLE) return 0.0F;
        if (time < 50.0F) return lerp(0.0F, -0.5F, sineDown(time / 50.0F));
        if (time < 200.0F) return lerp(-0.5F, 0.0F, smooth((time - 50.0F) / 150.0F));
        return 0.0F;
    }

    private static float leverAngle(LasrifleItem.GunAnimation animation, float time) {
        return switch (animation) {
            case RELOAD -> lever(time, 0.0F, 350.0F, 1_850.0F, 2_200.0F);
            case JAMMED -> lever(time, 500.0F, 850.0F, 1_450.0F, 1_800.0F);
            case INSPECT -> lever(time, 0.0F, 350.0F, 950.0F, 1_300.0F);
            default -> 0.0F;
        };
    }

    private static float lever(float time, float start, float open, float close, float end) {
        if (time < start) return 0.0F;
        if (time < open) return lerp(0.0F, -90.0F, sineUp((time - start) / (open - start)));
        if (time < close) return -90.0F;
        if (time < end) return lerp(-90.0F, 0.0F, sineUp((time - close) / (end - close)));
        return 0.0F;
    }

    private static Vec magazineOffset(LasrifleItem.GunAnimation animation, float time) {
        return switch (animation) {
            case RELOAD -> magazine(time, -5.0F,
                    350.0F, 700.0F, 1_200.0F, 1_700.0F, 1_850.0F, 2_200.0F);
            case JAMMED -> magazine(time, -2.0F,
                    850.0F, 1_050.0F, 1_050.0F, 1_300.0F, 1_450.0F, 1_800.0F);
            case INSPECT -> magazine(time, -2.0F,
                    350.0F, 550.0F, 550.0F, 800.0F, 950.0F, 1_300.0F);
            default -> Vec.ZERO;
        };
    }

    private static Vec magazine(float time, float pulledY, float pullStart, float pullEnd,
                                float liftStart, float liftEnd, float insertStart, float insertEnd) {
        if (time < pullStart) return Vec.ZERO;
        if (time < pullEnd) {
            return new Vec(0.0D, lerp(0.0F, pulledY,
                    sineUp((time - pullStart) / (pullEnd - pullStart))), 0.0D);
        }
        if (time < liftStart) return new Vec(0.0D, pulledY, 0.0D);
        if (time < liftEnd) {
            return new Vec(0.0D, lerp(pulledY, -0.25F,
                    smooth((time - liftStart) / (liftEnd - liftStart))), 0.0D);
        }
        if (time < insertStart) return new Vec(0.0D, -0.25D, 0.0D);
        if (time < insertEnd) {
            return new Vec(0.0D, lerp(-0.25F, 0.0F,
                    (time - insertStart) / (insertEnd - insertStart)), 0.0D);
        }
        return Vec.ZERO;
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, float angle) {
        if (angle == 0.0F) return;
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees(angle));
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
