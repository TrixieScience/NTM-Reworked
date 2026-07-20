package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.inventory.CombustionEngineMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CombustionEngineControlPayload(CombustionEngineBlockEntity.Control control, int value)
        implements CustomPacketPayload {
    public static final Type<CombustionEngineControlPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "combustion_engine_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CombustionEngineControlPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {
                buffer.writeEnum(payload.control);
                buffer.writeVarInt(payload.value);
            }, buffer -> new CombustionEngineControlPayload(
                    buffer.readEnum(CombustionEngineBlockEntity.Control.class), buffer.readVarInt()));

    @Override public Type<CombustionEngineControlPayload> type() { return TYPE; }

    public static void handle(CombustionEngineControlPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof CombustionEngineMenu menu
                    && menu.blockEntity() != null) menu.blockEntity().setControl(payload.control, payload.value);
        });
    }
}
