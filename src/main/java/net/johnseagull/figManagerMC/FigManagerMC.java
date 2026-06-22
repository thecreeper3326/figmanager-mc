package net.johnseagull.figManagerMC;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.johnseagull.figManager.FigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.List;

/**
 * Main server-side manager for interpreting incoming config packets, saving/loading, conversion, and validation.
 */
public class FigManagerMC extends FigManager {



 
    @Override
    public void extension() {
        exName = "figManagerMC";
        PayloadTypeRegistry.playC2S().register(FigPacket.ID, FigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(FigPacket.ID, FigPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FigPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                LOGGER.warn("Received figs from client "+ context.player().getPlainTextName()+". Verifying...");
                if (context.player().permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) {
                    List<Object> e = validate(fromString(payload.figs(),FIGCLASS));

                    int errors = (int) e.get(1);
                    List<String> errorList = (List<String>) e.get(2);

                    FIGS = e.get(0);
                    if (errors != 0) {
                        LOGGER.error(errors + " errors occured:");
                        context.player().sendSystemMessage(Component.literal(errors + " options failed to process:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        for (String error : errorList) {
                            context.player().sendSystemMessage(Component.literal(error).withStyle(ChatFormatting.RED));
                            LOGGER.error(error);
                        }
                    } else {
                        context.player().sendSystemMessage(Component.literal("Updated Figs for " + name + "."));
                    }
                    for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                        ServerPlayNetworking.send(player, new FigPacket(
                                toString(FIGS)
                        ));
                    }


                    LOGGER.warn("Figs for " + name + " were modified by " + context.player().getPlainTextName());

                    FigManagerMC.save(name);
                } else {
                    LOGGER.error(context.player().getPlainTextName() + " attempted to modify figs without permission!");
                    context.player().sendSystemMessage(Component.literal("Failed to update figs, insufficient permissions"));
                }
            });
        });
        ServerPlayerEvents.JOIN.register(player -> {
            ServerPlayNetworking.send(player,new FigPacket(
                    toString(FIGS)
            ));
            LOGGER.info("Syncing figs for player " + player.getPlainTextName());
        });
    }


}