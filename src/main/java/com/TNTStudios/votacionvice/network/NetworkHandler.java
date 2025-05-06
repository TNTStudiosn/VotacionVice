package com.TNTStudios.votacionvice.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler {
    public static void sendOpenGuiPacket(ServerPlayerEntity player, String equipo) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(equipo);
        ServerPlayNetworking.send(player, PacketID.ABRIR_GUI, buf);
    }
}
