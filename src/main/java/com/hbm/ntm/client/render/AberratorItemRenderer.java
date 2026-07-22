package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.AberratorItem;
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

public final class AberratorItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/aberrator.obj");
    private static final ResourceLocation TEXTURE = id("textures/models/weapons/aberrator.png");
    private static final Set<String> GROUPS = Set.of(
            "Gun", "Sight", "Magazine", "Bullet", "Slide", "Hammer");
    private EnvsuitMesh mesh;

    public AberratorItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof AberratorItem)) return;
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND ->
                    renderFirstPerson(stack, poses, buffers, light, overlay);
            case THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                renderStatic(poses, buffers, light, overlay);
                renderEffects(stack, 0, poses, buffers);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                setupThirdPerson(context, poses);
                renderStatic(poses, buffers, light, overlay);
                renderEffects(stack, 0, poses, buffers);
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
        poses.translate(0.0D, 0.0D, 0.875D);
        float offset = 0.8F;
        poses.pushPose();
        poses.translate(-offset, -1.25D * offset, 1.25D * offset);
        poses.scale(0.25F, 0.25F, 0.25F);
        renderAnimated(stack, 0, 1, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderAnimated(ItemStack stack, int index, int direction, PoseStack poses,
                                MultiBufferSource buffers, int light, int overlay) {
        Animation animation = animation(stack, index);
        poses.translate(0.0D, animation.rise.y, 0.0D);

        poses.translate(0.0D, 1.0D, -2.25D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.equip.x));
        poses.translate(0.0D, -1.0D, 2.25D);

        poses.translate(0.0D, -1.0D, -4.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.recoil.x));
        poses.translate(0.0D, 1.0D, 4.0D);

        poses.translate(0.0D, 1.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.roll.z * direction));
        poses.translate(0.0D, -1.0D, 0.0D);

        poses.pushPose();
        poses.translate(0.0D, 2.0D, 4.0D);
        poses.mulPose(Axis.XN.rotationDegrees((float) animation.recoil.x));
        poses.mulPose(Axis.ZN.rotationDegrees((float) animation.roll.z * direction));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, index, poses, buffers, 0.5D,
                WeaponSmokeRenderer.STANDARD,
                AberratorItem.state(stack, index) == AberratorItem.GunState.RELOADING);
        poses.popPose();

        render("Gun", poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 2.4375D, -1.9375D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.sight.x));
        poses.translate(0.0D, -2.4375D, 1.9375D);
        render("Sight", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x * direction, animation.mag.y, animation.mag.z);
        poses.translate(0.0D, 1.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.magroll.z * direction));
        poses.translate(0.0D, -1.0D, 0.0D);
        render("Magazine", poses, buffers, light, overlay);
        poses.translate(animation.bullet.x, animation.bullet.y, animation.bullet.z);
        render("Bullet", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.slide.z);
        render("Slide", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 1.25D, -3.625D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (-45.0D + animation.hammer.x)));
        poses.translate(0.0D, -1.25D, 3.625D);
        render("Hammer", poses, buffers, light, overlay);
        poses.popPose();

        renderEffects(stack, index, poses, buffers);
    }

    private void renderStatic(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        render("Gun", poses, buffers, light, overlay);
        render("Hammer", poses, buffers, light, overlay);
        render("Magazine", poses, buffers, light, overlay);
        render("Slide", poses, buffers, light, overlay);
        render("Sight", poses, buffers, light, overlay);
    }

    private void renderInventory(PoseStack poses, MultiBufferSource buffers,
                                 int light, int overlay) {
        poses.scale(1.0F, -1.0F, -1.0F);
        poses.scale(2.5F / 16.0F, 2.5F / 16.0F, 2.5F / 16.0F);
        poses.translate(0.0D, 1.0D, 0.0D);

        poses.pushPose();
        poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(25.0F));
        poses.mulPose(Axis.YP.rotationDegrees(45.0F));
        poses.translate(-1.0D, 0.0D, 0.0D);
        renderStatic(poses, buffers, light, overlay);
        poses.popPose();

        poses.translate(0.0D, 0.0D, 5.0D);
        poses.pushPose();
        poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        poses.mulPose(Axis.XN.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(25.0F));
        poses.mulPose(Axis.YN.rotationDegrees(45.0F));
        poses.translate(1.0D, 0.0D, 0.0D);
        renderStatic(poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderDropped(PoseStack poses, MultiBufferSource buffers,
                               int light, int overlay) {
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        poses.pushPose();
        poses.translate(-1.0D, 1.0D, 0.0D);
        renderStatic(poses, buffers, light, overlay);
        poses.popPose();
        poses.pushPose();
        poses.translate(1.0D, 1.0D, 0.0D);
        renderStatic(poses, buffers, light, overlay);
        poses.popPose();
    }

    private static void setupThirdPerson(ItemDisplayContext context, PoseStack poses) {
        boolean left = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        float side = left ? -1.0F : 1.0F;
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
        poses.mulPose(Axis.XP.rotationDegrees(left ? 10.0F : 15.0F));
        poses.translate(side * (left ? 5.0D : 3.5D), 0.0D, 0.0D);
        poses.translate(0.0D, -1.0D, 4.0D);
        poses.scale(1.5F, 1.5F, 1.5F);
    }

    private static void renderEffects(ItemStack stack, int index, PoseStack poses,
                                      MultiBufferSource buffers) {
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack, index);
        if (elapsed >= 0L && elapsed < 75L) {
            poses.pushPose();
            poses.translate(0.0D, 2.0D, 4.0D);
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            poses.mulPose(Axis.XP.rotationDegrees(
                    90.0F * ClientWeaponEvents.shotRandom(stack, index)));
            poses.scale(0.75F, 0.75F, 0.75F);
            SednaMuzzleFlash.render(poses, buffers, elapsed / 75.0F, 7.5F);
            poses.popPose();
        }
        if (elapsed >= 0L && elapsed < 150L) {
            poses.pushPose();
            poses.translate(0.0D, 2.0D, -1.5D);
            poses.scale(0.5F, 0.5F, 0.5F);
            SednaMuzzleFlash.renderFireball(poses, buffers, elapsed / 150.0F);
            poses.popPose();
        }
    }

    private void render(String group, PoseStack poses, MultiBufferSource buffers,
                        int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Aberrator");
        }
        return mesh;
    }

    private static Animation animation(ItemStack stack, int index) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (AberratorItem.animationTimer(stack, index) + partial) * 50.0D;
        int ammo = AberratorItem.rounds(stack, 0);
        return switch (AberratorItem.animation(stack, index)) {
            case EQUIP -> new Animation(
                    sequence(time, frame(360, 0, 0, 0), frame(0, 0, 0, 500, Curve.SIN_FULL)),
                    sequence(time, frame(0, -3, 0, 0), frame(0, 0, 0, 500, Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Animation(ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 50), frame(-25, 0, 0, 100, Curve.SIN_DOWN),
                            frame(0, 0, 0, 500, Curve.SIN_FULL)),
                    sequence(time, frame(0, 0, 0, 50), frame(15, 0, 0, 100, Curve.SIN_DOWN),
                            frame(0, 0, 0, 250, Curve.SIN_FULL)),
                    sequence(time, frame(0, 0, 0, 50), frame(0, 0, -1.125, 50, Curve.SIN_DOWN),
                            frame(0, 0, -1.125, 50), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    ammo <= 1 ? ZERO : sequence(time, frame(0, 0.375, 1.125, 150, Curve.SIN_UP)),
                    sequence(time, frame(45, 0, 0, 50),
                            frame(-45, 0, -1.125, 50, Curve.SIN_DOWN),
                            frame(-20, 0, -1.125, 50), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new Animation(ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 700), frame(-5, 0, 0, 100, Curve.SIN_FULL),
                            frame(0, 0, 0, 250, Curve.SIN_FULL)), ZERO,
                    sequence(time, frame(0, 0, 0, 550),
                            frame(0, 0, -1.125, 150, Curve.SIN_FULL),
                            frame(0, 0, -1.125, 50), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    ZERO,
                    sequence(time, frame(45, 0, 0, 50), frame(45, 0, 0, 500),
                            frame(-45, 0, -1.125, 150, Curve.SIN_FULL),
                            frame(-20, 0, -1.125, 50), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    ZERO, ZERO, ZERO);
            case RELOAD -> new Animation(
                    sequence(time, frame(0, 0, 0, 750), frame(5, 0, 0, 150, Curve.SIN_FULL),
                            frame(-190, 0, 0, 500, Curve.SIN_FULL), frame(-190, 0, 0, 450),
                            frame(-360, 0, 0, 350, Curve.SIN_DOWN), frame(0, 0, 0, 0)),
                    ZERO,
                    sequence(time, frame(0, 0, 0, 2350), frame(-5, 0, 0, 100, Curve.SIN_FULL),
                            frame(0, 0, 0, 250, Curve.SIN_FULL)), ZERO,
                    sequence(time, frame(0, 0, 0, 2200),
                            frame(0, 0, -1.125, 150, Curve.SIN_FULL),
                            frame(0, 0, -1.125, 50), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    sequence(time, frame(ammo > 0 ? 0 : -100, 0, 0, 0),
                            frame(ammo > 0 ? 0 : -100, 0, 0, 2400), frame(0, 0, 0, 0),
                            frame(0, 0.375, 1.125, 150, Curve.SIN_UP)),
                    sequence(time, frame(0, 0, 0, 2250),
                            frame(-45, 0, -1.125, 100, Curve.SIN_FULL),
                            frame(-20, 0, -1.125, 50), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    sequence(time, frame(0, 0, 20, 150, Curve.SIN_FULL),
                            frame(0, 0, 20, 50), frame(0, 0, -45, 150, Curve.SIN_UP),
                            frame(0, 0, 0, 150, Curve.SIN_FULL)),
                    sequence(time, frame(0, 0, 0, 350), frame(0, -2, 0, 0),
                            frame(-15, -5, 0, 350), frame(-15, 0, 0, 0),
                            frame(-15, 0, 0, 700), frame(3, 3, 0, 0),
                            frame(0, -2, 0, 250, Curve.SIN_DOWN), frame(0, -2, 0, 50),
                            frame(0, 0, 0, 150, Curve.SIN_DOWN)),
                    sequence(time, frame(0, 0, 0, 350),
                            frame(0, 0, -180, 250), frame(0, 0, 0, 0)));
            case INSPECT -> new Animation(
                    sequence(time, frame(0, 0, 0, 0),
                            frame(-720, 0, 0, 1000, Curve.SIN_FULL),
                            frame(-720, 0, 0, 250), frame(0, 0, 0, 1000, Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
        };
    }

    private static Vec sequence(double time, Frame... frames) {
        double elapsed = 0.0D;
        Vec previous = ZERO;
        for (Frame frame : frames) {
            if (frame.duration <= 0.0D) {
                previous = frame.value;
                continue;
            }
            if (time < elapsed + frame.duration) {
                double progress = Math.max(0.0D,
                        Math.min((time - elapsed) / frame.duration, 1.0D));
                return previous.lerp(frame.value, frame.curve.apply(progress));
            }
            elapsed += frame.duration;
            previous = frame.value;
        }
        return previous;
    }

    private static Frame frame(double x, double y, double z, double duration) {
        return frame(x, y, z, duration, Curve.LINEAR);
    }

    private static Frame frame(double x, double y, double z, double duration, Curve curve) {
        return new Frame(new Vec(x, y, z), duration, curve);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static final Vec ZERO = new Vec(0.0D, 0.0D, 0.0D);
    private record Vec(double x, double y, double z) {
        private Vec lerp(Vec other, double amount) {
            return new Vec(x + (other.x - x) * amount, y + (other.y - y) * amount,
                    z + (other.z - z) * amount);
        }
    }
    private record Frame(Vec value, double duration, Curve curve) { }
    private enum Curve {
        LINEAR { @Override double apply(double x) { return x; } },
        SIN_UP { @Override double apply(double x) { return 1.0D - Math.cos(x * Math.PI * 0.5D); } },
        SIN_DOWN { @Override double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { @Override double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }
    private record Animation(Vec equip, Vec rise, Vec recoil, Vec sight, Vec slide,
                             Vec bullet, Vec hammer, Vec roll, Vec mag, Vec magroll) { }
}
