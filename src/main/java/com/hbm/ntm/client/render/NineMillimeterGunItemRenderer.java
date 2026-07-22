package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.item.NineMillimeterGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** Exact grouped ItemRenderGreasegun/ItemRenderUzi transforms and XFactory9mm buses. */
public final class NineMillimeterGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation GREASE_GUN = model("greasegun_gun");
    public static final ModelResourceLocation GREASE_STOCK = model("greasegun_stock");
    public static final ModelResourceLocation GREASE_MAGAZINE = model("greasegun_magazine");
    public static final ModelResourceLocation GREASE_BULLET = model("greasegun_bullet");
    public static final ModelResourceLocation GREASE_HANDLE = model("greasegun_handle");
    public static final ModelResourceLocation GREASE_FLAP = model("greasegun_flap");
    public static final ModelResourceLocation UZI_GUN = model("uzi_gun");
    public static final ModelResourceLocation UZI_STOCK_FRONT = model("uzi_stock_front");
    public static final ModelResourceLocation UZI_STOCK_BACK = model("uzi_stock_back");
    public static final ModelResourceLocation UZI_SLIDE = model("uzi_slide");
    public static final ModelResourceLocation UZI_MAGAZINE = model("uzi_magazine");
    public static final ModelResourceLocation UZI_BULLET = model("uzi_bullet");

    public NineMillimeterGunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof NineMillimeterGunItem gun)) return;
        boolean grease = gun.variant() == NineMillimeterGunItem.Variant.GREASE_GUN;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        poses.pushPose();
        setupContext(context, poses, grease);
        if (firstPerson) {
            if (grease) renderGreaseFirstPerson(stack, poses, buffers, packedLight, packedOverlay);
            else renderUziFirstPerson(stack, poses, buffers, packedLight, packedOverlay);
        } else {
            renderStatic(grease, poses, buffers, packedLight, packedOverlay);
        }

        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        if (held && elapsed >= 0L && elapsed < 75L) {
            renderMuzzleFlash(poses, buffers, elapsed / 75.0F, grease);
        }
        poses.popPose();
    }

    private static void renderGreaseFirstPerson(ItemStack stack, PoseStack poses,
                                                 MultiBufferSource buffers, int light, int overlay) {
        GreaseAnimation animation = greaseAnimation(stack);
        pivotRotateX(poses, 0.0D, -3.0D, -3.0D, animation.equip.x);
        pivotRotateX(poses, 0.0D, -3.0D, -3.0D, animation.lift.x);
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        if (ClientWeaponEvents.aimingProgress(partial) < 1.0F) {
            poses.mulPose(Axis.ZP.rotationDegrees((float) animation.turn.z));
        }
        poses.translate(0.0D, 0.0D, animation.recoil.z);

        renderModel(GREASE_GUN, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 0.0D, -4.0D - animation.stock.z);
        renderModel(GREASE_STOCK, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        renderModel(GREASE_MAGAZINE, poses, buffers, light, overlay);
        if (animation.bullet.x != 1.0D) renderModel(GREASE_BULLET, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        pivotRotateX(poses, 0.0D, -1.4375D, -0.125D, animation.handle.x);
        renderModel(GREASE_HANDLE, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.53125D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.flap.z));
        poses.translate(0.0D, -0.5125D, 0.0D);
        renderModel(GREASE_FLAP, poses, buffers, light, overlay);
        poses.popPose();

        boolean reloading = NineMillimeterGunItem.state(stack) == NineMillimeterGunItem.GunState.RELOADING;
        poses.pushPose();
        poses.translate(-0.25D, 0.0D, 1.5D);
        poses.mulPose(Axis.ZN.rotationDegrees((float) animation.turn.z));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.25F, 0.25F, 0.25F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D,
                WeaponSmokeRenderer.NINE_MM, reloading);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, 8.0D);
        poses.mulPose(Axis.ZN.rotationDegrees((float) animation.turn.z));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.25F, 0.25F, 0.25F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D,
                WeaponSmokeRenderer.NINE_MM, reloading);
        poses.popPose();
    }

    private static void renderUziFirstPerson(ItemStack stack, PoseStack poses,
                                              MultiBufferSource buffers, int light, int overlay) {
        UziAnimation animation = uziAnimation(stack);
        poses.translate(animation.yeet.x, animation.yeet.y, animation.yeet.z);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.speen.x));
        pivotRotateX(poses, 0.0D, -2.0D, -4.0D, animation.equip.x);
        pivotRotateX(poses, 0.0D, 0.0D, -6.0D, animation.lift.x);
        poses.translate(0.0D, 0.0D, animation.recoil.z);

        renderModel(UZI_GUN, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 0.3125D, -5.75D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (180.0D - animation.stockFront.x)));
        poses.translate(0.0D, -0.3125D, 5.75D);
        renderModel(UZI_STOCK_FRONT, poses, buffers, light, overlay);
        poses.translate(0.0D, -0.3125D, -3.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (-200.0D - animation.stockBack.x)));
        poses.translate(0.0D, 0.3125D, 3.0D);
        renderModel(UZI_STOCK_BACK, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.slide.z);
        renderModel(UZI_SLIDE, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        renderModel(UZI_MAGAZINE, poses, buffers, light, overlay);
        if (animation.bullet.x == 1.0D) renderModel(UZI_BULLET, poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.75D, 8.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.75D,
                WeaponSmokeRenderer.NINE_MM,
                NineMillimeterGunItem.state(stack) == NineMillimeterGunItem.GunState.RELOADING);
        poses.popPose();
    }

    private static void renderStatic(boolean grease, PoseStack poses, MultiBufferSource buffers,
                                     int light, int overlay) {
        if (grease) {
            renderModel(GREASE_GUN, poses, buffers, light, overlay);
            renderModel(GREASE_STOCK, poses, buffers, light, overlay);
            renderModel(GREASE_MAGAZINE, poses, buffers, light, overlay);
            renderModel(GREASE_BULLET, poses, buffers, light, overlay);
            renderModel(GREASE_HANDLE, poses, buffers, light, overlay);
            renderModel(GREASE_FLAP, poses, buffers, light, overlay);
        } else {
            renderModel(UZI_GUN, poses, buffers, light, overlay);
            renderModel(UZI_STOCK_FRONT, poses, buffers, light, overlay);
            renderModel(UZI_STOCK_BACK, poses, buffers, light, overlay);
            renderModel(UZI_SLIDE, poses, buffers, light, overlay);
            renderModel(UZI_MAGAZINE, poses, buffers, light, overlay);
        }
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses, boolean grease) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.5F / 16.0F, 1.5F / 16.0F, 1.5F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(grease ? -0.5D : 0.0D, grease ? 2.0D : 1.0D, 0.0D);
            }
            case GROUND, FIXED -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(context, poses, grease);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                if (grease) {
                    poses.translate(lerp(-1.2D, 0.0D, aim), lerp(-0.8D, -0.328125D, aim),
                            0.875D + lerp(1.4D, 1.125D, aim));
                    poses.scale(0.375F, 0.375F, 0.375F);
                } else {
                    poses.translate(lerp(-1.4D, 0.0D, aim), lerp(-1.2D, -0.546875D, aim),
                            0.875D + lerp(2.0D, 1.0D, aim));
                    poses.scale(0.25F, 0.25F, 0.25F);
                }
            }
            default -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(ItemDisplayContext context, PoseStack poses, boolean grease) {
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
        poses.translate(0.0D, 1.0D, grease ? 3.0D : 1.0D);
    }

    private static GreaseAnimation greaseAnimation(ItemStack stack) {
        double time = animationTime(stack);
        boolean empty = NineMillimeterGunItem.amountBeforeReload(stack) <= 0;
        return switch (NineMillimeterGunItem.animation(stack)) {
            case EQUIP -> new GreaseAnimation(ZERO,
                    sequence(time, frame(80,0,0,0), frame(80,0,0,500), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,-4,0), frame(0,0,-4,200), frame(0,0,0,300,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new GreaseAnimation(
                    sequence(time, frame(0,0,NineMillimeterGunItem.aiming(stack) ? -0.25D : -0.5D,50,Curve.SIN_DOWN), frame(0,0,0,100,Curve.SIN_FULL)),
                    ZERO, ZERO,
                    sequence(time, frame(0,0,15,100,Curve.SIN_DOWN), frame(0,0,-5,100,Curve.SIN_FULL), frame(0,0,0,50,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new GreaseAnimation(ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,500), frame(-25,0,0,250,Curve.SIN_FULL), frame(-25,0,0,750), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,500), frame(0,0,-45,250,Curve.SIN_FULL), frame(0,0,-45,750), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,750), frame(-90,0,0,250,Curve.SIN_FULL), frame(0,0,0,250,Curve.SIN_FULL)), ZERO, ZERO);
            case RELOAD -> new GreaseAnimation(ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,500), frame(-25,0,0,250,Curve.SIN_FULL), frame(-25,0,0,1750), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,1750), frame(0,0,-45,250,Curve.SIN_FULL), frame(0,0,-45,500), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,2000), frame(-90,0,0,250,Curve.SIN_FULL), frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(0,-8,0,250,Curve.SIN_UP), frame(0,-8,0,750), frame(0,0,0,500,Curve.SIN_DOWN)),
                    sequence(time, frame(empty ? 1 : 0,0,0,0), frame(0,0,0,1000)));
            case JAMMED -> new GreaseAnimation(ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,500), frame(-25,0,0,250,Curve.SIN_FULL), frame(-25,0,0,1500), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,500), frame(0,0,-45,250,Curve.SIN_FULL), frame(0,0,-45,1500), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,750), frame(-90,0,0,250,Curve.SIN_FULL), frame(0,0,0,250,Curve.SIN_FULL), frame(0,0,0,250), frame(-90,0,0,250,Curve.SIN_FULL), frame(0,0,0,250,Curve.SIN_FULL)), ZERO, ZERO);
            case INSPECT -> new GreaseAnimation(ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,300), frame(0,0,180,150), frame(0,0,180,850), frame(0,0,0,150)),
                    ZERO,
                    sequence(time, frame(0,0,-45,150), frame(0,0,45,150), frame(0,0,45,50), frame(0,0,0,250), frame(0,0,0,500), frame(0,0,45,150), frame(0,0,-45,150), frame(0,0,0,150)),
                    ZERO, ZERO, ZERO);
            default -> GreaseAnimation.NONE;
        };
    }

    private static UziAnimation uziAnimation(ItemStack stack) {
        double time = animationTime(stack);
        boolean empty = NineMillimeterGunItem.amountBeforeReload(stack) <= 0;
        return switch (NineMillimeterGunItem.animation(stack)) {
            case EQUIP -> new UziAnimation(ZERO,
                    sequence(time, frame(80,0,0,0), frame(80,0,0,500), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(-200,0,0,0), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(180,0,0,0), frame(0,0,0,500,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new UziAnimation(
                    sequence(time, frame(0,0,NineMillimeterGunItem.aiming(stack) ? -0.5D : -0.75D,25,Curve.SIN_DOWN), frame(0,0,0,75,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new UziAnimation(ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,250), frame(-25,0,0,250,Curve.SIN_FULL), frame(-25,0,0,500), frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,500), frame(0,0,-2,150,Curve.SIN_FULL), frame(0,0,0,50,Curve.SIN_UP)), ZERO, ZERO, ZERO, ZERO);
            case RELOAD -> new UziAnimation(ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(-25,0,0,250,Curve.SIN_FULL), frame(-25,0,0,2000), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,2000), frame(0,0,-2,150,Curve.SIN_FULL), frame(0,0,0,50,Curve.SIN_UP)),
                    sequence(time, frame(0,0,0,250), frame(0,-10,0,250,Curve.SIN_UP), frame(0,-10,0,750), frame(0,0,0,500,Curve.SIN_DOWN)),
                    sequence(time, frame(empty ? 0 : 1,0,0,0), frame(empty ? 0 : 1,0,0,500), frame(1,0,0,0)), ZERO, ZERO);
            case JAMMED -> new UziAnimation(ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,500), frame(-25,0,0,250,Curve.SIN_FULL), frame(-25,0,0,1250), frame(0,0,0,500,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,1000), frame(0,0,-2,150,Curve.SIN_FULL), frame(0,0,0,50,Curve.SIN_UP), frame(0,0,0,500), frame(0,0,-2,150,Curve.SIN_FULL), frame(0,0,0,50,Curve.SIN_UP)), ZERO, ZERO, ZERO, ZERO);
            case INSPECT -> new UziAnimation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,-1,0,100), frame(0,0,0,100,Curve.SIN_UP), frame(0,12,0,350,Curve.SIN_DOWN), frame(0,0,0,350,Curve.SIN_UP), frame(0,-1,0,50,Curve.SIN_DOWN), frame(0,0,0,100,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,250), frame(-360,0,0,600)));
            default -> UziAnimation.NONE;
        };
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (NineMillimeterGunItem.animationTimer(stack) + partial) * 50.0D;
    }

    private static void pivotRotateX(PoseStack poses, double x, double y, double z, double degrees) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poses.translate(-x, -y, -z);
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

    private static double lerp(double start, double end, float progress) {
        return start + (end - start) * progress;
    }

    private static void renderModel(ModelResourceLocation location, PoseStack poses, MultiBufferSource buffers,
                                    int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(location);
        ModelBlockRenderer renderer = minecraft.getBlockRenderer().getModelRenderer();
        VertexConsumer consumer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        renderer.renderModel(poses.last(), consumer, Blocks.IRON_BLOCK.defaultBlockState(), model,
                1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
    }

    private static void renderMuzzleFlash(PoseStack poses, MultiBufferSource buffers,
                                          float progress, boolean grease) {
        poses.pushPose();
        poses.translate(0.0D, grease ? 0.0D : 0.75D, grease ? 8.0D : 8.5D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (grease) poses.scale(0.5F, 0.5F, 0.5F);
        SednaMuzzleFlash.render(poses, buffers, progress, 7.5F);
        poses.popPose();
    }

    private static void flashQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                  float width, float length, boolean vertical) {
        flashVertex(consumer, pose, vertical ? 0 : -width, vertical ? -width : 0, 0, 0, 1);
        flashVertex(consumer, pose, vertical ? 0 : width, vertical ? width : 0, 0, 1, 1);
        flashVertex(consumer, pose, vertical ? 0 : width, vertical ? width : 0, length, 1, 0);
        flashVertex(consumer, pose, vertical ? 0 : -width, vertical ? -width : 0, length, 0, 0);
    }

    private static void flashVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "item/" + path));
    }

    private static final Vec ZERO = new Vec(0, 0, 0);
    private record Vec(double x, double y, double z) {
        Vec lerp(Vec other, double progress) {
            return new Vec(x + (other.x - x) * progress, y + (other.y - y) * progress,
                    z + (other.z - z) * progress);
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
    private record GreaseAnimation(Vec recoil, Vec equip, Vec stock, Vec flap, Vec lift,
                                   Vec turn, Vec handle, Vec mag, Vec bullet) {
        private static final GreaseAnimation NONE = new GreaseAnimation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
    private record UziAnimation(Vec recoil, Vec equip, Vec stockBack, Vec stockFront,
                                Vec lift, Vec slide, Vec mag, Vec bullet, Vec yeet, Vec speen) {
        private static final UziAnimation NONE = new UziAnimation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
