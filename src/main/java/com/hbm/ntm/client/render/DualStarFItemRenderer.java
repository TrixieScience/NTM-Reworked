package com.hbm.ntm.client.render;

import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.DualStarFItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Paired elite Star F models, each with its own animation problems. */
public final class DualStarFItemRenderer extends BlockEntityWithoutLevelRenderer {
    private EnvsuitMesh mesh;

    public DualStarFItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof DualStarFItem)) return;
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND ->
                    renderFirstPerson(stack, poses, buffers, light, overlay);
            case THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                renderStatic(1, poses, buffers, light, overlay);
                renderHeldFlash(stack, 1, poses, buffers);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                setupThirdPerson(context, poses);
                renderStatic(0, poses, buffers, light, overlay);
                renderHeldFlash(stack, 0, poses, buffers);
            }
            case GUI -> renderInventory(poses, buffers, light, overlay);
            case GROUND, FIXED -> renderDropped(poses, buffers, light, overlay);
            default -> renderDropped(poses, buffers, light, overlay);
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses,
                                   MultiBufferSource buffers, int light, int overlay) {
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        for (int index = 0; index < DualStarFItem.RECEIVER_COUNT; index++) {
            int direction = index == 0 ? -1 : 1;
            poses.pushPose();
            poses.translate(-2.0D * 0.8D * direction, -1.75D * 0.8D,
                    0.875D + 2.5D * 0.8D);
            poses.scale(0.25F, 0.25F, 0.25F);
            float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            double time = (DualStarFItem.animationTimer(stack, index) + partial) * 50.0D;
            TwentyTwoGunItemRenderer.StarAnimation animation =
                    TwentyTwoGunItemRenderer.starAnimation(
                            DualStarFItem.animation(stack, index).ordinal(), time, false,
                            DualStarFItem.rounds(stack, index));
            TwentyTwoGunItemRenderer.renderStarAnimated(mesh(),
                    TwentyTwoGunItemRenderer.STAR_ELITE_TEXTURE, animation, direction,
                    poses, buffers, light, overlay);
            poses.pushPose();
            poses.translate(0.0D, 3.0D, 6.125D);
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            poses.scale(0.5F, 0.5F, 0.5F);
            WeaponSmokeRenderer.render(stack, index, poses, buffers, 0.75D,
                    WeaponSmokeRenderer.TWENTY_TWO,
                    DualStarFItem.state(stack, index) == DualStarFItem.GunState.RELOADING);
            poses.popPose();
            renderHeldFlash(stack, index, poses, buffers);
            poses.popPose();
        }
    }

    private void renderInventory(PoseStack poses, MultiBufferSource buffers,
                                 int light, int overlay) {
        poses.scale(1.0F, -1.0F, 1.0F);
        poses.scale(1.0F, 1.0F, -1.0F);
        poses.scale(1.5F / 16.0F, 1.5F / 16.0F, 1.5F / 16.0F);

        poses.pushPose();
        poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(25.0F));
        poses.mulPose(Axis.YP.rotationDegrees(45.0F));
        poses.translate(0.5D, 0.0D, 0.0D);
        renderStatic(1, poses, buffers, light, overlay);
        poses.popPose();

        poses.translate(0.0D, 0.0D, 5.0D);
        poses.pushPose();
        poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        poses.mulPose(Axis.XN.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(25.0F));
        poses.mulPose(Axis.YN.rotationDegrees(45.0F));
        poses.translate(-0.5D, 0.0D, 0.0D);
        renderStatic(0, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderDropped(PoseStack poses, MultiBufferSource buffers,
                               int light, int overlay) {
        poses.scale(0.075F, 0.075F, 0.075F);
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        poses.pushPose();
        poses.translate(-1.0D, 1.0D, 0.0D);
        renderStatic(1, poses, buffers, light, overlay);
        poses.popPose();
        poses.pushPose();
        poses.translate(1.0D, 1.0D, 0.0D);
        renderStatic(0, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderStatic(int index, PoseStack poses, MultiBufferSource buffers,
                              int light, int overlay) {
        TwentyTwoGunItemRenderer.renderStarStatic(mesh(),
                TwentyTwoGunItemRenderer.STAR_ELITE_TEXTURE, poses, buffers, light, overlay);
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
        poses.translate(0.0D, -0.25D, 1.75D);
        poses.scale(0.75F, 0.75F, 0.75F);
    }

    private static void renderHeldFlash(ItemStack stack, int index, PoseStack poses,
                                        MultiBufferSource buffers) {
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack, index);
        if (elapsed < 0L || elapsed >= 75L) return;
        TwentyTwoGunItemRenderer.renderStarFlash(poses, buffers, elapsed / 75.0F,
                ClientWeaponEvents.shotRandom(stack, index));
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = TwentyTwoGunItemRenderer.loadStarMesh();
        return mesh;
    }
}
