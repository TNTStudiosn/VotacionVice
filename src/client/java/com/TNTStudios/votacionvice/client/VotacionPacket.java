package com.TNTStudios.votacionvice.client;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Map;

public class VotacionPacket {
    public static void send(String equipo, Map<String, Integer> votos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeString(equipo);
        buf.writeMap(votos, FriendlyByteBuf::writeString, FriendlyByteBuf::writeInt);
        ClientPlayNetworking.send(PacketID.VOTACION, buf);
    }
}

