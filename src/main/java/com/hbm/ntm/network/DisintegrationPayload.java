package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.BiConsumer;

public record DisintegrationPayload(int entityId, boolean cremate) implements CustomPacketPayload {
    private static BiConsumer<Integer, Boolean> clientHandler = (entityId, cremate) -> { };
    public static final Type<DisintegrationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "disintegration"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DisintegrationPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, DisintegrationPayload::entityId,
                    ByteBufCodecs.BOOL, DisintegrationPayload::cremate,
                    DisintegrationPayload::new);

    @Override
    public Type<DisintegrationPayload> type() {
        return TYPE;
    }

    public static void handle(DisintegrationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> clientHandler.accept(payload.entityId, payload.cremate));
    }

    public static void installClientHandler(BiConsumer<Integer, Boolean> handler) {
        clientHandler = handler;
    }
}
