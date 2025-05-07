package com.TNTStudios.votacionvice.network;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import toni.immersivemessages.api.ImmersiveMessage;
import toni.immersivemessages.api.SoundEffect;
import toni.immersivemessages.api.TextAnchor;

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

            // 🔔 Enviar notificación a todos los jugadores
            server.execute(() -> {
                MinecraftServer mcServer = player.getServer();
                if (mcServer != null) {
                    String titulo   = "§l§b¡Voto Registrado!";
                    String subtitulo = "§7El juez §a" + player.getName().getString() + " §7envió su voto";

                    for (ServerPlayerEntity onlinePlayer : mcServer.getPlayerManager().getPlayerList()) {
                        double randomYOffset = Math.random() * 60 - 30;

                        ImmersiveMessage.builder(4.0f, titulo)
                                .anchor(TextAnchor.TOP_CENTER)
                                .wrap(200)
                                .y(50f + (float)randomYOffset)
                                .size(1.2f)
                                .background()
                                .slideDown(0.5f)
                                .slideOutUp(0.5f)
                                .fadeIn(0.5f)
                                .fadeOut(0.5f)
                                .sound(SoundEffect.LOW)
                                .typewriter(2.0f, true)
                                .subtext(0.5f, subtitulo, 15f, (subtext) -> subtext
                                        .anchor(TextAnchor.TOP_CENTER)
                                        .wrap(200)
                                        .size(1.0f)
                                        .slideDown(0.5f)        // Añadido para coordinar entrada
                                        .slideOutUp(0.5f)       // Añadido para coordinar salida
                                        .fadeIn(0.5f)
                                        .fadeOut(0.5f)
                                        .typewriter(1.5f, true)
                                )
                                .sendServer(onlinePlayer);

                        // Reproducir sonido adicional para llamar la atención
                        onlinePlayer.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    }
                }
            });



        });
    }
}
