package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.item.DaniItem;
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

public final class DaniItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation BARREL_CELESTIAL = model("dani_barrel_celestial");
    public static final ModelResourceLocation LATCH_CELESTIAL = model("dani_latch_celestial");
    public static final ModelResourceLocation HAMMER_CELESTIAL = model("dani_hammer_celestial");
    public static final ModelResourceLocation DRUM_CELESTIAL = model("dani_drum_celestial");
    public static final ModelResourceLocation GRIP_CELESTIAL = model("dani_grip_celestial");
    public static final ModelResourceLocation BARREL_LUNAR = model("dani_barrel_lunar");
    public static final ModelResourceLocation LATCH_LUNAR = model("dani_latch_lunar");
    public static final ModelResourceLocation HAMMER_LUNAR = model("dani_hammer_lunar");
    public static final ModelResourceLocation DRUM_LUNAR = model("dani_drum_lunar");
    public static final ModelResourceLocation GRIP_LUNAR = model("dani_grip_lunar");

    public DaniItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof DaniItem)) return;
        poses.pushPose();
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND ->
                    renderFirstPersonPair(stack, poses, buffers, packedLight, packedOverlay);
            case THIRD_PERSON_LEFT_HAND -> renderThirdPerson(stack, 0, context, poses, buffers, packedLight, packedOverlay);
            case THIRD_PERSON_RIGHT_HAND -> renderThirdPerson(stack, 1, context, poses, buffers, packedLight, packedOverlay);
            case GUI -> renderInventory(poses, buffers, packedLight, packedOverlay);
            default -> renderDropped(poses, buffers, packedLight, packedOverlay);
        }
        poses.popPose();
    }

    private static void renderFirstPersonPair(ItemStack stack, PoseStack poses,
                                               MultiBufferSource buffers, int light, int overlay) {
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        for (int index = 0; index < DaniItem.RECEIVER_COUNT; index++) {
            int side = index == 0 ? -1 : 1;
            poses.pushPose();
            poses.translate(-1.5D * 0.8D * side, -0.6D, 0.8D + 0.875D);
            poses.scale(0.125F, 0.125F, 0.125F);
            renderFirstPerson(stack, index, poses, buffers, light, overlay);
            renderHeldFlash(stack, index, poses, buffers);
            poses.popPose();
        }
    }

    private static void renderThirdPerson(ItemStack stack, int index, ItemDisplayContext context,
                                          PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        setupThirdPerson(context, poses);
        renderStatic(index, poses, buffers, light, overlay);
        renderHeldFlash(stack, index, poses, buffers);
    }

    private static void renderInventory(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        poses.scale(1.0F, -1.0F, -1.0F);
        poses.scale(1.125F / 16.0F, 1.125F / 16.0F, 1.125F / 16.0F);
        for (int index = 0; index < 2; index++) {
            poses.pushPose();
            if (index == 1) poses.translate(0.0D, 0.0D, 5.0D);
            poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
            poses.mulPose((index == 0 ? Axis.YP : Axis.YN).rotationDegrees(90.0F));
            if (index == 1) poses.mulPose(Axis.XN.rotationDegrees(90.0F));
            poses.mulPose(Axis.XP.rotationDegrees(25.0F));
            poses.mulPose((index == 0 ? Axis.YP : Axis.YN).rotationDegrees(45.0F));
            poses.translate(index == 0 ? 2.0D : -2.0D, 0.0D, 0.0D);
            renderStatic(index, poses, buffers, light, overlay);
            poses.popPose();
        }
    }

    private static void renderDropped(PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        poses.scale(0.075F, 0.075F, 0.075F);
        poses.mulPose(Axis.YN.rotationDegrees(90.0F));
        for (int index = 0; index < 2; index++) {
            poses.pushPose();
            poses.translate(index == 0 ? -2.0D : 2.0D, 1.0D, 0.0D);
            renderStatic(index, poses, buffers, light, overlay);
            poses.popPose();
        }
    }

    private static void renderFirstPerson(ItemStack stack, int index, PoseStack poses,
                                          MultiBufferSource buffers, int light, int overlay) {
        Animation animation = animation(stack, index);
        poses.translate(animation.recoil.x, animation.recoil.y, animation.recoil.z);
        poses.mulPose(Axis.XP.rotationDegrees((float) (animation.recoil.z * 10.0D)));

        int direction = index == 0 ? -1 : 1;
        poses.translate(0.0D, -2.0D, -2.0D);
        poses.mulPose(Axis.XN.rotationDegrees((float) animation.equip.x));
        poses.translate(0.0D, 2.0D, 2.0D);

        poses.pushPose();
        poses.translate(0.0D, 1.5D, 9.25D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (-animation.recoil.z * 10.0D)));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        WeaponSmokeRenderer.render(stack, index, poses, buffers, 0.5D,
                WeaponSmokeRenderer.STANDARD,
                DaniItem.state(stack, index) == DaniItem.GunState.RELOADING);
        poses.popPose();

        poses.translate(animation.reloadMove.x, animation.reloadMove.y, animation.reloadMove.z);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.reloadRot.x));
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.reloadRot.z * direction));
        poses.mulPose(Axis.YP.rotationDegrees((float) animation.reloadRot.y * direction));
        renderModel(part(index, GRIP_CELESTIAL, GRIP_LUNAR), poses, buffers, light, overlay);

        poses.pushPose();
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.front.z));
        renderModel(part(index, BARREL_CELESTIAL, BARREL_LUNAR), poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 2.3125D, -0.875D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.latch.z));
        poses.translate(0.0D, -2.3125D, 0.875D);
        renderModel(part(index, LATCH_CELESTIAL, LATCH_LUNAR), poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 1.0D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) (animation.drum.z * 60.0D)));
        poses.translate(0.0D, -1.0D, animation.drumPush.z);
        renderModel(part(index, DRUM_CELESTIAL, DRUM_LUNAR), poses, buffers, light, overlay);
        poses.popPose();
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, -4.5D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (-45.0D + 45.0D * animation.hammer.z)));
        poses.translate(0.0D, 0.0D, 4.5D);
        renderModel(part(index, HAMMER_CELESTIAL, HAMMER_LUNAR), poses, buffers, light, overlay);
        poses.popPose();
    }

    private static void renderStatic(int index, PoseStack poses, MultiBufferSource buffers,
                                     int light, int overlay) {
        renderModel(part(index, BARREL_CELESTIAL, BARREL_LUNAR), poses, buffers, light, overlay);
        renderModel(part(index, LATCH_CELESTIAL, LATCH_LUNAR), poses, buffers, light, overlay);
        renderModel(part(index, HAMMER_CELESTIAL, HAMMER_LUNAR), poses, buffers, light, overlay);
        renderModel(part(index, DRUM_CELESTIAL, DRUM_LUNAR), poses, buffers, light, overlay);
        renderModel(part(index, GRIP_CELESTIAL, GRIP_LUNAR), poses, buffers, light, overlay);
    }

    private static ModelResourceLocation part(int index, ModelResourceLocation celestial,
                                              ModelResourceLocation lunar) {
        return index == 0 ? celestial : lunar;
    }

    private static void setupContext(ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                // GUI adds a Y reflection that the revolver camera never asked for. Cancel it.
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                poses.scale(1.125F / 16.0F, 1.125F / 16.0F, 1.125F / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                poses.translate(-0.5D, 1.5D, 0.0D);
            }
            case GROUND -> {
                poses.scale(0.125F, 0.125F, 0.125F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case FIXED -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                poses.translate(0.0D, 0.0D, 0.875D);
                poses.translate(lerp(side * 0.8D, 0.0D, aim), lerp(-0.6D, -0.390625D, aim),
                        lerp(0.8D, 0.25D, aim));
                poses.scale(0.125F, 0.125F, 0.125F);
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
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.translate(0.0D, 1.0D, 3.0D);
    }

    private static Animation animation(ItemStack stack, int index) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (DaniItem.animationTimer(stack, index) + partial) * 50.0D;
        return switch (DaniItem.animation(stack, index)) {
            case EQUIP -> new Animation(ZERO, sequence(time,
                    frame(1080, 0, 0, 0), frame(0, 0, 0, 1000, Curve.SIN_DOWN)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Animation(
                    sequence(time, frame(0, 0, 0, 50), frame(0, 0, -3, 50), frame(0, 0, 0, 250)),
                    ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 250), frame(0, 0, 1, 200)), ZERO,
                    sequence(time, frame(0, 0, 1, 50), frame(0, 0, 1, 300), frame(0, 0, 0, 200)));
            case CYCLE_DRY -> new Animation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0, 0, 0, 250), frame(0, 0, 1, 200)), ZERO,
                    sequence(time, frame(0, 0, 1, 50), frame(0, 0, 1, 200), frame(0, 0, 0, 200)));
            case RELOAD -> reload(time);
            case INSPECT -> inspect(time);
            case JAMMED -> jammed(time);
            default -> Animation.NONE;
        };
    }

    private static void renderHeldFlash(ItemStack stack, int index, PoseStack poses,
                                        MultiBufferSource buffers) {
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack, index);
        if (elapsed >= 0L && elapsed < 75L) renderMuzzleFlash(poses, buffers, elapsed / 75.0F);
    }

    private static Animation reload(double time) {
        return new Animation(ZERO, ZERO,
                sequence(time, frame(0,0,0,300), frame(0,-15,0,1000), frame(0,0,0,450)),
                sequence(time, frame(0,0,0,300), frame(60,0,0,500), frame(60,0,0,500),
                        frame(0,-90,-90,0), frame(0,-90,-90,600), frame(0,0,0,300),
                        frame(0,0,0,100), frame(-45,0,0,50), frame(-45,0,0,100), frame(0,0,0,300)),
                sequence(time, frame(0,0,0,200), frame(0,0,45,150), frame(0,0,45,2000), frame(0,0,0,75)),
                sequence(time, frame(0,0,90,300), frame(0,0,90,2000), frame(0,0,0,150)),
                ZERO,
                sequence(time, frame(0,0,0,1600), frame(0,0,-5,0), frame(0,0,0,300)), ZERO);
    }

    private static Animation inspect(double time) {
        return new Animation(ZERO, ZERO,
                sequence(time, frame(0,0,0,300), frame(0,-2.5,0,500,Curve.SIN_FULL),
                        frame(0,-2.5,0,500), frame(0,0,0,350)),
                sequence(time, frame(0,0,0,300), frame(45,0,0,500,Curve.SIN_FULL),
                        frame(45,0,0,500), frame(-45,0,0,50), frame(-45,0,0,100), frame(0,0,0,300)),
                sequence(time, frame(0,0,0,200), frame(0,0,45,150), frame(0,0,45,1000), frame(0,0,0,75)),
                sequence(time, frame(0,0,90,300), frame(0,0,90,1000), frame(0,0,0,150)),
                ZERO, ZERO, ZERO);
    }

    private static Animation jammed(double time) {
        return new Animation(ZERO, ZERO,
                sequence(time, frame(0,0,0,500), frame(0,0,0,300),
                        frame(0,-2.5,0,500,Curve.SIN_FULL), frame(0,-2.5,0,500), frame(0,0,0,350)),
                sequence(time, frame(0,0,0,500), frame(0,0,0,300),
                        frame(45,0,0,500,Curve.SIN_FULL), frame(45,0,0,500),
                        frame(-45,0,0,50), frame(-45,0,0,100), frame(0,0,0,300)),
                sequence(time, frame(0,0,0,500), frame(0,0,0,200), frame(0,0,45,150),
                        frame(0,0,45,1000), frame(0,0,0,75)),
                sequence(time, frame(0,0,0,500), frame(0,0,90,300), frame(0,0,90,1000), frame(0,0,0,150)),
                ZERO, ZERO, ZERO);
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
                progress = frame.curve.apply(progress);
                return previous.lerp(frame.value, progress);
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

    private static void renderMuzzleFlash(PoseStack poses, MultiBufferSource buffers, float progress) {
        poses.pushPose();
        poses.translate(0.0D, 1.5D, 9.25D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
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
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }
    private record Animation(Vec recoil, Vec equip, Vec reloadMove, Vec reloadRot,
                             Vec front, Vec latch, Vec drum, Vec drumPush, Vec hammer) {
        private static final Animation NONE = new Animation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
