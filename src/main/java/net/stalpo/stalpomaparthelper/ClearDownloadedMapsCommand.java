package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class ClearDownloadedMapsCommand {
    public static void registerClearDownloadedMaps(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(literal("clearDownloadedMaps")
                .executes(ctx -> ClearDownlaodedMaps()));
    }

    public static int ClearDownlaodedMaps() {
        File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts");
        for(File f : checkdir.listFiles()){
            f.delete();
        }
        MapartShulker.setNextMap();

        ImageHelper.clearImages(false);

        StalpoMapartHelper.CHAT("Cleared downloaded maps");
        return Command.SINGLE_SUCCESS;
    }
}