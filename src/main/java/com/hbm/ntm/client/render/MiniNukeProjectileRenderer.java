package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.entity.MiniNukeProjectileEntity;
import com.hbm.ntm.weapon.MiniNukeAmmoType;
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

public final class MiniNukeProjectileRenderer extends EntityRenderer<MiniNukeProjectileEntity> {
    private static final ResourceLocation MODEL = id("models/weapons/fatman.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/fatman_mininuke.png");
    private static final ResourceLocation BALEFIRE = id("textures/models/weapons/fatman_balefire.png");
    private static final ResourceLocation BLANK = id("textures/particle/particle_base.png");
    private EnvsuitMesh mesh;

    public MiniNukeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(MiniNukeProjectileEntity projectile, float yaw, float partialTick,
                       PoseStack poses, MultiBufferSource buffers, int light) {
        Vec3 direction = projectile.direction();
        float horizontal = Mth.sqrt((float) (direction.x * direction.x + direction.z * direction.z));
        float authoredYaw = (float) (Mth.atan2(direction.x, direction.z) * Mth.RAD_TO_DEG);
        float authoredPitch = (float) (Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG);
        ResourceLocation texture = projectile.ammoType() == MiniNukeAmmoType.BALEFIRE
                ? BALEFIRE : TEXTURE;

        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(authoredYaw - 90.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(authoredPitch + 180.0F));
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        poses.translate(0.0D, -1.0D, 1.0D);
        mesh().render("MiniNuke", poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, OverlayTexture.NO_OVERLAY, -1);
        if (projectile.ammoType() == MiniNukeAmmoType.BALEFIRE) {
            mesh().render("MiniNuke", poses.last(), buffers.getBuffer(RenderType.entityGlint()),
                    1.0F, light, OverlayTexture.NO_OVERLAY, 0xFF00CC26);
        }
        poses.popPose();
        super.render(projectile, yaw, partialTick, poses, buffers, light);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL,
                    Set.of("MiniNuke"), "Fat Man Projectile");
        }
        return mesh;
    }

    @Override public ResourceLocation getTextureLocation(MiniNukeProjectileEntity entity) { return BLANK; }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }
}
