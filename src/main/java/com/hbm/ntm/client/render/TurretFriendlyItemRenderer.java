package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
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

public final class TurretFriendlyItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/turrets/turret_chekhov.obj");
    private static final ResourceLocation BASE = id("textures/models/turrets/base_friendly.png");
    private static final ResourceLocation CARRIAGE = id("textures/models/turrets/carriage_friendly.png");
    private static final ResourceLocation BODY = id("textures/models/turrets/chekhov.png");
    private static final ResourceLocation BARRELS = id("textures/models/turrets/chekhov_barrels.png");
    private static final Set<String> GROUPS = Set.of("Base", "Carriage", "Body", "Barrels", "Connectors");
    private EnvsuitMesh mesh;

    public TurretFriendlyItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.5D, 0.25D, 0.5D);
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YP.rotationDegrees(135F));
        render("Base", BASE, poses, buffers, light, overlay);
        render("Carriage", CARRIAGE, poses, buffers, light, overlay);
        render("Body", BODY, poses, buffers, light, overlay);
        render("Barrels", BARRELS, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void render(String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                MODEL, GROUPS, "Mister Friendly item");
        return mesh;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
