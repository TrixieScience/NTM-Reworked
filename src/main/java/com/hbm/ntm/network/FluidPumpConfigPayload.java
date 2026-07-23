package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.FluidPumpMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidPumpConfigPayload(int throughput, int pressure, int priority)
        implements CustomPacketPayload {
    public static final Type<FluidPumpConfigPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "fluid_pump_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPumpConfigPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {
                buffer.writeVarInt(payload.throughput);
                buffer.writeByte(payload.pressure);
                buffer.writeByte(payload.priority);
            }, buffer -> new FluidPumpConfigPayload(buffer.readVarInt(), buffer.readByte(), buffer.readByte()));

    @Override public Type<FluidPumpConfigPayload> type() { return TYPE; }

    public static void handle(FluidPumpConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof FluidPumpMenu menu && menu.blockEntity() != null) {
                menu.blockEntity().configure(payload.throughput, payload.pressure, payload.priority);
            }
        });
    }
}
