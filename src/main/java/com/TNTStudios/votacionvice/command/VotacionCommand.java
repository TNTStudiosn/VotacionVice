package com.TNTStudios.votacionvice.command;

import com.TNTStudios.votacionvice.network.NetworkHandler;
import com.TNTStudios.votacionvice.util.LuckPermsUtil;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicInteger;


import java.util.List;

public class VotacionCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("votacion")
                .requires(src -> src.hasPermissionLevel(4))
                .then(CommandManager.literal("iniciar")
                        .then(CommandManager.argument("equipo", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of("1","2","3","4","5","6","7"), builder))
                                .executes(ctx -> {
                                    String equipo = StringArgumentType.getString(ctx, "equipo");
                                    ServerCommandSource source = ctx.getSource();
                                    ServerWorld world = source.getWorld();
                                    MinecraftServer server = world.getServer();

                                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                        if (LuckPermsUtil.hasPermission(player, "votacion.juez")) {
                                            NetworkHandler.sendOpenGuiPacket(player, equipo);
                                        }
                                    }


                                    source.sendFeedback(() -> Text.literal("Votación iniciada para equipo " + equipo), false);
                                    return 1;
                                }
                                )
                        )
                )
                .then(CommandManager.literal("media")
                        .then(CommandManager.argument("equipo", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of("1","2","3","4","5","6","7"), builder))
                                .executes(ctx -> {
                                    String equipo = StringArgumentType.getString(ctx, "equipo");
                                    ServerCommandSource source = ctx.getSource();

                                    Path folder = FabricLoader.getInstance().getConfigDir().resolve("Votaciones/equipo" + equipo);
                                    if (!Files.exists(folder)) {
                                        source.sendError(Text.literal("No hay votaciones para el equipo " + equipo));
                                        return 0;
                                    }

                                    try (Stream<Path> archivos = Files.list(folder)) {
                                        List<Double> medias = archivos
                                                .filter(p -> p.toString().endsWith(".json"))
                                                .map(path -> {
                                                    try (Reader reader = Files.newBufferedReader(path)) {
                                                        JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                                                        return json.has("media") ? json.get("media").getAsDouble() : null;
                                                    } catch (IOException e) {
                                                        System.err.println("Error leyendo archivo: " + path + ": " + e.getMessage());
                                                        return null;
                                                    }
                                                })
                                                .filter(Objects::nonNull)
                                                .toList();

                                        if (medias.isEmpty()) {
                                            source.sendError(Text.literal("No hay votos válidos para el equipo " + equipo));
                                            return 0;
                                        }

                                        double promedio = medias.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                                        // Guardar la calificación final en archivo
                                        JsonObject jsonFinal = new JsonObject();
                                        jsonFinal.addProperty("equipo", equipo);
                                        jsonFinal.addProperty("media_final", promedio);

                                        Path resultado = FabricLoader.getInstance().getConfigDir()
                                                .resolve("Votaciones/Calificacionesfinales/CalifEquipo" + equipo + ".json");

                                        try {
                                            Files.createDirectories(resultado.getParent());
                                            Files.writeString(resultado, new GsonBuilder().setPrettyPrinting().create().toJson(jsonFinal));
                                        } catch (IOException e) {
                                            source.sendError(Text.literal("No se pudo guardar la calificación final: " + e.getMessage()));
                                            return 0;
                                        }

                                        source.sendFeedback(() -> Text.literal("Media final del equipo " + equipo + ": " + String.format("%.2f", promedio)), false);
                                        return 1;

                                    } catch (IOException e) {
                                        source.sendError(Text.literal("Error leyendo archivos de votación: " + e.getMessage()));
                                        return 0;
                                    }
                                })
                        )
                )
                .then(CommandManager.literal("quitarvotos")
                        .requires(src -> src.hasPermissionLevel(4))
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            Path basePath = FabricLoader.getInstance().getConfigDir().resolve("Votaciones");

                            if (!Files.exists(basePath)) {
                                source.sendFeedback(() -> Text.literal("No hay datos de votación para eliminar."), false);
                                return 1;
                            }

                            AtomicInteger archivosEliminados = new AtomicInteger(0);

                            try {
                                // Eliminar archivos dentro de cada carpeta de equipo
                                for (int i = 1; i <= 7; i++) {
                                    Path equipoPath = basePath.resolve("equipo" + i);
                                    if (Files.exists(equipoPath)) {
                                        try (Stream<Path> files = Files.list(equipoPath)) {
                                            files.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                                                try {
                                                    Files.deleteIfExists(p);
                                                    archivosEliminados.incrementAndGet();
                                                } catch (IOException e) {
                                                    System.err.println("Error al borrar archivo: " + p);
                                                }
                                            });
                                        }
                                    }
                                }

                                // Eliminar archivos de calificaciones finales
                                Path finalesPath = basePath.resolve("Calificacionesfinales");
                                if (Files.exists(finalesPath)) {
                                    try (Stream<Path> files = Files.list(finalesPath)) {
                                        files.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                                            try {
                                                Files.deleteIfExists(p);
                                                archivosEliminados.incrementAndGet();
                                            } catch (IOException e) {
                                                System.err.println("Error al borrar archivo: " + p);
                                            }
                                        });
                                    }
                                }

                                int total = archivosEliminados.get();
                                source.sendFeedback(() -> Text.literal("Se eliminaron " + total + " archivos de votación."), false);
                                return 1;

                            } catch (IOException e) {
                                source.sendError(Text.literal("Error al eliminar archivos: " + e.getMessage()));
                                return 0;
                            }
                        })
                )
                .then(CommandManager.literal("removervoto")
                        .requires(src -> src.hasPermissionLevel(4))
                        .then(CommandManager.argument("equipo", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of("1","2","3","4","5","6","7"), builder))
                                .then(CommandManager.argument("juez", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String equipo = StringArgumentType.getString(ctx, "equipo");
                                            String juez = StringArgumentType.getString(ctx, "juez");
                                            ServerCommandSource source = ctx.getSource();

                                            Path archivo = FabricLoader.getInstance().getConfigDir()
                                                    .resolve("Votaciones/equipo" + equipo + "/" + juez + ".json");

                                            if (!Files.exists(archivo)) {
                                                source.sendError(Text.literal("No existe voto registrado de '" + juez + "' para el equipo " + equipo));
                                                return 0;
                                            }

                                            try {
                                                Files.delete(archivo);
                                                source.sendFeedback(() -> Text.literal("Voto de '" + juez + "' para equipo " + equipo + " ha sido eliminado."), false);
                                                return 1;
                                            } catch (IOException e) {
                                                source.sendError(Text.literal("Error al eliminar el voto: " + e.getMessage()));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("removercalif")
                        .requires(src -> src.hasPermissionLevel(4))
                        .then(CommandManager.argument("equipo", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of("1","2","3","4","5","6","7"), builder))
                                .executes(ctx -> {
                                    String equipo = StringArgumentType.getString(ctx, "equipo");
                                    ServerCommandSource source = ctx.getSource();

                                    Path archivo = FabricLoader.getInstance().getConfigDir()
                                            .resolve("Votaciones/Calificacionesfinales/CalifEquipo" + equipo + ".json");

                                    if (!Files.exists(archivo)) {
                                        source.sendError(Text.literal("No existe calificación final para el equipo " + equipo));
                                        return 0;
                                    }

                                    try {
                                        Files.delete(archivo);
                                        source.sendFeedback(() -> Text.literal("Calificación final del equipo " + equipo + " ha sido eliminada."), false);
                                        return 1;
                                    } catch (IOException e) {
                                        source.sendError(Text.literal("Error al eliminar la calificación: " + e.getMessage()));
                                        return 0;
                                    }
                                })
                        )
                )
                .then(CommandManager.literal("establecerjueces")
                        .requires(src -> src.hasPermissionLevel(4))
                        .then(CommandManager.argument("cantidad", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int cantidad = IntegerArgumentType.getInteger(ctx, "cantidad");
                                    ServerCommandSource source = ctx.getSource();

                                    JsonObject json = new JsonObject();
                                    json.addProperty("numero", cantidad);

                                    Path archivo = FabricLoader.getInstance().getConfigDir().resolve("Votaciones/NumeroDeJueces.json");

                                    try {
                                        Files.createDirectories(archivo.getParent());
                                        Files.writeString(archivo, new GsonBuilder().setPrettyPrinting().create().toJson(json));
                                        source.sendFeedback(() -> Text.literal("Número de jueces establecido en: " + cantidad), false);
                                        return 1;
                                    } catch (IOException e) {
                                        source.sendError(Text.literal("Error al guardar el archivo: " + e.getMessage()));
                                        return 0;
                                    }
                                })
                        )
                )
        );
    }
}
