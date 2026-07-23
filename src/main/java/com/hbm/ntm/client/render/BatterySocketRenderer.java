package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.BatterySocketBlock;
import com.hbm.ntm.blockentity.BatterySocketBlockEntity;
import com.hbm.ntm.item.BatteryPackItem;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public final class BatterySocketRenderer implements BlockEntityRenderer<BatterySocketBlockEntity> {
    public static final ModelResourceLocation SOCKET = model("battery_socket_body");
    public static final ModelResourceLocation SUPPORTS = model("battery_socket_supports");
    public static final ModelResourceLocation HORSE = model("battery_socket_horse");
    private static final RenderType BEAM = RenderType.create(
            "hbm_battery_socket_beam", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 2048, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));
    public static final Map<BatteryPackItem.BatteryType, ModelResourceLocation> BATTERY_MODELS = createBatteryModels();

    public BatterySocketRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BatterySocketBlockEntity socket, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poses.pushPose();
        poses.translate(0.5D, 0.0D, 0.5D);
        poses.mulPose(Axis.YP.rotationDegrees(270.0F
                - socket.getBlockState().getValue(BatterySocketBlock.FACING).toYRot()));
        poses.translate(-0.5D, 0.0D, 0.5D);
        renderModel(SOCKET, poses, buffers, packedLight, packedOverlay);
        if (socket.getLevel() != null && !socket.getLevel().getBlockState(socket.getBlockPos().above(2)).isAir()) {
            renderModel(SUPPORTS, poses, buffers, packedLight, packedOverlay);
        }
        ItemStack stack = socket.getItem(0);
        if (stack.getItem() instanceof BatteryPackItem) {
            renderModel(BATTERY_MODELS.get(BatteryPackItem.type(stack)), poses, buffers, packedLight, packedOverlay);
        } else if (stack.is(ModItems.BATTERY_CREATIVE.get())) {
            renderCreative(socket, partialTick, poses, buffers, packedLight, packedOverlay);
        }
        poses.popPose();
    }

    public static void renderBattery(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                     int packedLight, int packedOverlay) {
        renderModel(BATTERY_MODELS.get(BatteryPackItem.type(stack)), poses, buffers, packedLight, packedOverlay);
    }

    public static void renderSocket(PoseStack poses, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        renderModel(SOCKET, poses, buffers, packedLight, packedOverlay);
    }

    private static void renderCreative(BatterySocketBlockEntity socket, float partialTick, PoseStack poses,
                                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (socket.getLevel() == null) return;
        long gameTime = socket.getLevel().getGameTime();
        poses.pushPose();
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YN.rotationDegrees(((gameTime % 360L) + partialTick) * 25.0F));
        renderModel(HORSE, poses, buffers, packedLight, packedOverlay);
        poses.popPose();

        Random visible = new Random(gameTime / 5L);
        visible.nextBoolean();
        int waveSeed = (int) (System.currentTimeMillis() % 1_000L) / 50;
        VertexConsumer consumer = buffers.getBuffer(BEAM);
        poses.pushPose();
        poses.translate(0.0D, 0.75D, 0.0D);
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                if (visible.nextInt(4) != 0) continue;
                Vector3f target = new Vector3f(0.4375F * x, 1.1875F, 0.4375F * z);
                renderBeam(consumer, poses.last(), target, waveSeed, 15, 0.0625F, 3, 0.025F);
                renderBeam(consumer, poses.last(), target, waveSeed, 1, 0.0F, 3, 0.025F);
            }
        }
        poses.popPose();
    }

    private static void renderBeam(VertexConsumer consumer, PoseStack.Pose pose, Vector3f target,
                                   int seed, int segments, float jitter, int layers, float thickness) {
        Random random = new Random(seed);
        Vector3f[] points = new Vector3f[segments + 1];
        points[0] = new Vector3f();
        Vector3f direction = new Vector3f(target).normalize();
        Vector3f reference = Math.abs(direction.y()) > 0.9F
                ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f right = direction.cross(reference, new Vector3f()).normalize();
        Vector3f up = right.cross(direction, new Vector3f()).normalize();
        for (int index = 1; index < segments; index++) {
            float progress = index / (float) segments;
            points[index] = new Vector3f(target).mul(progress)
                    .fma((random.nextFloat() * 2.0F - 1.0F) * jitter, right)
                    .fma((random.nextFloat() * 2.0F - 1.0F) * jitter, up);
        }
        points[segments] = new Vector3f(target);
        for (int index = 0; index < segments; index++) {
            beamSegment(consumer, pose, points[index], points[index + 1], layers, thickness);
        }
    }

    private static void beamSegment(VertexConsumer consumer, PoseStack.Pose pose,
                                    Vector3f start, Vector3f end, int layers, float thickness) {
        Vector3f direction = new Vector3f(end).sub(start).normalize();
        Vector3f reference = Math.abs(direction.y()) > 0.9F
                ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f right = direction.cross(reference, new Vector3f()).normalize();
        Vector3f up = right.cross(direction, new Vector3f()).normalize();
        for (int layer = 0; layer < layers; layer++) {
            float blend = layers == 1 ? 1.0F : layer / (float) (layers - 1);
            float radius = thickness * (1.0F - blend * 0.65F);
            int red = (int) (64.0F * (1.0F - blend));
            int green = (int) (64.0F * (1.0F - blend) + 32.0F * blend);
            int blue = (int) (64.0F * (1.0F - blend) + 64.0F * blend);
            Vector3f r = new Vector3f(right).mul(radius);
            Vector3f u = new Vector3f(up).mul(radius);
            Vector3f a = new Vector3f(start).sub(r).sub(u);
            Vector3f b = new Vector3f(start).add(r).sub(u);
            Vector3f c = new Vector3f(start).add(r).add(u);
            Vector3f d = new Vector3f(start).sub(r).add(u);
            Vector3f e = new Vector3f(end).sub(r).sub(u);
            Vector3f f = new Vector3f(end).add(r).sub(u);
            Vector3f g = new Vector3f(end).add(r).add(u);
            Vector3f h = new Vector3f(end).sub(r).add(u);
            quad(consumer, pose, a, b, f, e, red, green, blue);
            quad(consumer, pose, b, c, g, f, red, green, blue);
            quad(consumer, pose, c, d, h, g, red, green, blue);
            quad(consumer, pose, d, a, e, h, red, green, blue);
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, Vector3f a, Vector3f b,
                             Vector3f c, Vector3f d, int red, int green, int blue) {
        vertex(consumer, pose, a, red, green, blue);
        vertex(consumer, pose, b, red, green, blue);
        vertex(consumer, pose, c, red, green, blue);
        vertex(consumer, pose, d, red, green, blue);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f point,
                               int red, int green, int blue) {
        consumer.addVertex(pose, point.x(), point.y(), point.z()).setColor(red, green, blue, 255);
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

    private static Map<BatteryPackItem.BatteryType, ModelResourceLocation> createBatteryModels() {
        Map<BatteryPackItem.BatteryType, ModelResourceLocation> models =
                new EnumMap<>(BatteryPackItem.BatteryType.class);
        for (BatteryPackItem.BatteryType type : BatteryPackItem.BatteryType.values()) {
            models.put(type, model("battery_pack_" + type.id()));
        }
        return models;
    }

    private static ModelResourceLocation model(String path) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/" + path));
    }

    @Override
    public AABB getRenderBoundingBox(BatterySocketBlockEntity socket) {
        return socket.renderBounds();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
