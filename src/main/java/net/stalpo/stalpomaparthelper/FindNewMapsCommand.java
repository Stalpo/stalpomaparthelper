package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class FindNewMapsCommand {
    public static void registerFindNewMaps(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(literal("findNewMaps")
                .then(argument("newFolder", greedyString())
                        .executes(ctx -> FindNewMaps(getString(ctx, "newFolder")))));
    }

    public static int FindNewMaps(String folder) {
        StalpoMapartHelper.CHAT("Finding new maps in folder (new compared to maparts folder): " + folder);

        File checkDir = new File(StalpoMapartHelper.modFolder, folder);
        if(!checkDir.exists()){
            StalpoMapartHelper.CHAT("Folder " + folder + " doesn't exist!");
            return Command.SINGLE_SUCCESS;
        }

        ImageHelper.initializeImages();
        for(File f : checkDir.listFiles()){
            if(ImageHelper.isDuplicate(f)){
                f.delete();
            }
        }

        StalpoMapartHelper.CHAT("Found new maps and deleted all maps in the " + folder + " folder that weren't new");
        return Command.SINGLE_SUCCESS;
    }
}