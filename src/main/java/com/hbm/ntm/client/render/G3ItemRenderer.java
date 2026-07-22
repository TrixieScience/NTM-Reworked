package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.G3Item;
import com.hbm.ntm.weapon.WeaponModManager;
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

/** G3 model groups driven by the LAMBDA_G3 animation railway. */
public final class G3ItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/g3.obj");
    private static final ResourceLocation STANDARD_TEXTURE = id("textures/models/weapons/g3.png");
    private static final ResourceLocation ZEBRA_TEXTURE = id("textures/models/weapons/g3_zebra.png");
    private static final ResourceLocation ATTACHMENTS_TEXTURE =
            id("textures/models/weapons/g3_attachments.png");
    private static final Set<String> GROUPS = Set.of(
            "Rifle", "Bullet", "Guide_And_Bolt", "Handle", "Plug", "Mag_Paddle",
            "Magazine", "Stock", "Flash_Hider", "Scope", "Silencer", "Selector", "Trigger");

    private EnvsuitMesh mesh;

    public G3ItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof G3Item)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        boolean zebra = ((G3Item) stack.getItem()).variant() == G3Item.Variant.ZEBRA;
        boolean silenced = zebra || WeaponModManager.hasMod(stack, 0, WeaponModManager.SILENCER);
        boolean scoped = zebra || WeaponModManager.hasMod(stack, 0, WeaponModManager.SCOPE);
        boolean stocked = G3Item.hasStock(stack);
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);

        poses.pushPose();
        setupContext(context, poses, scoped, silenced, stocked);
        if (!(firstPerson && scoped && ClientWeaponEvents.fullyAimed())) {
            if (firstPerson) renderFirstPerson(stack, poses, buffers, light, overlay,
                    zebra, silenced, scoped, stocked);
            else renderStatic(stack, poses, buffers, light, overlay, zebra, silenced, scoped, stocked);
        }
        if (!silenced && held && !(firstPerson && scoped && ClientWeaponEvents.fullyAimed())
                && elapsed >= 0L && elapsed < 75L) {
            renderFlash(poses, buffers, elapsed, ClientWeaponEvents.shotRandom(stack));
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay, boolean zebra, boolean silenced,
                                   boolean scoped, boolean stocked) {
        G3Animation animation = animation(stack, animationTime(stack));
        poses.scale(0.375F, 0.375F, 0.375F);
        pivotX(poses, 0.0D, -2.0D, -6.0D, animation.equip.x);
        pivotX(poses, 0.0D, 0.0D, -4.0D, animation.lift.x);
        poses.translate(0.0D, 0.0D, animation.recoil.z);

        renderBody("Rifle", zebra, poses, buffers, light, overlay);
        if (stocked) renderBody("Stock", zebra, poses, buffers, light, overlay);
        if (!silenced) renderBody("Flash_Hider", false, poses, buffers, light, overlay);
        renderBody("Trigger", zebra, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        poses.translate(0.0D, -1.75D, -0.5D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.speen.z));
        poses.mulPose(Axis.YP.rotationDegrees((float) animation.speen.y));
        poses.translate(0.0D, 1.75D, 0.5D);
        renderBody("Magazine", zebra, poses, buffers, light, overlay);
        if (animation.bullet.x == 0.0D) renderBody("Bullet", zebra, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.bolt.z);
        renderBody("Guide_And_Bolt", zebra, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.625D, animation.plug.z);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.handle.z));
        poses.translate(0.0D, -0.625D, 0.0D);
        renderBody("Plug", zebra, poses, buffers, light, overlay);
        poses.translate(0.0D, 0.625D, 5.25D);
        poses.mulPose(Axis.ZP.rotationDegrees(22.5F));
        poses.mulPose(Axis.YP.rotationDegrees((float) animation.handle.y));
        poses.mulPose(Axis.ZN.rotationDegrees(22.5F));
        poses.translate(0.0D, -0.625D, -5.25D);
        renderBody("Handle", zebra, poses, buffers, light, overlay);
        poses.popPose();

        renderSelector(G3Item.mode(stack), zebra, poses, buffers, light, overlay);
        if (silenced || scoped) {
            if (silenced) render("Silencer", ATTACHMENTS_TEXTURE, poses, buffers, light, overlay);
            if (scoped) render("Scope", ATTACHMENTS_TEXTURE, poses, buffers, light, overlay);
        }
        if (!silenced) {
            poses.pushPose();
            poses.translate(0.0D, 0.0D, 13.0D);
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            poses.scale(0.75F, 0.75F, 0.75F);
            WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.5D,
                    WeaponSmokeRenderer.SEVEN_SIX_TWO,
                    G3Item.state(stack) == G3Item.GunState.RELOADING);
            poses.popPose();
        }
    }

    private void renderStatic(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                              int light, int overlay, boolean zebra, boolean silenced,
                              boolean scoped, boolean stocked) {
        renderBody("Rifle", zebra, poses, buffers, light, overlay);
        if (stocked) renderBody("Stock", zebra, poses, buffers, light, overlay);
        renderBody("Magazine", zebra, poses, buffers, light, overlay);
        if (!silenced) renderBody("Flash_Hider", false, poses, buffers, light, overlay);
        renderBody("Guide_And_Bolt", zebra, poses, buffers, light, overlay);
        renderBody("Handle", zebra, poses, buffers, light, overlay);
        renderBody("Trigger", zebra, poses, buffers, light, overlay);
        // Third person always shows selector pose zero.
        renderSelector(0, zebra, poses, buffers, light, overlay);
        if (silenced || scoped) {
            if (silenced) render("Silencer", ATTACHMENTS_TEXTURE, poses, buffers, light, overlay);
            if (scoped) render("Scope", ATTACHMENTS_TEXTURE, poses, buffers, light, overlay);
        }
    }

    private void renderSelector(int mode, boolean zebra, PoseStack poses, MultiBufferSource buffers,
                                int light, int overlay) {
        poses.pushPose();
        poses.translate(0.0D, -0.875D, -3.5D);
        poses.mulPose(Axis.XN.rotationDegrees(30.0F * (1 - mode)));
        poses.translate(0.0D, 0.875D, 3.5D);
        renderBody("Selector", zebra, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderBody(String group, boolean zebra, PoseStack poses, MultiBufferSource buffers,
                            int light, int overlay) {
        render(group, zebra ? ZEBRA_TEXTURE : STANDARD_TEXTURE, poses, buffers, light, overlay);
    }

    private void render(String group, ResourceLocation texture, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS, "G3");
        }
        return mesh;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses, boolean scoped,
                                     boolean silenced, boolean stocked) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = stocked ? 0.875F : 1.125F;
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(silenced ? (stocked ? 50.0F : 55.0F) : 45.0F));
                poses.translate(stocked ? (silenced ? 0.75D : -0.5D) : 2.5D, 0.5D, 0.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.translate(0.0D, 0.0D, 0.875D);
                Vec hip = new Vec(side, -0.8D, 2.2D);
                Vec aimed = scoped
                        ? new Vec(0.0D, -0.69140625D, 1.46875D)
                        : new Vec(0.0D, -0.4453125D, 1.75D);
                poses.translate(lerp(hip.x, aimed.x, aim), lerp(hip.y, aimed.y, aim),
                        lerp(hip.z, aimed.z, aim));
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
        poses.translate(0.0D, 2.0D, 4.0D);
    }

    private static void renderFlash(PoseStack poses, MultiBufferSource buffers, long elapsed, float random) {
        poses.pushPose();
        poses.translate(0.0D, 0.0D, 12.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(-25.0F + random * 10.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        SednaMuzzleFlash.render(poses, buffers, elapsed / 75.0F, 10.0F);
        poses.popPose();
    }

    private static G3Animation animation(ItemStack stack, double time) {
        boolean empty = G3Item.rounds(stack) <= 0;
        boolean stocked = G3Item.hasStock(stack);
        return switch (G3Item.animation(stack)) {
            case EQUIP -> new G3Animation(
                    seq(time, f(45,0,0,0), f(0,0,0,stocked ? 500 : 250,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new G3Animation(ZERO, ZERO,
                    seq(time, f(0,0,!stocked && !G3Item.aiming(stack) ? -0.75 : -0.25,
                                    25,Curve.SIN_DOWN), f(0,0,0,75,Curve.SIN_FULL)),
                    ZERO, ZERO,
                    seq(time, f(0,0,0,20), f(0,0,-4.5,40), f(0,0,0,40)),
                    ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new G3Animation(ZERO,
                    seq(time, f(0,0,0,400), f(-1,0,0,100,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO,
                    seq(time, f(0,0,0,250), f(0,0,-0.3125,100), f(0,0,-0.3125,25),
                            f(0,0,-2.75,130), f(0,0,-2.75,50), f(0,0,-2.4375,50), f(0,0,0,85)),
                    seq(time, f(0,0,0,375), f(0,0,-2.4375,130), f(0,0,-2.4375,100), f(0,0,0,85)),
                    seq(time, f(0,0,0,250), f(0,90,0,100), f(0,90,0,205), f(0,0,0,50)), ZERO);
            case RELOAD -> new G3Animation(ZERO,
                    seq(time, f(0,0,0,750), f(-25,0,0,500,Curve.SIN_FULL), f(-25,0,0,300),
                            f(-26,0,0,100,Curve.SIN_DOWN), f(-25,0,0,100,Curve.SIN_FULL),
                            f(-25,0,0,250), f(0,0,0,500,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,-8,0,250,Curve.SIN_UP), f(0,-8,0,1050), f(0,0,0,250)), ZERO,
                    seq(time, f(0,0,0,200), f(0,0,-0.3125,100), f(0,0,-0.3125,10),
                            f(0,0,-3.25,200), f(0,0,-3.25,1365), f(0,0,-2.9375,50), f(0,0,0,100)),
                    seq(time, f(0,0,0,310), f(0,0,-2.9375,200), f(0,0,-2.9375,1415), f(0,0,0,100)),
                    seq(time, f(0,0,0,200), f(0,90,0,100), f(0,90,0,210), f(0,90,45,75),
                            f(0,90,45,1190), f(0,90,0,100), f(0,0,0,50)),
                    seq(time, f(empty ? 1 : 0,0,0,0), f(0,0,0,1000)));
            case INSPECT -> new G3Animation(ZERO,
                    seq(time, f(0,0,0,1450), f(-2,0,0,100,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)),
                    ZERO,
                    seq(time, f(0,-1,0,150), f(2,-1,0,150), f(2,8,0,350,Curve.SIN_DOWN),
                            f(2,-2,0,350,Curve.SIN_UP), f(2,-1,0,50), f(2,-1,0,100),
                            f(0,-1,0,150,Curve.SIN_FULL), f(0,0,0,150,Curve.SIN_UP)),
                    seq(time, f(0,0,0,300), f(0,360,360,700)), ZERO, ZERO, ZERO,
                    new Vec(empty ? 1 : 0,0,0));
            case JAMMED -> new G3Animation(ZERO,
                    seq(time, f(0,0,0,500), f(-25,0,0,250,Curve.SIN_FULL), f(-25,0,0,1250),
                            f(0,0,0,350,Curve.SIN_FULL)), ZERO, ZERO, ZERO,
                    seq(time, f(0,0,0,1000), f(0,0,-3.25,150), f(0,0,0,100),
                            f(0,0,0,250), f(0,0,-3.25,150), f(0,0,0,100)),
                    seq(time, f(0,0,0,1000), f(0,0,-3.25,150), f(0,0,0,100),
                            f(0,0,0,250), f(0,0,-3.25,150), f(0,0,0,100)), ZERO, ZERO);
            default -> G3Animation.NONE;
        };
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, double degrees) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poses.translate(-x, -y, -z);
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (G3Item.animationTimer(stack) + partial) * 50.0D;
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
            return new Vec(G3ItemRenderer.lerp(x, other.x, progress),
                    G3ItemRenderer.lerp(y, other.y, progress), G3ItemRenderer.lerp(z, other.z, progress));
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
    private record G3Animation(Vec equip, Vec lift, Vec recoil, Vec mag, Vec speen,
                               Vec bolt, Vec plug, Vec handle, Vec bullet) {
        private static final G3Animation NONE = new G3Animation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
