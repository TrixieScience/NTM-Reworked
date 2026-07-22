package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.TurretFriendlyMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TurretWhitelistPayload(int action, String name, int index) implements CustomPacketPayload {
    public static final Type<TurretWhitelistPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "turret_whitelist"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TurretWhitelistPayload> STREAM_CODEC =
            StreamCodec.ofMember(TurretWhitelistPayload::encode, TurretWhitelistPayload::decode);

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(action);
        buffer.writeUtf(name, 25);
        buffer.writeVarInt(index);
    }

    private static TurretWhitelistPayload decode(RegistryFriendlyByteBuf buffer) {
        return new TurretWhitelistPayload(buffer.readVarInt(), buffer.readUtf(25), buffer.readVarInt());
    }

    @Override public Type<TurretWhitelistPayload> type() { return TYPE; }

    public static void handle(TurretWhitelistPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof TurretFriendlyMenu menu) {
                menu.editWhitelist(payload.action, payload.name, payload.index);
            }
        });
    }
}
