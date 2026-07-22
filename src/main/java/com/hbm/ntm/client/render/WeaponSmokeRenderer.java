package com.hbm.ntm.client.render;

import com.hbm.ntm.client.ClientWeaponEvents;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class WeaponSmokeRenderer {
    public static final Profile STANDARD = new Profile(2_000L, 0.025D, 1.15D);
    public static final Profile NINE_MM = new Profile(2_000L, 0.05D, 1.1D);
    public static final Profile TWENTY_TWO = new Profile(3_000L, 0.05D, 1.1D);
    public static final Profile SEVEN_SIX_TWO = new Profile(1_500L, 0.075D, 1.1D);
    public static final Profile FORTY_MM = new Profile(1_500L, 0.025D, 1.05D);
    public static final Profile FIFTY = new Profile(2_000L, 0.05D, 1.1D);

    private static final RenderType SMOKE = RenderType.create(
            "hbm_weapon_smoke", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));
    private static final Map<ItemStack, Track[]> TRACKS = new WeakHashMap<>();

    private WeaponSmokeRenderer() { }

    public static void render(ItemStack stack, int receiver, PoseStack poses,
                              MultiBufferSource buffers, double widthScale,
                              Profile profile, boolean reloading) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        Track track = track(stack, receiver);
        long shot = ClientWeaponEvents.lastShot(stack, receiver);
        long tick = minecraft.level.getGameTime();
        if (track.updatedTick != tick || track.shot != shot) {
            update(track, player, shot, profile, reloading);
            track.updatedTick = tick;
            track.shot = shot;
        }
        if (track.nodes.size() < 2) return;

        VertexConsumer consumer = buffers.getBuffer(SMOKE);
        PoseStack.Pose pose = poses.last();
        for (int i = 0; i < track.nodes.size() - 1; i++) {
            Node node = track.nodes.get(i);
            Node past = track.nodes.get(i + 1);
            ribbon(consumer, pose, node, past, widthScale);
        }
    }

    private static void update(Track track, LocalPlayer player, long shot,
                               Profile profile, boolean reloading) {
        long now = System.currentTimeMillis();
        boolean smoking = shot >= 0L && shot + profile.duration > now;
        if (!smoking) {
            track.nodes.clear();
            return;
        }

        Vec3 movement = player.getDeltaMovement().scale(-1.0D)
                .yRot(player.getYRot() * ((float) Math.PI / 180.0F));
        double side = (player.getYRot() - player.yHeadRotO) * 0.1D;
        for (Node node : track.nodes) {
            node.forward += -movement.z * 15.0D + player.getRandom().nextGaussian() * 0.025D;
            node.lift += movement.y + 1.5D;
            node.side += movement.x * 15.0D + player.getRandom().nextGaussian() * 0.025D + side;
            if (node.alpha > 0.0D) node.alpha -= profile.alphaDecay;
            node.width *= profile.widthGrowth;
        }

        double alpha = (1.0D - (now - shot) / (double) profile.duration) * 0.5D;
        if (reloading || track.nodes.isEmpty()) alpha = 0.0D;
        track.nodes.add(new Node(alpha));
    }

    private static void ribbon(VertexConsumer consumer, PoseStack.Pose pose,
                               Node node, Node past, double scale) {
        vertex(consumer, pose, node.forward, node.lift, node.side, node.alpha);
        vertex(consumer, pose, node.forward, node.lift, node.side + node.width * scale, 0.0D);
        vertex(consumer, pose, past.forward, past.lift, past.side + past.width * scale, 0.0D);
        vertex(consumer, pose, past.forward, past.lift, past.side, past.alpha);

        vertex(consumer, pose, node.forward, node.lift, node.side, node.alpha);
        vertex(consumer, pose, node.forward, node.lift, node.side - node.width * scale, 0.0D);
        vertex(consumer, pose, past.forward, past.lift, past.side - past.width * scale, 0.0D);
        vertex(consumer, pose, past.forward, past.lift, past.side, past.alpha);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               double x, double y, double z, double alpha) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(1.0F, 1.0F, 1.0F, (float) alpha);
    }

    private static Track track(ItemStack stack, int receiver) {
        int size = Math.max(2, receiver + 1);
        Track[] tracks = TRACKS.computeIfAbsent(stack, ignored -> new Track[size]);
        if (receiver >= tracks.length) {
            Track[] grown = new Track[size];
            System.arraycopy(tracks, 0, grown, 0, tracks.length);
            tracks = grown;
            TRACKS.put(stack, tracks);
        }
        if (tracks[receiver] == null) tracks[receiver] = new Track();
        return tracks[receiver];
    }

    public record Profile(long duration, double alphaDecay, double widthGrowth) { }

    private static final class Track {
        private final List<Node> nodes = new ArrayList<>();
        private long updatedTick = Long.MIN_VALUE;
        private long shot = Long.MIN_VALUE;
    }

    private static final class Node {
        private double forward;
        private double side;
        private double lift;
        private double alpha;
        private double width = 1.0D;

        private Node(double alpha) {
            this.alpha = alpha;
        }
    }
}
