package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.AmatItem;
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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Exact grouped {@code ItemRenderAmat} presentation and {@code LAMBDA_AMAT_ANIMS} buses. */
public final class AmatItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation STANDARD_TEXTURE = id("textures/models/weapons/amat.png");
    private static final ResourceLocation MODEL = id("models/weapons/amat.obj");
    private static final ResourceLocation G3_MODEL = id("models/weapons/g3.obj");
    private static final ResourceLocation ATTACHMENTS_TEXTURE =
            id("textures/models/weapons/g3_attachments.png");
    private static final ResourceLocation FLASH_TEXTURE = id("textures/models/weapons/lilmac_plume.png");
    private static final RenderType GAP_FLASH = RenderType.create(
            "hbm_amat_gap_flash", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(FLASH_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false));
    private static final Set<String> GROUPS = Set.of(
            "Bolt", "Gun", "Scope", "Magazine", "Bullet", "BipodRight",
            "BipodHingeRight", "MuzzleBrake", "BipodHingeLeft", "BipodLeft");

    private final ResourceLocation texture;
    private final boolean silenced;
    private EnvsuitMesh mesh;
    private EnvsuitMesh attachmentMesh;

    public AmatItemRenderer() {
        this(STANDARD_TEXTURE, false);
    }

    /** One mesh, three AMAT textures, no questions at customs. */
    public AmatItemRenderer(ResourceLocation texture) {
        this(texture, false);
    }

    /** Penance uses the same authored buses but permanently replaces the muzzle brake. */
    public AmatItemRenderer(ResourceLocation texture, boolean silenced) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.texture = texture;
        this.silenced = silenced;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        boolean hiddenByScope = firstPerson && ClientWeaponEvents.fullyAimed();
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);

        poses.pushPose();
        setupContext(context, poses, silenced);
        if (!hiddenByScope) {
            if (firstPerson) renderFirstPerson(stack, poses, buffers, light, overlay);
            else renderStatic(poses, buffers, light, overlay);
            if (!silenced && held && elapsed >= 0L && elapsed < 75L) {
                renderGapFlash(poses, buffers, elapsed);
            }
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        AmatAnimation animation = animation(stack, animationTime(stack));
        poses.scale(0.375F, 0.375F, 0.375F);
        poses.translate(0.0D, 0.0D, animation.recoil.z);
        pivotX(poses, 0.0D, -3.0D, -8.0D, animation.equip.x);
        pivotX(poses, 0.0D, -3.0D, -8.0D, animation.lift.x);

        render("Gun", poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(animation.scopeThrow.x, animation.scopeThrow.y, animation.scopeThrow.z);
        pivotX(poses, 0.0D, 1.5D, -4.5D, animation.scopeSpin.x);
        render("Scope", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.625D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.boltTurn.z));
        poses.translate(0.0D, -0.625D, animation.boltPull.z);
        render("Bolt", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        render("Magazine", poses, buffers, light, overlay);
        poses.popPose();

        double bipodX = AmatItem.animation(stack) == AmatItem.GunAnimation.EQUIP
                ? animation.bipod.x : 80.0D;
        double bipodY = AmatItem.animation(stack) == AmatItem.GunAnimation.EQUIP
                ? animation.bipod.y : 25.0D;
        renderLeftBipod(bipodX, bipodY, poses, buffers, light, overlay);
        renderRightBipod(bipodX, bipodY, poses, buffers, light, overlay);
        renderMuzzle(poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 0.625D, 12.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D,
                WeaponSmokeRenderer.SEVEN_SIX_TWO,
                AmatItem.state(stack) == AmatItem.GunState.RELOADING);
        poses.popPose();
    }

    private void renderLeftBipod(double deployed, double spread, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.3125D, -0.625D, -1.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) spread));
        poses.translate(-0.3125D, 0.625D, 1.0D);
        render("BipodHingeLeft", poses, buffers, light, overlay);
        poses.translate(0.3125D, -0.625D, -1.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) deployed));
        poses.translate(-0.3125D, 0.625D, 1.0D);
        render("BipodLeft", poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderRightBipod(double deployed, double spread, PoseStack poses,
                                  MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(-0.3125D, -0.625D, -1.0D);
        poses.mulPose(Axis.ZN.rotationDegrees((float) spread));
        poses.translate(0.3125D, 0.625D, 1.0D);
        render("BipodHingeRight", poses, buffers, light, overlay);
        poses.translate(-0.3125D, -0.625D, -1.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) deployed));
        poses.translate(0.3125D, 0.625D, 1.0D);
        render("BipodRight", poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        render("Gun", poses, buffers, light, overlay);
        render("Bolt", poses, buffers, light, overlay);
        render("Magazine", poses, buffers, light, overlay);
        render("BipodLeft", poses, buffers, light, overlay);
        render("BipodHingeLeft", poses, buffers, light, overlay);
        render("BipodRight", poses, buffers, light, overlay);
        render("BipodHingeRight", poses, buffers, light, overlay);
        render("Scope", poses, buffers, light, overlay);
        renderMuzzle(poses, buffers, light, overlay);
    }

    private void renderMuzzle(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (!silenced) {
            render("MuzzleBrake", poses, buffers, light, overlay);
            return;
        }
        poses.pushPose();
        poses.translate(0.0D, 0.625D, -4.3125D);
        poses.scale(1.25F, 1.25F, 1.25F);
        attachmentMesh().render("Silencer", poses.last(),
                buffers.getBuffer(RenderType.entityCutout(ATTACHMENTS_TEXTURE)),
                1.0F, light, overlay, -1);
        poses.popPose();
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS, "AMAT");
        }
        return mesh;
    }

    private EnvsuitMesh attachmentMesh() {
        if (attachmentMesh == null) {
            attachmentMesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    G3_MODEL, Set.of("Silencer"), "AMAT silencer");
        }
        return attachmentMesh;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses, boolean silenced) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = silenced ? 0.8175F : 0.9375F;
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(-0.5D, 0.5D, silenced ? -1.0D : 0.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 0.8D, 0.0D, aim),
                        lerp(-0.8D, -0.609375D, aim), lerp(2.6D, 1.875D, aim));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(context, poses);
            case GROUND, FIXED -> {
                poses.scale(0.075F, 0.075F, 0.075F);
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
        poses.translate(0.0D, 0.5D, 6.75D);
    }

    /** Two-sided gap flash with a small rising plume. */
    private static void renderGapFlash(PoseStack poses, MultiBufferSource buffers, long elapsed) {
        float fire = Math.max(0.0F, Math.min(elapsed / 75.0F, 1.0F));
        float height = 4.0F * fire;
        float length = 15.0F * fire;
        float lift = 3.0F * fire;
        float offset = fire;
        float lengthOffset = 0.125F;

        poses.pushPose();
        poses.translate(0.0D, 0.5D, 11.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        VertexConsumer consumer = buffers.getBuffer(GAP_FLASH);
        PoseStack.Pose pose = poses.last();

        vertex(consumer, pose, 0, -height, -offset, 1, 1);
        vertex(consumer, pose, 0, height, -offset, 0, 1);
        vertex(consumer, pose, 0, height + lift, length - offset, 0, 0);
        vertex(consumer, pose, 0, -height + lift, length - offset, 1, 0);

        vertex(consumer, pose, 0, height, offset, 0, 1);
        vertex(consumer, pose, 0, -height, offset, 1, 1);
        vertex(consumer, pose, 0, -height + lift, -length + offset, 1, 0);
        vertex(consumer, pose, 0, height + lift, -length + offset, 0, 0);

        vertex(consumer, pose, 0, -height, -offset, 1, 1);
        vertex(consumer, pose, 0, height, -offset, 0, 1);
        vertex(consumer, pose, lengthOffset, height, length - offset, 0, 0);
        vertex(consumer, pose, lengthOffset, -height, length - offset, 1, 0);

        vertex(consumer, pose, 0, height, offset, 0, 1);
        vertex(consumer, pose, 0, -height, offset, 1, 1);
        vertex(consumer, pose, lengthOffset, -height, -length + offset, 1, 0);
        vertex(consumer, pose, lengthOffset, height, -length + offset, 0, 0);
        poses.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static AmatAnimation animation(ItemStack stack, double time) {
        double turn = -60.0D;
        double pull = -2.5D;
        return switch (AmatItem.animation(stack)) {
            case EQUIP -> new AmatAnimation(
                    seq(time, f(45,0,0,0), f(0,0,0,500,Curve.SIN_FULL)),
                    seq(time, f(0,0,0,500), f(80,0,0,350), f(80,25,0,150)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new AmatAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,600), f(-3,0,0,150,Curve.SIN_DOWN),
                            f(-3,0,0,300), f(0,0,0,250,Curve.SIN_FULL)),
                    seq(time, f(0,0,-0.5,50,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)),
                    seq(time, f(0,0,0,250), f(0,0,turn,150), f(0,0,turn,700), f(0,0,0,150)),
                    seq(time, f(0,0,0,350), f(0,0,pull,250,Curve.SIN_UP),
                            f(0,0,pull,250), f(0,0,0,200)), ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new AmatAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,600), f(-3,0,0,150,Curve.SIN_DOWN),
                            f(-3,0,0,300), f(0,0,0,250,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,0,250), f(0,0,turn,150), f(0,0,turn,700), f(0,0,0,150)),
                    seq(time, f(0,0,0,350), f(0,0,pull,250,Curve.SIN_UP),
                            f(0,0,pull,250), f(0,0,0,200)), ZERO, ZERO, ZERO);
            case RELOAD -> new AmatAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,1000), f(-2,0,0,150,Curve.SIN_DOWN),
                            f(0,0,0,250,Curve.SIN_FULL), f(0,0,0,450),
                            f(-3,0,0,150,Curve.SIN_DOWN), f(-3,0,0,300),
                            f(0,0,0,250,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,0,1500), f(0,0,turn,150),
                            f(0,0,turn,700), f(0,0,0,150)),
                    seq(time, f(0,0,0,1600), f(0,0,pull,250,Curve.SIN_UP),
                            f(0,0,pull,250), f(0,0,0,200)),
                    seq(time, f(0,-10,0,350,Curve.SIN_UP), f(0,0,0,650,Curve.SIN_UP)),
                    ZERO, ZERO);
            case JAMMED -> new AmatAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,250), f(-15,0,0,500,Curve.SIN_FULL),
                            f(-15,0,0,900), f(0,0,0,500,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,0,250), f(0,0,turn,150),
                            f(0,0,turn,850), f(0,0,0,150)),
                    seq(time, f(0,0,0,350), f(0,0,pull,250,Curve.SIN_UP),
                            f(0,0,0,200), f(0,0,pull,250,Curve.SIN_UP), f(0,0,0,200)),
                    ZERO, ZERO, ZERO);
            case INSPECT -> new AmatAnimation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                    seq(time, f(0,0.5,0,100,Curve.SIN_FULL), f(4,-2,0,500,Curve.SIN_FULL),
                            f(4,-2.5,0,100), f(4,7,0,350,Curve.SIN_FULL),
                            f(4,-2.5,0,350,Curve.SIN_DOWN), f(4,-2,0,100),
                            f(4,-2,0,250), f(0,0.5,0,500,Curve.SIN_FULL),
                            f(0,0,0,250,Curve.SIN_FULL)),
                    seq(time, f(0,0,0,700), f(-360,0,0,700)));
            default -> AmatAnimation.NONE;
        };
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (AmatItem.animationTimer(stack) + partial) * 50.0D;
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, double degrees) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poses.translate(-x, -y, -z);
    }

    private static Vec seq(double time, Frame... frames) {
        double elapsed = 0.0D;
        Vec previous = ZERO;
        for (Frame frame : frames) {
            if (frame.duration <= 0.0D) {
                previous = frame.value;
                continue;
            }
            if (time < elapsed + frame.duration) {
                double progress = Math.max(0.0D, Math.min((time - elapsed) / frame.duration, 1.0D));
                return previous.lerp(frame.value, frame.curve.apply(progress));
            }
            elapsed += frame.duration;
            previous = frame.value;
        }
        return previous;
    }

    private static Frame f(double x, double y, double z, double duration) {
        return f(x, y, z, duration, Curve.LINEAR);
    }
    private static Frame f(double x, double y, double z, double duration, Curve curve) {
        return new Frame(new Vec(x, y, z), duration, curve);
    }
    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static final Vec ZERO = new Vec(0,0,0);
    private record Vec(double x, double y, double z) {
        Vec lerp(Vec other, double progress) {
            return new Vec(AmatItemRenderer.lerp(x, other.x, progress),
                    AmatItemRenderer.lerp(y, other.y, progress),
                    AmatItemRenderer.lerp(z, other.z, progress));
        }
    }
    private record Frame(Vec value, double duration, Curve curve) { }
    private enum Curve {
        LINEAR { double apply(double x) { return x; } },
        SIN_UP { double apply(double x) { return 1.0D - Math.cos(x * Math.PI * 0.5D); } },
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }
    private record AmatAnimation(Vec equip, Vec bipod, Vec lift, Vec recoil, Vec boltTurn,
                                 Vec boltPull, Vec mag, Vec scopeThrow, Vec scopeSpin) {
        private static final AmatAnimation NONE = new AmatAnimation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
