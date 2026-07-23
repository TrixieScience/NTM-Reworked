package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.FluidBarrelMenu;
import com.hbm.ntm.inventory.FluidStorageTankMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidStorageTankModePayload() implements CustomPacketPayload {
    public static final Type<FluidStorageTankModePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "fluid_storage_tank_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStorageTankModePayload> STREAM_CODEC =
            StreamCodec.unit(new FluidStorageTankModePayload());

    @Override public Type<FluidStorageTankModePayload> type() { return TYPE; }

    public static void handle(FluidStorageTankModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof FluidStorageTankMenu menu
                    && menu.blockEntity() != null) menu.blockEntity().cycleMode();
            if (context.player().containerMenu instanceof FluidBarrelMenu menu
                    && menu.blockEntity() != null) menu.blockEntity().cycleMode();
        });
    }
}
