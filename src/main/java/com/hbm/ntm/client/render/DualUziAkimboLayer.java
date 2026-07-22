package com.hbm.ntm.client.render;

import com.hbm.ntm.item.DualMaresLegItem;
import com.hbm.ntm.item.DualUziItem;
import com.hbm.ntm.item.EyesOfTheTempestItem;
import com.hbm.ntm.item.DaniItem;
import com.hbm.ntm.item.DualStarFItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Extra left arm pass for the second terrible idea. */
public final class DualUziAkimboLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public DualUziAkimboLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffers, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = player.getMainHandItem();
        // ItemRenderUziAkimbo and ItemRenderMareslegAkimbo both answered isAkimbo() == true.
        if (!(stack.getItem() instanceof DualUziItem)
                && !(stack.getItem() instanceof DualMaresLegItem)
                && !(stack.getItem() instanceof DualStarFItem)
                && !(stack.getItem() instanceof DaniItem)
                && !(stack.getItem() instanceof EyesOfTheTempestItem)) return;

        poses.pushPose();
        PlayerModel<AbstractClientPlayer> model = getParentModel();
        float previousYaw = model.leftArm.yRot;
        model.leftArm.yRot = 0.1F + model.head.yRot;
        model.translateToHand(HumanoidArm.LEFT, poses);
        model.leftArm.yRot = previousYaw;

        poses.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        poses.translate(-1.0F / 16.0F, 0.125F, -0.625F);
        itemInHandRenderer.renderItem(player, stack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                true, poses, buffers, packedLight);
        poses.popPose();
    }
}
