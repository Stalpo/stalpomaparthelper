package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class SetMaxWrongPixelsCommand {
    public static void registerSetMaxWrongPixels(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(literal("setMaxWrongPixels")
                .then(argument("max", integer())
                        .executes(ctx -> SetMaxWrongPixels(getInteger(ctx, "max")))));
    }

    public static int SetMaxWrongPixels(int maxWrong) {
        ImageHelper.maxWrong = maxWrong;

        StalpoMapartHelper.CHAT("Set max wrong pixels for duplicate checker and smi lookup to: " + maxWrong);

        return Command.SINGLE_SUCCESS;
    }
}