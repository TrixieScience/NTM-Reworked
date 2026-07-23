package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.entity.TurretOrdnanceEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class TurretOrdnanceRenderer extends EntityRenderer<TurretOrdnanceEntity> {
    private static final ResourceLocation PROJECTILES = id("models/projectiles/projectiles.obj");
    private static final ResourceLocation HIMARS = id("models/turrets/turret_himars.obj");
    private static final ResourceLocation GRENADE = id("textures/models/projectiles/grenade.png");
    private static final ResourceLocation BLANK = id("textures/particle/particle_base.png");
    private EnvsuitMesh projectileMesh;
    private EnvsuitMesh himarsMesh;

    public TurretOrdnanceRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0F;
    }

    @Override public void render(TurretOrdnanceEntity projectile, float yaw, float partialTick,
                                 PoseStack poses, MultiBufferSource buffers, int light) {
        Vec3 direction = projectile.direction();
        float horizontal = Mth.sqrt((float) (direction.x * direction.x + direction.z * direction.z));
        float authoredYaw = (float) (Mth.atan2(direction.x, direction.z) * Mth.RAD_TO_DEG);
        float authoredPitch = (float) (Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG);
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(authoredYaw - 90F));
        poses.mulPose(Axis.ZP.rotationDegrees(authoredPitch - 90F));
        if (projectile.isHimars()) {
            poses.mulPose(Axis.YP.rotationDegrees(90F));
            poses.mulPose(Axis.XP.rotationDegrees(90F));
            boolean single = projectile.ammoIndex() == 1 || projectile.ammoIndex() == 5;
            himarsMesh().render(single ? "RocketSingle" : "RocketStandard", poses.last(),
                    buffers.getBuffer(RenderType.entityCutout(himarsTexture(projectile.ammoIndex()))),
                    1F, light, OverlayTexture.NO_OVERLAY, -1);
        } else {
            poses.scale(2.5F, 5F, 2.5F);
            projectileMesh().render("Grenade", poses.last(),
                    buffers.getBuffer(RenderType.entityCutout(GRENADE)),
                    1F, light, OverlayTexture.NO_OVERLAY, -1);
        }
        poses.popPose();
        super.render(projectile, yaw, partialTick, poses, buffers, light);
    }

    private EnvsuitMesh projectileMesh() {
        if (projectileMesh == null) projectileMesh = EnvsuitMesh.load(
                Minecraft.getInstance().getResourceManager(), PROJECTILES,
                Set.of("Grenade"), "Artillery shell");
        return projectileMesh;
    }

    private EnvsuitMesh himarsMesh() {
        if (himarsMesh == null) himarsMesh = EnvsuitMesh.load(
                Minecraft.getInstance().getResourceManager(), HIMARS,
                Set.of("RocketStandard", "RocketSingle"), "HIMARS rocket");
        return himarsMesh;
    }

    private static ResourceLocation himarsTexture(int type) {
        String name = switch (type) {
            case 1 -> "himars_single";
            case 2 -> "himars_standard_he";
            case 3 -> "himars_standard_wp";
            case 4 -> "himars_standard_tb";
            case 5 -> "himars_single_tb";
            case 6 -> "himars_standard_mini_nuke";
            case 7 -> "himars_standard_lava";
            default -> "himars_standard";
        };
        return id("textures/models/projectiles/" + name + ".png");
    }

    @Override public ResourceLocation getTextureLocation(TurretOrdnanceEntity entity) { return BLANK; }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
