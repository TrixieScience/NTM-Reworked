package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.network.DisintegrationPayload;
import com.hbm.ntm.registry.ModParticles;
import com.hbm.ntm.registry.ModSounds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Client half of the source AshesCreator + SkeletonCreator death presentation. */
public final class ClientDisintegrationEffects {
    private static final ResourceLocation MODEL = id("models/effect/skeleton.obj");
    private static final ResourceLocation SKELETON = id("textures/particle/skeleton.png");
    private static final ResourceLocation VILLAGER = id("textures/particle/skoilet.png");
    private static final RenderType SKELETON_TYPE = RenderType.entityTranslucent(SKELETON, false);
    private static final RenderType VILLAGER_TYPE = RenderType.entityTranslucent(VILLAGER, false);
    private static final Set<String> GROUPS = Set.of("Skull", "Torso", "Limb", "SkullVillager");
    private static final List<Bone> BONES = new ArrayList<>();
    private static EnvsuitMesh mesh;

    private ClientDisintegrationEffects() { }

    public static void register() {
        DisintegrationPayload.installClientHandler(ClientDisintegrationEffects::spawn);
        NeoForge.EVENT_BUS.addListener(ClientDisintegrationEffects::tick);
        NeoForge.EVENT_BUS.addListener(ClientDisintegrationEffects::render);
    }

