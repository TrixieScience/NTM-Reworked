package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.blockentity.TurretVariant;
import com.hbm.ntm.registry.ModItems;
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
    private static final ResourceLocation CHEKHOV_MODEL = id("models/turrets/turret_chekhov.obj");
    private static final ResourceLocation JEREMY_MODEL = id("models/turrets/turret_jeremy.obj");
    private static final ResourceLocation TAUON_MODEL = id("models/turrets/turret_tauon.obj");
    private static final ResourceLocation BASE = id("textures/models/turrets/base.png");
    private static final ResourceLocation CARRIAGE = id("textures/models/turrets/carriage.png");
    private static final ResourceLocation FRIENDLY_BASE = id("textures/models/turrets/base_friendly.png");
    private static final ResourceLocation FRIENDLY_CARRIAGE = id("textures/models/turrets/carriage_friendly.png");
    private static final ResourceLocation BODY = id("textures/models/turrets/chekhov.png");
    private static final ResourceLocation BARRELS = id("textures/models/turrets/chekhov_barrels.png");
    private static final ResourceLocation JEREMY = id("textures/models/turrets/jeremy.png");
    private static final ResourceLocation TAUON = id("textures/models/turrets/tauon.png");
    private EnvsuitMesh chekhovMesh, jeremyMesh, tauonMesh;

    public TurretFriendlyItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.5D, 0.25D, 0.5D);
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YP.rotationDegrees(135F));
        TurretVariant variant = variant(stack);
        if (variant == TurretVariant.JEREMY) poses.scale(0.625F, 0.625F, 0.625F);
        if (variant == TurretVariant.TAUON) poses.scale(1.25F, 1.25F, 1.25F);
        boolean friendly = variant == TurretVariant.FRIENDLY;
        render(chekhovMesh(), "Base", friendly ? FRIENDLY_BASE : BASE, poses, buffers, light, overlay);
        render(chekhovMesh(), "Carriage", friendly ? FRIENDLY_CARRIAGE : CARRIAGE, poses, buffers, light, overlay);
        switch (variant) {
            case CHEKHOV, FRIENDLY -> {
                render(chekhovMesh(), "Body", BODY, poses, buffers, light, overlay);
                render(chekhovMesh(), "Barrels", BARRELS, poses, buffers, light, overlay);
            }
            case JEREMY -> render(jeremyMesh(), "Gun", JEREMY, poses, buffers, light, overlay);
            case TAUON -> {
                render(tauonMesh(), "Cannon", TAUON, poses, buffers, light, overlay);
                render(tauonMesh(), "Rotor", TAUON, poses, buffers, light, overlay);
            }
        }
        poses.popPose();
    }

    private void render(EnvsuitMesh mesh, String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh.render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1F, light, overlay, -1);
    }

    private EnvsuitMesh chekhovMesh() {
        if (chekhovMesh == null) chekhovMesh = load(CHEKHOV_MODEL,
                Set.of("Base", "Carriage", "Body", "Barrels", "Connectors"), "turret item");
        return chekhovMesh;
    }
    private EnvsuitMesh jeremyMesh() {
        if (jeremyMesh == null) jeremyMesh = load(JEREMY_MODEL, Set.of("Gun"), "Jeremy item");
        return jeremyMesh;
    }
    private EnvsuitMesh tauonMesh() {
        if (tauonMesh == null) tauonMesh = load(TAUON_MODEL, Set.of("Cannon", "Rotor"), "Tauon item");
        return tauonMesh;
    }
    private static EnvsuitMesh load(ResourceLocation model, Set<String> groups, String name) {
        return EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), model, groups, name);
    }
    private static TurretVariant variant(ItemStack stack) {
        if (stack.is(ModItems.TURRET_CHEKHOV_ITEM.get())) return TurretVariant.CHEKHOV;
        if (stack.is(ModItems.TURRET_JEREMY_ITEM.get())) return TurretVariant.JEREMY;
        if (stack.is(ModItems.TURRET_TAUON_ITEM.get())) return TurretVariant.TAUON;
        return TurretVariant.FRIENDLY;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
