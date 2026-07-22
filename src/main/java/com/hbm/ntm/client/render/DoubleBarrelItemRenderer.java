package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.DoubleBarrelItem;
import com.hbm.ntm.item.SacredDragonItem;
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

public final class DoubleBarrelItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = id("models/weapons/sacred_dragon.obj");
    private static final ResourceLocation TEXTURE_CLASSIC = id("textures/models/weapons/double_barrel.png");
    private static final ResourceLocation TEXTURE_SACRED_DRAGON = id(
            "textures/models/weapons/double_barrel_sacred_dragon.png");
    private static final Set<String> GROUPS = Set.of(
            "Stock", "BarrelShort", "Barrel", "Buckle", "Lever", "Shells");
    private EnvsuitMesh mesh;

    public DoubleBarrelItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!supports(stack)) return;
        boolean first = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = first || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        poses.pushPose();
        setupContext(stack, context, poses);
        if (first) renderFirstPerson(stack, poses, buffers, light, overlay);
        else renderStatic(stack, poses, buffers, light, overlay);
        if (held) renderMuzzleFlash(stack, poses, buffers);
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                   int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (animationTimer(stack) + partial) * 50.0D;
        Animation animation = animation(stack, time);

        poses.translate(animation.recoil.x * 3.0D, animation.recoil.y, animation.recoil.z);
        poses.mulPose(Axis.XP.rotationDegrees((float) (animation.recoil.z * 10.0D)));

        poses.translate(0.0D, 0.0D, -4.0D);
        poses.mulPose(Axis.XN.rotationDegrees((float) animation.equip.x));
        poses.translate(0.0D, 0.0D, 4.0D);

        poses.translate(0.0D, 0.0D, -4.0D);
        poses.mulPose(Axis.YP.rotationDegrees((float) animation.turn.y));
        poses.translate(0.0D, 0.0D, 4.0D);

        poses.translate(0.0D, 0.0D, -4.0D);
        poses.mulPose(Axis.XN.rotationDegrees((float) animation.lift.x));
        poses.translate(0.0D, 0.0D, 4.0D);

        render("Stock", stack, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, -0.4375D, -0.875D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.barrel.x));
        poses.translate(0.0D, 0.4375D, 0.875D);
        render("BarrelShort", stack, poses, buffers, light, overlay);
        if (!isSawedOff(stack)) render("Barrel", stack, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.75D, 0.0D, -0.6875D);
        poses.mulPose(Axis.YP.rotationDegrees((float) animation.buckle.y));
        poses.translate(-0.75D, 0.0D, 0.6875D);
        render("Buckle", stack, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(-0.3125D, 0.3125D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.lever.z));
        poses.translate(0.3125D, -0.3125D, 0.0D);
        render("Lever", stack, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.shells.x, animation.shells.y, animation.shells.z);
        poses.translate(0.0D, 0.0D, -1.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.shellFlip.x));
        poses.translate(0.0D, 0.0D, 1.0D);
        render("Shells", stack, poses, buffers, light, overlay);
        poses.popPose();
        poses.popPose();
    }

    private void renderStatic(ItemStack stack, PoseStack poses, MultiBufferSource buffers, int light,
                             int overlay) {
        for (String group : GROUPS) {
            if ("Barrel".equals(group) && isSawedOff(stack)) continue;
            render(group, stack, poses, buffers, light, overlay);
        }
    }

    private static void renderMuzzleFlash(ItemStack stack, PoseStack poses,
                                          MultiBufferSource buffers) {
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        if (elapsed < 0L || elapsed >= 75L) return;
        poses.pushPose();
        poses.translate(0.0D, 0.0D, 8.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F * ClientWeaponEvents.shotRandom(stack)));
        poses.scale(2.0F, 2.0F, 2.0F);
        SednaMuzzleFlash.render(poses, buffers, elapsed / 75.0F, 5.0F);
        poses.popPose();
    }

    private void render(String group, ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                        int light, int overlay) {
        mesh().render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture(stack))),
                1.0F, light, overlay, -1);
    }

    private static boolean supports(ItemStack stack) {
        return stack.getItem() instanceof DoubleBarrelItem
                || stack.getItem() instanceof SacredDragonItem;
    }

    private static boolean isSacredDragon(ItemStack stack) {
        return stack.getItem() instanceof SacredDragonItem;
    }

    private static boolean isSawedOff(ItemStack stack) {
        return isSacredDragon(stack) || stack.getItem() instanceof DoubleBarrelItem
                && DoubleBarrelItem.isSawedOff(stack);
    }

    private static double animationTimer(ItemStack stack) {
        if (stack.getItem() instanceof DoubleBarrelItem) return DoubleBarrelItem.animationTimer(stack);
        if (stack.getItem() instanceof SacredDragonItem) return SacredDragonItem.animationTimer(stack);
        return 0.0D;
    }

    private static Animation animation(ItemStack stack, double time) {
        if (stack.getItem() instanceof DoubleBarrelItem) {
            return animation(DoubleBarrelItem.animation(stack), time);
        }
        if (stack.getItem() instanceof SacredDragonItem) {
            return animation(SacredDragonItem.animation(stack), time);
        }
        return Animation.NONE;
    }

    private static ResourceLocation texture(ItemStack stack) {
        return isSacredDragon(stack) ? TEXTURE_SACRED_DRAGON : TEXTURE_CLASSIC;
    }

    private EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "Double Barrel");
        }
        return mesh;
    }

    private static void setupContext(ItemStack stack, ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = isSawedOff(stack) ? 2.0F : 1.375F;
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(isSawedOff(stack) ? -2.0D : 0.0D, 0.5D, 0.0D);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(-1.0D, 0.0D, aim), lerp(-0.8D, -0.25D, aim),
                        lerp(1.6D, 1.0D, aim));
                poses.scale(0.375F, 0.375F, 0.375F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                setupThirdPerson(context, poses);
                poses.scale(1.75F, 1.75F, 1.75F);
                poses.translate(0.0D, 1.0D, 3.0D);
            }
            case GROUND, FIXED -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            default -> {
                poses.scale(0.125F, 0.125F, 0.125F);
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
    }

    private static Animation animation(DoubleBarrelItem.GunAnimation type, double time) {
        return switch (type) {
            case EQUIP -> new Animation(
                    sequence(time, frame(-60, 0, 0, 0), frame(0, 0, -3, 500, Curve.SIN_DOWN)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Animation(ZERO,
                    sequence(time, frame(0, 0, -1, 50), frame(0, 0, 0, 250)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0, -60, 0, 50), frame(0, 0, 0, 250)));
            case RELOAD -> new Animation(ZERO, ZERO,
                    sequence(time, frame(0, 30, 0, 350, Curve.SIN_FULL),
                            frame(0, 30, 0, 1_150), frame(0, 0, 0, 350, Curve.SIN_FULL)),
                    sequence(time, frame(0, 0, 0, 300), frame(60, 0, 0, 150, Curve.SIN_UP),
                            frame(60, 0, 0, 1_150), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    sequence(time, frame(0, 0, 0, 350), frame(-5, 0, 0, 150, Curve.SIN_FULL),
                            frame(0, 0, 0, 100, Curve.SIN_FULL), frame(0, 0, 0, 700),
                            frame(-5, 0, 0, 100, Curve.SIN_FULL), frame(0, 0, 0, 100, Curve.SIN_UP),
                            frame(45, 0, 0, 150), frame(45, 0, 0, 150),
                            frame(-5, 0, 0, 150, Curve.SIN_DOWN), frame(0, 0, 0, 100, Curve.SIN_FULL)),
                    sequence(time, frame(0, 0, 0, 450), frame(0, 0, -2.5, 100),
                            frame(0, -5, -5, 350, Curve.SIN_DOWN), frame(0, -3, -2, 0),
                            frame(0, 0, -2, 250), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    sequence(time, frame(0, 0, 0, 450), frame(-360, 0, 0, 450),
                            frame(0, 0, 0, 0)),
                    sequence(time, frame(0, 0, 0, 250), frame(0, 0, -90, 100, Curve.SIN_FULL),
                            frame(0, 0, -90, 1_300), frame(0, 0, 0, 100, Curve.SIN_FULL)), ZERO);
            case INSPECT -> new Animation(ZERO, ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 300), frame(60, 0, 0, 150, Curve.SIN_UP),
                            frame(60, 0, 0, 650), frame(0, 0, 0, 150, Curve.SIN_UP)),
                    sequence(time, frame(0, 0, 0, 350), frame(-5, 0, 0, 150, Curve.SIN_FULL),
                            frame(0, 0, 0, 100, Curve.SIN_FULL), frame(0, 0, 0, 200),
                            frame(-5, 0, 0, 100, Curve.SIN_FULL), frame(0, 0, 0, 100, Curve.SIN_UP),
                            frame(45, 0, 0, 150), frame(45, 0, 0, 150),
                            frame(-5, 0, 0, 150, Curve.SIN_DOWN), frame(0, 0, 0, 100, Curve.SIN_FULL)),
                    ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 250), frame(0, 0, -90, 100, Curve.SIN_FULL),
                            frame(0, 0, -90, 800), frame(0, 0, 0, 100, Curve.SIN_FULL)), ZERO);
            default -> Animation.NONE;
        };
    }

    private static Animation animation(SacredDragonItem.GunAnimation type, double time) {
        return animation(DoubleBarrelItem.GunAnimation.valueOf(type.name()), time);
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
                double progress = Math.max(0.0D, Math.min((time - elapsed) / frame.duration, 1.0D));
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

    private static double lerp(double from, double to, float amount) {
        return from + (to - from) * amount;
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
    private record Animation(Vec equip, Vec recoil, Vec turn, Vec barrel, Vec lift,
                             Vec shells, Vec shellFlip, Vec lever, Vec buckle) {
        private static final Animation NONE = new Animation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
