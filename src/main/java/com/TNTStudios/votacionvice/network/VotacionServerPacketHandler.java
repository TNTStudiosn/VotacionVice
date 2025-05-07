package com.TNTStudios.votacionvice.network;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class VotacionServerPacketHandler {

    public static final Identifier VOTACION = new Identifier("votacionvice", "votacion");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(VOTACION, (server, player, handler, buf, responseSender) -> {
            String equipo = buf.readString();
            Map<String, Integer> votos = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readInt);
            double media = votos.values().stream().mapToInt(i -> i).average().orElse(0);

            Path path = FabricLoader.getInstance().getConfigDir()
                    .resolve("Votaciones/equipo" + equipo + "/" + player.getName().getString() + ".json");

            // Si ya votó, ignorar
            if (Files.exists(path)) {
                System.out.println("[VotacionVice] El jugador " + player.getName().getString() + " ya votó por el equipo " + equipo + ". Voto descartado.");
                return;
            }

            JsonObject json = new JsonObject();
            votos.forEach(json::addProperty);
            json.addProperty("media", media);

            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(json));
                System.out.println("[VotacionVice] Voto registrado para " + player.getName().getString() + " en equipo " + equipo);
            } catch (IOException e) {
                System.err.println("Error al guardar votación del jugador " + player.getName().getString() + ": " + e.getMessage());
            }
        });
    }
}
