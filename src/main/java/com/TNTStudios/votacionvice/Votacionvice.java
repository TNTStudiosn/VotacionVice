package com.TNTStudios.votacionvice;

import com.TNTStudios.votacionvice.command.VotacionCommand;
import com.TNTStudios.votacionvice.network.VotacionServerPacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Votacionvice implements ModInitializer {

    @Override
    public void onInitialize() {
        VotacionServerPacketHandler.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            VotacionCommand.register(dispatcher);
        });

    }
}
