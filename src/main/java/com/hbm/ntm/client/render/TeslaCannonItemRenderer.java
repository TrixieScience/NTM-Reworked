package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.TeslaCannonItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Source group choreography: gun, extension, cog, and the capacitor belt. */
public final class TeslaCannonItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/tesla_cannon.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/tesla_cannon.png");
    private static final ResourceLocation YOMI_MODEL = id("models/trinkets/yomi.obj");
    private static final ResourceLocation YOMI_TEXTURE = id("textures/models/trinkets/yomi.png");
    private static final Set<String> GROUPS = Set.of("Gun", "Extension", "Cog", "Capacitor");
    private EnvsuitMesh mesh;
    private EnvsuitMesh yomi;

    public TeslaCannonItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof TeslaCannonItem)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        poses.pushPose();
        setupContext(context, poses);
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float time = (TeslaCannonItem.animationTimer(stack) + partial) * 50.0F;
        float cog = cycleAngle(stack, time);
        if (first) {
            float equip = TeslaCannonItem.animation(stack) == TeslaCannonItem.GunAnimation.EQUIP
                    ? lerp(60.0F, 0.0F, Math.min(1.0F, time / 1000.0F)) : 0.0F;
            float recoil = TeslaCannonItem.animation(stack) == TeslaCannonItem.GunAnimation.CYCLE
                    ? recoil(time, TeslaCannonItem.aiming(stack) ? -0.5F : -1.0F) : 0.0F;
            poses.scale(0.75F, 0.75F, 0.75F);
            poses.translate(0.0D, -2.0D, -2.0D);
            poses.mulPose(Axis.XP.rotationDegrees(equip));
            poses.translate(0.0D, 2.0D, 2.0D + recoil);
            poses.mulPose(Axis.XP.rotationDegrees(recoil * 2.0F));
        }
        renderGroup("Gun", poses, buffers, light, overlay);
        renderGroup("Extension", poses, buffers, light, overlay);
        poses.pushPose();
        rotateCog(poses, cog);
        renderGroup("Cog", poses, buffers, light, overlay);
        poses.popPose();
        renderCapacitors(poses, buffers, light, overlay, first
                ? Math.min(TeslaCannonItem.cycleCount(stack), 8) : 10, cog);
        if (first && TeslaCannonItem.animation(stack) == TeslaCannonItem.GunAnimation.INSPECT) {
            renderYomi(poses, buffers, light, overlay, time);
        }
        poses.popPose();
    }

    private void renderCapacitors(PoseStack poses, MultiBufferSource buffers, int light, int overlay,
                                  int amount, float cog) {
        poses.pushPose();
        rotateCog(poses, cog);
        for (int i = 0; i < amount; i++) {
            renderGroup("Capacitor", poses, buffers, light, overlay);
            if (i < 4) rotateCog(poses, -22.5F);
            else {
                if (i == 4) {
                    rotateCog(poses, -cog);
                    poses.translate(-cog * 0.5D / 22.5D, 0.0D, 0.0D);
                }
                poses.translate(0.5D, 0.0D, 0.0D);
            }
        }
        poses.popPose();
    }

    private void renderYomi(PoseStack poses, MultiBufferSource buffers, int light, int overlay, float time) {
        float x;
        float y;
        if (time < 500.0F) { float p = time / 500.0F; x = lerp(8, 4, p); y = lerp(-4, -1, p); }
        else if (time < 1500.0F) { x = 4; y = -1; }
        else { float p = Math.min(1, (time - 1500.0F) / 500.0F); x = lerp(4, 6, p); y = lerp(-1, -6, p); }
        poses.pushPose();
        poses.translate(x, y, 0.0D);
        poses.mulPose(Axis.YP.rotationDegrees(135.0F));
        float squeeze = time >= 1050.0F && time < 1250.0F ? 0.5F : 1.0F;
        poses.scale(1.0F, 1.0F, squeeze);
        var consumer = buffers.getBuffer(RenderType.entityCutout(YOMI_TEXTURE));
        yomi().render("Plane", poses.last(), consumer, 1.0F, light, overlay, -1);
        poses.popPose();
    }

    private static float cycleAngle(ItemStack stack, float time) {
        if (TeslaCannonItem.animation(stack) != TeslaCannonItem.GunAnimation.CYCLE
                && TeslaCannonItem.animation(stack) != TeslaCannonItem.GunAnimation.CYCLE_DRY) return 0.0F;
        if (time <= 150.0F) return 0.0F;
        return 22.5F * Math.min(1.0F, (time - 150.0F) / 350.0F);
    }

    private static float recoil(float time, float kick) {
        if (time <= 100.0F) return kick * (float) Math.sin(Math.PI * time / 200.0F);
        if (time >= 250.0F) return 0.0F;
        return kick * (1.0F - (time - 100.0F) / 150.0F);
    }

    private static void rotateCog(PoseStack poses, float angle) {
        poses.translate(0.0D, -1.625D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees(angle));
        poses.translate(0.0D, 1.625D, 0.0D);
    }

    private void renderGroup(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL,
                GROUPS, "Tesla Cannon");
        return mesh;
    }
    private EnvsuitMesh yomi() {
        if (yomi == null) yomi = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), YOMI_MODEL,
                Set.of("Plane"), "Yomi");
        return yomi;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, -1.0F); poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F)); poses.scale(1.25F / 16.0F, 1.25F / 16.0F, 1.25F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F)); poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(0.0D, 0.5D, 0.0D);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses); poses.scale(2.75F, 2.75F, 2.75F); poses.translate(0.0D, 1.5D, 1.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F)); poses.translate(0, 0, 0.875D);
                poses.translate(lerp((float) (side * 1.4D), (float) (side * 1.05D), aim),
                        lerp(-0.4F, 0.0F, aim), lerp(1.4F, -0.4F, aim));
            }
            default -> { poses.scale(0.075F, 0.075F, 0.075F); poses.mulPose(Axis.YN.rotationDegrees(90)); }
        }
    }

    private static void setupThirdPerson(ItemDisplayContext context, PoseStack poses) {
        float side = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? -1.0F : 1.0F;
        poses.translate(-side / 16.0D, -0.125D, 0.625D); poses.mulPose(Axis.YN.rotationDegrees(180));
        poses.mulPose(Axis.XP.rotationDegrees(90)); poses.translate(-side / 16.0D, 0.4375D, 0.0625D);
        poses.translate(side * 0.25D, 0.1875D, -0.1875D); poses.scale(0.375F, 0.375F, 0.375F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 60)); poses.mulPose(Axis.XN.rotationDegrees(90));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 20)); poses.translate(0, -0.3D, 0);
        poses.scale(1.5F, 1.5F, 1.5F); poses.mulPose(Axis.YP.rotationDegrees(side * 50));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 335)); poses.translate(-side * 0.9375D, -0.0625D, 0);
        poses.scale(0.125F, 0.125F, 0.125F); poses.mulPose(Axis.ZP.rotationDegrees(side * 15));
        poses.mulPose(Axis.YP.rotationDegrees(side * 12.5F)); poses.mulPose(Axis.XP.rotationDegrees(15));
        poses.translate(side * 3.5D, 0, 0);
    }

    private static float lerp(float from, float to, float progress) { return from + (to - from) * progress; }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path); }
}
