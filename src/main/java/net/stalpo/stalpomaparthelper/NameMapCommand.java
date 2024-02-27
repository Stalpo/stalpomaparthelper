package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class NameMapCommand {
    public static void registerNameMap(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(literal("NameMap")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("x", integer())
                        .then(argument("y", integer())
                                .then(argument("name", greedyString())
                                        .executes(ctx -> NameMap(getString(ctx, "name"), getInteger(ctx, "x"), getInteger(ctx, "y")))))));
    }

    public static int NameMap(String name, int x, int y) {
        MapartShulker.mapName = name;
        MapartShulker.mapX = x;
        MapartShulker.mapY = y;
        MapartShulker.currX = 0;
        MapartShulker.currY = 0;

        StalpoMapartHelper.LOGCHAT("Set next map for map namer to: \"" + name + "\"");

        return Command.SINGLE_SUCCESS;
    }
}