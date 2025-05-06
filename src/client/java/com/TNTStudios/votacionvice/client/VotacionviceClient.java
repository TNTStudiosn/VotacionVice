package com.TNTStudios.votacionvice.client;

import com.TNTStudios.votacionvice.client.PantallaVotacion;
import com.TNTStudios.votacionvice.network.PacketID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public class VotacionviceClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PacketID.ABRIR_GUI, (client, handler, buf, sender) -> {
            String equipo = buf.readString();
            client.execute(() -> {
                MinecraftClient.getInstance().setScreen(new PantallaVotacion(equipo));
            });
        });
    }
}
