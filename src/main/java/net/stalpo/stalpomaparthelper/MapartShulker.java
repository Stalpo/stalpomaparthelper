package net.stalpo.stalpomaparthelper;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.EmptyMapItem;
import net.minecraft.item.Items;
import net.stalpo.stalpomaparthelper.interfaces.InventoryExporter;
import net.stalpo.stalpomaparthelper.interfaces.FakeMapRenderer;
import net.stalpo.stalpomaparthelper.interfaces.SlotClicker;
import net.stalpo.stalpomaparthelper.mixin.MapRendererInvoker;
import net.stalpo.stalpomaparthelper.mixin.MapTextureAccessor;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.item.FilledMapItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.map.MapState;
import net.minecraft.client.render.MapRenderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapartShulker {
    public static ScreenHandler sh;
    private static MapState mapState;
    private static int mapId;
    private static int nextMap;
    private static int nextSMIMap;

    private static List<MapState> states;

    public static String mapName = "StalpoIsAwesome";
    public static int mapX = 1;
    public static int mapY = 1;
    public static int currX = 0;
    public static int currY = 0;

    public static List<Integer> getIds(){
        Inventory inventory = ((InventoryExporter)sh).getInventory();
        List<Integer> ids = new ArrayList<Integer>();;
        states = new ArrayList<MapState>();

        for(int i = 0; i < inventory.size(); i++){
            if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                continue;
            }

            mapId = FilledMapItem.getMapId(inventory.getStack(i));
            ids.add(mapId);
            states.add(FilledMapItem.getMapState(mapId, MinecraftClient.getInstance().world));
        }

        return ids;
    }

    public static List<MapState> getStates(){
        return states;
    }

    public static void downloadShulker(){
        if(sh != null){
            StalpoMapartHelper.LOGCHAT("Downloading shulker");
            Inventory inventory = ((InventoryExporter)sh).getInventory();

            ((FakeMapRenderer)MinecraftClient.getInstance().currentScreen).stalpomaparthelper$renderMaps(getIds(), getStates());

            for(int i = 0; i < inventory.size(); i++){
                if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                    continue;
                }

                mapId = FilledMapItem.getMapId(inventory.getStack(i));
                mapState = FilledMapItem.getMapState(mapId, MinecraftClient.getInstance().world);
                if(StalpoMapartHelper.SMIDownloadModeToggled){
                    downloadMap("maparts_smi", getSMIFileName(nextSMIMap));
                    nextSMIMap = getNextSMIMap();
                }else{
                    downloadMap("maparts", getFileName(nextMap));
                    nextMap = getNextMap();
                }
            }
            StalpoMapartHelper.LOGCHAT("Finished downloading shulker");
        }
    }

    public static void findDuplicates(){
        if(sh != null){
            StalpoMapartHelper.LOGCHAT("Finding duplicates");
            Inventory inventory = ((InventoryExporter)sh).getInventory();

            ((FakeMapRenderer)MinecraftClient.getInstance().currentScreen).stalpomaparthelper$renderMaps(getIds(), getStates());

            List<Integer> duplicates = new ArrayList<Integer>();

            File dump = new File(MinecraftClient.getInstance().runDirectory, "maparts_dump");
            for(File f : dump.listFiles()){
                f.delete();
            }

            for(int i = 0; i < inventory.size(); i++){
                if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                    continue;
                }

                mapId = FilledMapItem.getMapId(inventory.getStack(i));
                mapState = FilledMapItem.getMapState(mapId, MinecraftClient.getInstance().world);
                downloadMap("maparts_dump", "map"+i+".png");

                File f1 = new File(new File(MinecraftClient.getInstance().runDirectory, "maparts_dump"), getFileName(i));
                File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts");
                for(File f2 : checkdir.listFiles()){
                    if(ImageHelper.sameImage(f1, f2)){
                        StalpoMapartHelper.LOGCHAT("Duplicate found! (in downloads)");
                        duplicates.add((Integer)i);
                        break;
                    }
                }
                checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts_dump");
                for(File f2 : checkdir.listFiles()){
                    if(!f2.getPath().equals(f1.getPath())){
                        if(ImageHelper.sameImage(f1, f2)){
                            StalpoMapartHelper.LOGCHAT("Duplicate found! (in this shulk)");
                            duplicates.add((Integer)i);
                            break;
                        }
                    }
                }
            }
            for(Integer i : duplicates){
                swap(i, i+27);
            }
            StalpoMapartHelper.LOGCHAT("Finished finding duplicates");
        }
    }

    public static void getSMI(){
        if(sh != null){
            StalpoMapartHelper.LOGCHAT("Getting SMIs");
            Inventory inventory = ((InventoryExporter)sh).getInventory();

            ((FakeMapRenderer)MinecraftClient.getInstance().currentScreen).stalpomaparthelper$renderMaps(getIds(), getStates());

            for(int i = 0; i < inventory.size(); i++){
                if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                    continue;
                }

                mapId = FilledMapItem.getMapId(inventory.getStack(i));
                mapState = FilledMapItem.getMapState(mapId, MinecraftClient.getInstance().world);
                downloadMap("maparts_dump", "map"+i+".png");

                File f1 = new File(new File(MinecraftClient.getInstance().runDirectory, "maparts_dump"), getFileName(i));
                File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts_smi");
                String name = null;
                for(File f2 : checkdir.listFiles()){
                    if(ImageHelper.sameImage(f1, f2)){
                        name = f2.getName();
                        break;
                    }
                }
                if(name == null){
                    StalpoMapartHelper.CHAT("Slot: " + i + " MapId: " + mapId + " SMI: not in database");
                }else{
                    StalpoMapartHelper.CHAT("Slot: " + i + " MapId: " + mapId + " SMI: " + name.substring(0, name.length() - 4));
                }
            }
            StalpoMapartHelper.LOGCHAT("Finished getting SMIs");
        }
    }

    private static void downloadMap(String DirName, String FileName){

        MapRenderer.MapTexture txt = ((MapRendererInvoker)MinecraftClient.getInstance().gameRenderer.getMapRenderer()).invokeGetMapTexture(mapId, mapState);

        File screensDir = new File(MinecraftClient.getInstance().runDirectory, DirName);
        if(!screensDir.exists() && !screensDir.mkdir()) {
            StalpoMapartHelper.ERROR("Could not create directory " + screensDir.getAbsolutePath() + " cannot continue!");
            return;
        }

        File screenshot = new File(screensDir, FileName);

        //Util.getIoWorkerExecutor().execute(() -> {
            try {
                ((MapTextureAccessor)txt).getNativeImage().getImage().writeTo(screenshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        //});
    }

    public static void lockShulkerCheck(){
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();

        for(int i = 9; i < 36; i++){
            if(inventory.getStack(i).getItem().getClass() == FilledMapItem.class){
                putShulker();
                return;
            }
        }
        //findNotLocked(client);
    }

    public static void findNotLocked(){
        if(sh != null) {
            StalpoMapartHelper.LOGCHAT("Finding not locked");
            Inventory inventory = ((InventoryExporter) sh).getInventory();

            ((FakeMapRenderer)MinecraftClient.getInstance().currentScreen).stalpomaparthelper$renderMaps(getIds(), getStates());

            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).getItem().getClass() != FilledMapItem.class) {
                    continue;
                }

                mapId = FilledMapItem.getMapId(inventory.getStack(i));
                mapState = FilledMapItem.getMapState(mapId, MinecraftClient.getInstance().world);

                if (!mapState.locked) {
                    swap(i, i+27);
                }
            }
            StalpoMapartHelper.LOGCHAT("Finished finding not locked");
        }
    }

    public static void putTakeCheck(){
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();

        for(int i = 9; i < 36; i++){
            if(inventory.getStack(i).getItem().getClass() == FilledMapItem.class){
                putShulker();
                return;
            }
        }
        takeShulker();
    }

    private static void takeShulker(){
        StalpoMapartHelper.LOGCHAT("Taking shulker");
        Inventory inventory = ((InventoryExporter)sh).getInventory();
        for(int i = 0; i < inventory.size(); i++){
            moveOne(i, i+27);
        }
    }

    private static void putShulker(){
        StalpoMapartHelper.LOGCHAT("Putting shulker");
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }
            moveOne(i+27, i);
        }
    }

    public static void copyMaps(){
        StalpoMapartHelper.LOGCHAT("Copying maps");
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }
            moveOne(i+10, 1);
            boolean found = false;
            for(int k = 0; k < 9; k++){
                if(inventory.getStack(k).getItem().getClass() == EmptyMapItem.class){
                    moveOne(k+37, 2);
                    found = true;
                    break;
                }
            }
            if(!found){
                StalpoMapartHelper.LOGCHAT("Ran out of empty maps");
                return;
            }
            sh.onContentChanged(inventory);
            swap(0, i+10);
        }
        StalpoMapartHelper.LOGCHAT("Finished copying maps");
    }

    public static void lockMaps(){
        StalpoMapartHelper.LOGCHAT("Locking maps");
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }
            moveOne(i+3, 0);
            boolean found = false;
            for(int k = 0; k < 9; k++){
                if(inventory.getStack(k).getItem() == Items.GLASS_PANE){
                    moveOne(k+30, 1);
                    found = true;
                    break;
                }
            }
            if(!found){
                StalpoMapartHelper.LOGCHAT("Ran out of glass panes");
                return;
            }
            sh.onContentChanged(inventory);
            swap(2, i+3);
        }
        StalpoMapartHelper.LOGCHAT("Finished locking maps");
    }

    public static void nameMaps(){
        StalpoMapartHelper.LOGCHAT("Naming shulk");
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }
            if(MinecraftClient.getInstance().player.experienceLevel == 0){
                StalpoMapartHelper.LOGCHAT("Ran out of xp");
                return;
            }
            moveOne(i+3, 0);

            ((AnvilScreen)MinecraftClient.getInstance().currentScreen).onRenamed(mapName + " (" + currX + ", " + currY + ")");
            currX++;
            if(currX == mapX){
                currX = 0;
                currY ++;
                if(currY == mapY){
                    currY = 0;
                    swap(2, i+3);
                    StalpoMapartHelper.LOGCHAT("Finished naming map! :D");
                    StalpoMapartHelper.mapNamerToggled = false;
                    StalpoMapartHelper.LOGCHAT("Map namer disabled!");
                    return;
                }
            }

            swap(2, i+3);
        }
        StalpoMapartHelper.LOGCHAT("Finished naming shulk");
    }

    public static void nameSMIMaps(){
        StalpoMapartHelper.LOGCHAT("Naming shulk");

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Inventory inventory = MinecraftClient.getInstance().player.getInventory();
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }

            mapId = FilledMapItem.getMapId(inventory.getStack(i+9));
            mapState = FilledMapItem.getMapState(mapId, MinecraftClient.getInstance().world);
            ((FakeMapRenderer)MinecraftClient.getInstance().currentScreen).stalpomaparthelper$renderMap(mapId, mapState);
            downloadMap("maparts_dump", "map"+i+".png");

            File f1 = new File(new File(MinecraftClient.getInstance().runDirectory, "maparts_dump"), getFileName(i));
            File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts_smi");
            String name = null;
            for(File f2 : checkdir.listFiles()){
                if(ImageHelper.sameImage(f1, f2)){
                    name = f2.getName();
                    break;
                }
            }
            if(name == null){
                StalpoMapartHelper.CHAT("Slot: " + i + " MapId: " + mapId + " SMI: not in database");
                continue;
            }

            String n = inventory.getStack(i+9).getItem().getName().toString();
            if (n.equals("Map")){
                n = "";
            }

            if(MinecraftClient.getInstance().player.experienceLevel == 0){
                StalpoMapartHelper.LOGCHAT("Ran out of xp");
                return;
            }
            moveOne(i+3, 0);

            ((AnvilScreen)MinecraftClient.getInstance().currentScreen).onRenamed(n + name.substring(0, name.length() - 4));

            swap(2, i+3);
        }
        StalpoMapartHelper.LOGCHAT("Finished naming shulk");
    }

    private static void moveOne(int from, int to){
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, to, 1, SlotActionType.PICKUP);
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
    }

    private static void moveHalf(int from, int to){
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 1, SlotActionType.PICKUP);
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, to, 0, SlotActionType.PICKUP);
    }

    private static void swap(int from, int to){
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, to, 0, SlotActionType.PICKUP);
        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
    }

    public static void setNextMap(){
        File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts");
        int i = 0;
        while(new File(checkdir, getFileName(i)).isFile()){
            i++;
        }
        nextMap = i;
        checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts_smi");
        i = 0;
        while(new File(checkdir, getSMIFileName(i)).isFile()){
            i++;
        }
        nextSMIMap = i;
        StalpoMapartHelper.LOG("nextMap: " + nextMap + " nextSMIMap: " + nextSMIMap);
    }

    private static String getFileName(int i){
        return ("map"+i+".png");
    }

    private static String getSMIFileName(int i){
        return ("SMI_"+i+"_s"+(i / 27)+"s"+(i % 27)+".png");
    }

    private static int getNextMap(){
        File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts");
        int i = nextMap + 1;
        while(new File(checkdir, getFileName(i)).isFile()){
            i++;
        }
        return i;

    }

    private static int getNextSMIMap(){
        File checkdir = new File(MinecraftClient.getInstance().runDirectory, "maparts_smi");
        int i = nextSMIMap + 1;
        while(new File(checkdir, getSMIFileName(i)).isFile()){
            i++;
        }
        return i;
    }
}
