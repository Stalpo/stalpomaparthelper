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
        dispatcher.register(literal("nameMap")
                .then(argument("x", integer())
                        .then(argument("y", integer())
                                        .then(argument("name", greedyString())
                                                .executes(ctx -> NameMap(getString(ctx, "name"), getInteger(ctx, "x"), getInteger(ctx, "y"),
                                                        getInteger(ctx, "delay")
                                                ))))));
    }

    public static int NameMap(String name, int x, int y, int delay) {
        if (!name.contains("{x}") || !name.contains("{y}")) {
            StalpoMapartHelper.CHAT("You have to specify both §4{x}§r and §4{y}§r in the name!\nIf you're using \"§2{§r\" or \"§2}§r\" in the name, just use them as is: §2{§4{x}§r, §4{y}§4}§r");
            return Command.SINGLE_SUCCESS;  // wha is it
        }

        MapartShulker.mapName = name;
        MapartShulker.mapX = x;
        MapartShulker.mapY = y;
        MapartShulker.currX = 0;
        MapartShulker.currY = 0;

        StalpoMapartHelper.CHAT("Set next map for map namer to: \"" + name + "\" with the dimensions: " + x + " by " + y);

        return Command.SINGLE_SUCCESS;
    }
}