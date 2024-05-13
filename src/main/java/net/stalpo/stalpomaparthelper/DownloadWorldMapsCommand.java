package net.stalpo.stalpomaparthelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.map.MapState;
import net.minecraft.util.WorldSavePath;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public final class DownloadWorldMapsCommand {

    public static void registerDownloadWorldMaps(CommandDispatcher<FabricClientCommandSource> dispatcher){
        dispatcher.register(literal("downloadWorldMaps")
                .executes(ctx -> DownloadWorldMaps()));
    }

    private static int DownloadWorldMaps() {
        if (!MinecraftClient.getInstance().world.isClient) {
            StalpoMapartHelper.CHAT("You have to be in a single player world to download world maps!");
            return Command.SINGLE_SUCCESS;
        }

        String save = MinecraftClient.getInstance().getServer().getSavePath(WorldSavePath.ROOT).toString();
        save = save.substring(0, save.length() - 2);
        File dataDir = new File(save, "data");
        for(File f : dataDir.listFiles()){
            if(f.getName().substring(0, 4).equals("map_")){
                String mapId = f.getName().substring(4, f.getName().length() - 4);
                MapState mapState = MinecraftClient.getInstance().world.getMapState(mapId);

                MapHelper.downloadMap("maparts", MapartShulker.getFileName(MapartShulker.nextMap), Integer.parseInt(mapId), mapState);
                MapartShulker.nextMap = MapartShulker.getNextMap();
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}