    private static void spawn(int entityId, boolean cremate) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !(level.getEntity(entityId) instanceof LivingEntity victim)) return;

        victim.setInvisible(true);
        float brightness = cremate ? 0.25F : 1.0F;
        int amount = Mth.clamp((int) (victim.getBbWidth() * victim.getBbHeight()
                * victim.getBbWidth() * 25.0F), 5, 50);
        for (int index = 0; index < amount; index++) {
            double x = victim.getX() + (victim.getBbWidth() + 0.25D)
                    * (level.random.nextDouble() - 0.5D);
            double y = victim.getY() + victim.getBbHeight() * level.random.nextDouble();
            double z = victim.getZ() + (victim.getBbWidth() + 0.25D)
                    * (level.random.nextDouble() - 0.5D);
            level.addParticle(ModParticles.ASHES.get(), true, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, true, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        addSkeleton(victim, brightness);
        level.playLocalSound(victim.getX(), victim.getY(), victim.getZ(),
                ModSounds.DISINTEGRATION.get(), SoundSource.PLAYERS, 2.0F,
                0.9F + level.random.nextFloat() * 0.2F, false);
    }

    private static void addSkeleton(LivingEntity victim, float brightness) {
        Anatomy anatomy = anatomy(victim);
        if (anatomy == Anatomy.NONE) return;
        float bodyYaw = victim.yBodyRot;
        float headYaw = victim.getYHeadRot();
        float pitch = victim.getXRot();
        Vec3 arms = new Vec3(0.375D, 0.0D, 0.0D).yRot(-bodyYaw * Mth.DEG_TO_RAD);
        Vec3 legs = new Vec3(0.125D, 0.0D, 0.0D).yRot(-bodyYaw * Mth.DEG_TO_RAD);
        Vec3 forward = new Vec3(0.0D, 0.0D, 0.25D).yRot(-bodyYaw * Mth.DEG_TO_RAD);
        double x = victim.getX();
        double y = victim.getY();
        double z = victim.getZ();

        if (anatomy == Anatomy.VILLAGER) {
            add(BonePart.SKULL_VILLAGER, x, y + 1.6875D, z, -headYaw, pitch, brightness);
            add(BonePart.TORSO, x, y + 1.0D, z, -bodyYaw, 0.0F, brightness);
            add(BonePart.LIMB, x + arms.x + forward.x, y + 1.125D, z + arms.z + forward.z,
                    -bodyYaw, -45.0F, brightness);
            add(BonePart.LIMB, x - arms.x + forward.x, y + 1.125D, z - arms.z + forward.z,
                    -bodyYaw, -45.0F, brightness);
        } else {
            add(BonePart.SKULL, x, y + 1.75D, z, -headYaw, pitch, brightness);
            add(BonePart.TORSO, x, y + 1.125D, z, -bodyYaw, 0.0F, brightness);
            float armPitch = anatomy == Anatomy.ZOMBIE ? -90.0F : 0.0F;
            Vec3 armForward = anatomy == Anatomy.ZOMBIE ? forward : Vec3.ZERO;
            add(BonePart.LIMB, x + arms.x + armForward.x, y + (anatomy == Anatomy.ZOMBIE ? 1.375D : 1.125D),
                    z + arms.z + armForward.z, -bodyYaw, armPitch, brightness);
            add(BonePart.LIMB, x - arms.x + armForward.x, y + (anatomy == Anatomy.ZOMBIE ? 1.375D : 1.125D),
                    z - arms.z + armForward.z, -bodyYaw, armPitch, brightness);
        }
        add(BonePart.LIMB, x + legs.x, y + 0.375D, z + legs.z, -bodyYaw, 0.0F, brightness);
        add(BonePart.LIMB, x - legs.x, y + 0.375D, z - legs.z, -bodyYaw, 0.0F, brightness);
    }

    private static Anatomy anatomy(LivingEntity victim) {
        if (victim instanceof AbstractVillager || victim instanceof Witch) return Anatomy.VILLAGER;
        if (victim instanceof Zombie || victim instanceof AbstractSkeleton) return Anatomy.ZOMBIE;
        if (victim instanceof Player) return Anatomy.BIPED;
        return Anatomy.NONE;
    }

    private static void add(BonePart part, double x, double y, double z,
                            float yaw, float pitch, float brightness) {
        BONES.add(new Bone(part, new Vec3(x, y, z), yaw, pitch, brightness));
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            BONES.clear();
            return;
        }
        Iterator<Bone> iterator = BONES.iterator();
        while (iterator.hasNext()) {
            Bone bone = iterator.next();
            if (!bone.tick(level)) iterator.remove();
        }
    }

    private static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BONES.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poses = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        renderBones(level, event, poses, buffers.getBuffer(SKELETON_TYPE), camera, partial, false);
        buffers.endBatch(SKELETON_TYPE);
        renderBones(level, event, poses, buffers.getBuffer(VILLAGER_TYPE), camera, partial, true);
        buffers.endBatch(VILLAGER_TYPE);
    }

    private static void renderBones(ClientLevel level, RenderLevelStageEvent event, PoseStack poses,
                                    VertexConsumer consumer, Vec3 camera, float partial,
                                    boolean villagers) {
        for (Bone bone : BONES) {
            if ((bone.part == BonePart.SKULL_VILLAGER) != villagers) continue;
            Vec3 position = bone.oldPosition.lerp(bone.position, partial);
            if (!event.getFrustum().isVisible(AABB.ofSize(position, 1.0D, 1.0D, 1.0D))) continue;
            float alpha = bone.age + partial > bone.lifetime - 40
                    ? Mth.clamp((bone.lifetime - bone.age - partial) / 40.0F, 0.0F, 1.0F) : 1.0F;
            int shade = Mth.clamp(Math.round(bone.brightness * 255.0F), 0, 255);
            int color = FastColor.ARGB32.color(Math.round(alpha * 255.0F), shade, shade, shade);
            int light = LevelRenderer.getLightColor(level, BlockPos.containing(position));

            poses.pushPose();
            poses.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);
            poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial, bone.oldYaw, bone.yaw)));
            poses.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partial, bone.oldPitch, bone.pitch)));
            poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            mesh().render(bone.part.group, poses.last(), consumer, 1.0F, light,
                    OverlayTexture.NO_OVERLAY, color);
            poses.popPose();
        }
    }

    private static EnvsuitMesh mesh() {
        if (mesh == null) {
            mesh = EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(), MODEL, GROUPS,
                    "disintegration skeleton");
        }
        return mesh;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private enum Anatomy { NONE, BIPED, ZOMBIE, VILLAGER }

    private enum BonePart {
        SKULL("Skull"), TORSO("Torso"), LIMB("Limb"), SKULL_VILLAGER("SkullVillager");
        private final String group;
        BonePart(String group) { this.group = group; }
    }

    private static final class Bone {
        private final BonePart part;
        private final float brightness;
        private final int lifetime;
        private final float yawMomentum;
        private final float pitchMomentum;
        private Vec3 oldPosition;
        private Vec3 position;
        private Vec3 velocity = Vec3.ZERO;
        private float yaw;
        private float pitch;
        private float oldYaw;
        private float oldPitch;
        private int delay = 20;
        private int age;
        private boolean onGround;

        private Bone(BonePart part, Vec3 position, float yaw, float pitch, float brightness) {
            this.part = part;
            this.position = position;
            this.oldPosition = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.oldYaw = yaw;
            this.oldPitch = pitch;
            this.brightness = brightness;
            this.lifetime = 1_200 + (int) (Math.random() * 20.0D);
            this.yawMomentum = (float) ((Math.random() * 5.0D) * (Math.random() < 0.5D ? -1.0D : 1.0D));
            this.pitchMomentum = (float) ((Math.random() * 5.0D) * (Math.random() < 0.5D ? -1.0D : 1.0D));
        }

        private boolean tick(ClientLevel level) {
            oldPosition = position;
            oldYaw = yaw;
            oldPitch = pitch;
            if (delay-- > 0) return true;
            if (delay == -1) {
                velocity = new Vec3(level.random.nextGaussian() * 0.025D, 0.0D,
                        level.random.nextGaussian() * 0.025D);
            }
            if (age++ >= lifetime) return false;
            if (onGround) return true;

            velocity = velocity.add(0.0D, -0.02D, 0.0D);
            Vec3 next = position.add(velocity);
            if (level.noCollision(AABB.ofSize(next, 0.3D, 0.3D, 0.3D))) {
                position = next;
                velocity = velocity.scale(0.98D);
                yaw += yawMomentum;
                pitch += pitchMomentum;
            } else {
                onGround = true;
                velocity = Vec3.ZERO;
                level.playLocalSound(position.x, position.y, position.z, SoundEvents.SKELETON_HURT,
                        SoundSource.AMBIENT, 0.25F, 0.8F + level.random.nextFloat() * 0.4F, false);
            }
            return true;
        }
    }
}
