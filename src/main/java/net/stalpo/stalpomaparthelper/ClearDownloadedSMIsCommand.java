package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class ClearDownloadedSMIsCommand {
    public static void registerClearDownloadedSMIs(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(literal("clearDownloadedSMIs")
                .executes(ctx -> ClearDownloadedSMIs()));
    }

    public static int ClearDownloadedSMIs() {
        File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts_smi");
        for(File f : checkdir.listFiles()){
            f.delete();
        }
        MapartShulker.setNextMap();

        ImageHelper.clearImages(true);

        StalpoMapartHelper.CHAT("Cleared downloaded SMIs");
        return Command.SINGLE_SUCCESS;
    }
}