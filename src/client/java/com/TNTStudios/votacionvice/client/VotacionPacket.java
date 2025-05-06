package com.TNTStudios.votacionvice.client;

import com.TNTStudios.votacionvice.network.PacketID;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.util.Map;

public class VotacionPacket {
    public static void send(String equipo, Map<String, Integer> votos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(equipo);
        buf.writeMap(votos, PacketByteBuf::writeString, PacketByteBuf::writeInt);
        ClientPlayNetworking.send(PacketID.VOTACION, buf);
    }
}
