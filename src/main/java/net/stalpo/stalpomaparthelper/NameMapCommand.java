package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.stalpo.stalpomaparthelper.sequence.NameSequence;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class NameMapCommand {
    public static void registerNameMap(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("nameMap")
                .then(argument("x", integer())
                        .then(argument("y", integer())
                                .then(argument("increment Y", bool())
                                        .then(argument("name", greedyString())
                                                .executes(ctx -> NameMap(
                                                                getString(ctx, "name"),
                                                                getInteger(ctx, "x"),
                                                                getInteger(ctx, "y"),
                                                                getBool(ctx, "increment Y")
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    public static void registerNameMapWithoutY(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("nameMap")
                .then(argument("total", integer())
                        .then(argument("name", greedyString())
                                .executes(ctx -> NameMapWithoutY(
                                                getString(ctx, "name"),
                                                getInteger(ctx, "total")
                                        )
                                )
                        )
                )
        );
    }

    public static int NameMap(String name, int x, int y, boolean incrementY) {
        if (!name.contains("{x}") || !name.contains("{y}")) {
            StalpoMapartHelper.CHAT("You have to specify both §4{x}§r and §4{y}§r in the name!\nIf you're using \"§2{§r\" or \"§2}§r\" in the name, just use them as is: §2{§4{x}§r, §4{y}§2}§r");
            return Command.SINGLE_SUCCESS;  // wha is it
        }

        MapartShulker.sequence = new NameSequence(name, x, y, incrementY, true);

        StalpoMapartHelper.CHAT("Set next map for map namer to: \"" + name + "\" with the dimensions: " + x + " by " + y);

        return Command.SINGLE_SUCCESS;
    }


    public static int NameMapWithoutY(String name, int x) {
        if (!name.contains("{cur}") || !name.contains("{total}")) {
            StalpoMapartHelper.CHAT("You have to specify both §4{cur}§r and §4{total}§r in the name!\nIf you're using \"§2{§r\" or \"§2}§r\" in the name, just use them as is: §2{§4{cur}§r, §4{x}§2}§r");
            return Command.SINGLE_SUCCESS;  // wha is it
        }

        MapartShulker.sequence = new NameSequence(name, x, -1, true, false);

        StalpoMapartHelper.CHAT("Set next map for map namer to: \"" + name + "\"" + x + " total of " + x);

        return Command.SINGLE_SUCCESS;
    }
}