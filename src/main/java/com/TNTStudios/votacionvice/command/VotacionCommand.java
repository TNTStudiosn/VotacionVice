package com.TNTStudios.votacionvice.command;

import com.TNTStudios.votacionvice.network.NetworkHandler;
import com.TNTStudios.votacionvice.util.LuckPermsUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;


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
                                }))));
    }
}
