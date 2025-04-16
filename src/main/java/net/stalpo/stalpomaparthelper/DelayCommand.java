package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class DelayCommand {
    public static void registerChangeDelay(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("delay")
                .then(argument("value", integer())
                        .executes(ctx -> setDelay(getInteger(ctx, "value")
                                )
                        )
                )
        );
    }

    public static int setDelay(int delay) {
        if (delay < 0) {
            StalpoMapartHelper.CHAT("Delay can't be negative!");
            return Command.SINGLE_SUCCESS;  // wha is it i still don't understa
        }

        int oldDelay = MapartShulker.delay;
        MapartShulker.delay = delay;
        StalpoMapartHelper.CHAT("New delay set: " + delay + "ms. Old delay: " + oldDelay + "ms");

        return Command.SINGLE_SUCCESS;
    }
}