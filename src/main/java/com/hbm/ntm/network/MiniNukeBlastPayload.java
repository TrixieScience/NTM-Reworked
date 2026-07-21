package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MiniNukeBlastPayload(double x, double y, double z, boolean tiny, boolean balefire)
        implements CustomPacketPayload {
    public static final Type<MiniNukeBlastPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "mini_nuke_blast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiniNukeBlastPayload> STREAM_CODEC =
            StreamCodec.ofMember(MiniNukeBlastPayload::encode, MiniNukeBlastPayload::decode);

    @Override public Type<MiniNukeBlastPayload> type() { return TYPE; }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeBoolean(tiny);
        buffer.writeBoolean(balefire);
    }

    private static MiniNukeBlastPayload decode(RegistryFriendlyByteBuf buffer) {
        return new MiniNukeBlastPayload(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(MiniNukeBlastPayload payload, IPayloadContext context) {
        Player viewer = context.player();
        var level = viewer.level();
        int stem = payload.tiny ? 17 : 19;
        int ground = payload.tiny ? 50 : 100;
        int crown = payload.tiny ? 15 : 75;
        level.addParticle(ParticleTypes.FLASH, payload.x, payload.y + 0.5D, payload.z,
                0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.EXPLOSION_EMITTER, payload.x, payload.y + 0.5D, payload.z,
                1.0D, 0.0D, 0.0D);
        for (int i = 0; i < stem; i++) {
            double rise = i * 0.1D + level.random.nextGaussian() * 0.02D;
            particle(level, payload, payload.x, payload.y, payload.z,
                    level.random.nextGaussian() * 0.05D, rise,
                    level.random.nextGaussian() * 0.05D);
        }
        for (int i = 0; i < ground; i++) {
            particle(level, payload, payload.x, payload.y + 0.5D, payload.z,
                    level.random.nextGaussian() * 0.5D,
                    level.random.nextInt(5) == 0 ? 0.02D : 0.0D,
                    level.random.nextGaussian() * 0.5D);
        }
        for (int i = 0; i < crown; i++) {
            double mx = level.random.nextGaussian() * (payload.tiny ? 0.2D : 0.5D);
            double mz = level.random.nextGaussian() * (payload.tiny ? 0.2D : 0.5D);
            double limit = payload.tiny ? 0.75D : 1.5D;
            if (mx * mx + mz * mz > limit) {
                mx *= 0.5D;
                mz *= 0.5D;
            }
            double top = payload.tiny ? 1.6D : 1.8D;
            double variance = payload.tiny ? 2.0D : 3.0D;
            double my = top + (level.random.nextDouble() * variance - variance * 0.5D)
                    * (0.75D - (mx * mx + mz * mz)) * 0.5D;
            particle(level, payload, payload.x, payload.y, payload.z, mx,
                    my + level.random.nextGaussian() * 0.02D, mz);
        }
    }

    private static void particle(net.minecraft.world.level.Level level, MiniNukeBlastPayload payload,
                                 double x, double y, double z, double mx, double my, double mz) {
        level.addParticle(payload.balefire ? ParticleTypes.WITCH : ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y, z, mx, my, mz);
    }
}
