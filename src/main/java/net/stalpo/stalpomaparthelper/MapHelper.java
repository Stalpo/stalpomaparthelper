package net.stalpo.stalpomaparthelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.item.map.MapState;
import net.stalpo.stalpomaparthelper.mixin.MapRendererInvoker;
import net.stalpo.stalpomaparthelper.mixin.MapTextureAccessor;

import java.io.File;
import java.io.IOException;

public class MapHelper {
    public static boolean downloadMap(String DirName, String FileName, int mapId, MapState mapState){

        MapRenderer.MapTexture txt;
        try{
            txt = ((MapRendererInvoker) MinecraftClient.getInstance().gameRenderer.getMapRenderer()).invokeGetMapTexture(mapId, mapState);
        }catch(Exception e){
            StalpoMapartHelper.CHAT("MapId " + mapId + " wasn't loaded! Make sure all maps are loaded (more info in readme)");
            return false;
        }

        File screensDir = new File(StalpoMapartHelper.modFolder, DirName);
        if(!screensDir.exists() && !screensDir.mkdir()) {
            StalpoMapartHelper.ERROR("Could not create directory " + screensDir.getAbsolutePath() + " cannot continue!");
            return false;
        }

        File map = new File(screensDir, FileName);

        try {
            ((MapTextureAccessor)txt).getNativeImage().getImage().writeTo(map);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if(screensDir.equals("maparts_smi")){
            ImageHelper.addImage(true, map);
        }else if(screensDir.equals("maparts")){
            ImageHelper.addImage(false, map);
        }

        return true;
    }

    public static void downloadMapFromDat(){

    }
}
