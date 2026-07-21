package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.registry.ModParticles;
import com.hbm.ntm.item.FlamerGunItem;
import com.hbm.ntm.item.LaserPistolItem;
import com.hbm.ntm.item.RocketLauncherItem;
import com.hbm.ntm.item.StingerLauncherItem;
import com.hbm.ntm.item.CoilgunItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GunEffectPayload(int shooterId, int receiverIndex, double x, double y, double z,
                               double headingX, double headingY, double headingZ,
                               boolean blackPowder) implements CustomPacketPayload {
    public static final Type<GunEffectPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "gun_effect")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GunEffectPayload> STREAM_CODEC =
            StreamCodec.ofMember(GunEffectPayload::encode, GunEffectPayload::decode);

    public static GunEffectPayload fired(int shooterId, Vec3 origin, Vec3 heading) {
        return fired(shooterId, origin, heading, true);
    }

    public static GunEffectPayload fired(int shooterId, Vec3 origin, Vec3 heading, boolean blackPowder) {
        return fired(shooterId, origin, heading, blackPowder, 0);
    }

    public static GunEffectPayload fired(int shooterId, Vec3 origin, Vec3 heading,
                                         boolean blackPowder, int receiverIndex) {
        return new GunEffectPayload(shooterId, receiverIndex, origin.x, origin.y, origin.z,
                heading.x, heading.y, heading.z, blackPowder);
    }

    @Override
    public Type<GunEffectPayload> type() {
        return TYPE;
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(shooterId);
        buffer.writeVarInt(receiverIndex);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeDouble(headingX);
        buffer.writeDouble(headingY);
        buffer.writeDouble(headingZ);
        buffer.writeBoolean(blackPowder);
    }

    private static GunEffectPayload decode(RegistryFriendlyByteBuf buffer) {
        return new GunEffectPayload(buffer.readVarInt(), buffer.readVarInt(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean());
    }

    public static void handle(GunEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player viewer = context.player();
            Vec3 heading = new Vec3(payload.headingX, payload.headingY, payload.headingZ).normalize();
            boolean customMuzzle = viewer.level().getEntity(payload.shooterId)
                    instanceof net.minecraft.world.entity.LivingEntity living
                    && (living.getMainHandItem().getItem() instanceof FlamerGunItem
                    || living.getMainHandItem().getItem() instanceof LaserPistolItem
                    || living.getMainHandItem().getItem() instanceof RocketLauncherItem
                    || living.getMainHandItem().getItem() instanceof StingerLauncherItem
                    || living.getMainHandItem().getItem() instanceof CoilgunItem);
            int smokeCount = customMuzzle ? 0 : payload.blackPowder ? 10 : 3;
            for (int i = 0; i < smokeCount; i++) {
                double speed = (payload.blackPowder ? 0.5D : 0.2D) * (0.85D + viewer.getRandom().nextDouble() * 0.3D);
                viewer.level().addParticle(ModParticles.BLACK_POWDER_SMOKE.get(), payload.x, payload.y, payload.z,
                        heading.x * speed + viewer.getRandom().nextGaussian() * 0.05D,
                        heading.y * speed + viewer.getRandom().nextGaussian() * 0.05D,
                        heading.z * speed + viewer.getRandom().nextGaussian() * 0.05D);
            }
            if (payload.blackPowder) {
                for (int i = 0; i < 10; i++) {
                    double speed = 0.25D * (0.85D + viewer.getRandom().nextDouble() * 0.3D);
                    viewer.level().addParticle(ModParticles.BLACK_POWDER_SPARK.get(), payload.x, payload.y, payload.z,
                            heading.x * speed + viewer.getRandom().nextGaussian() * 0.02D,
                            heading.y * speed + viewer.getRandom().nextGaussian() * 0.02D,
                            heading.z * speed + viewer.getRandom().nextGaussian() * 0.02D);
                }
            }
            ClientWeaponEvents.onGunFired(payload.shooterId, payload.receiverIndex);
        });
    }
}
