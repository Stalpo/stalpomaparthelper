package net.stalpo.stalpomaparthelper;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
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

public class MapartShulker {
    public static ScreenHandler sh;
    private static MapState mapState;
    private static int mapId;
    private static MapIdComponent mapIdComponent;
    private static int nextMap;
    private static int nextSMIMap;

    private static List<MapState> states;

    public static String mapName = "StalpoIsAwesome";
    public static int mapX = 1;
    public static int mapY = 1;
    public static int currX = 0;
    public static int currY = 0;

    public static List<MapIdComponent> getIds(){
        Inventory inventory = ((InventoryExporter)sh).getInventory();
        List<MapIdComponent> ids = new ArrayList<MapIdComponent>();;
        states = new ArrayList<MapState>();

        for(int i = 0; i < inventory.size(); i++){
            if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                continue;
            }

            mapIdComponent = (MapIdComponent)inventory.getStack(i).get(DataComponentTypes.MAP_ID);
            mapId = mapIdComponent.id();
            ids.add(mapIdComponent);
            states.add(FilledMapItem.getMapState(inventory.getStack(i), MinecraftClient.getInstance().world));
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

            renderMaps(getIds(), getStates());

            for(int i = 0; i < inventory.size(); i++){
                if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                    continue;
                }

                mapIdComponent = (MapIdComponent)inventory.getStack(i).get(DataComponentTypes.MAP_ID);
                mapId = mapIdComponent.id();
                mapState = FilledMapItem.getMapState(inventory.getStack(i), MinecraftClient.getInstance().world);
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

            renderMaps(getIds(), getStates());

            ImageHelper.initializeImages();

            List<Integer> duplicates = new ArrayList<Integer>();

            File dump = new File(StalpoMapartHelper.modFolder, "maparts_dump");
            for(File f : dump.listFiles()){
                f.delete();
            }

            for(int i = 0; i < inventory.size(); i++){
                if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                    continue;
                }

                mapIdComponent = (MapIdComponent)inventory.getStack(i).get(DataComponentTypes.MAP_ID);
                mapId = mapIdComponent.id();
                mapState = FilledMapItem.getMapState(inventory.getStack(i), MinecraftClient.getInstance().world);
                downloadMap("maparts_dump", "map"+i+".png");

                File f1 = new File(new File(StalpoMapartHelper.modFolder, "maparts_dump"), getFileName(i));

                if(ImageHelper.isDuplicate(f1)){
                    StalpoMapartHelper.LOGCHAT("Duplicate found! (in downloads)");
                    duplicates.add((Integer)i);
                }

                File checkdir = new File(StalpoMapartHelper.modFolder, "maparts_dump");
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

            renderMaps(getIds(), getStates());

            ImageHelper.initializeImagesSMI();

            for(int i = 0; i < inventory.size(); i++){
                if(inventory.getStack(i).getItem().getClass() != FilledMapItem.class){
                    continue;
                }

                mapIdComponent = (MapIdComponent)inventory.getStack(i).get(DataComponentTypes.MAP_ID);
                mapId = mapIdComponent.id();
                mapState = FilledMapItem.getMapState(inventory.getStack(i), MinecraftClient.getInstance().world);
                downloadMap("maparts_dump", "map"+i+".png");

                File f = new File(new File(StalpoMapartHelper.modFolder, "maparts_dump"), getFileName(i));
                String name = ImageHelper.getSMI(f);

                if(name == null){
                    StalpoMapartHelper.CHAT("Slot: " + (i + 1) + " MapId: " + mapId + " SMI: not in database");
                }else{
                    StalpoMapartHelper.CHAT("Slot: " + (i + 1) + " MapId: " + mapId + " SMI: " + name);
                }
            }
            StalpoMapartHelper.LOGCHAT("Finished getting SMIs");
        }
    }

    private static boolean downloadMap(String DirName, String FileName){

        MapRenderer.MapTexture txt = ((MapRendererInvoker)MinecraftClient.getInstance().gameRenderer.getMapRenderer()).invokeGetMapTexture(mapIdComponent, mapState);

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

    public static void lockShulkerCheck(){
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();

        for(int i = 9; i < 36; i++){
            if(inventory.getStack(i).getItem().getClass() == FilledMapItem.class){
                putShulker();
                return;
            }
        }
    }

    public static void findNotLocked(){
        if(sh != null) {
            StalpoMapartHelper.LOGCHAT("Finding not locked");
            Inventory inventory = ((InventoryExporter) sh).getInventory();

            renderMaps(getIds(), getStates());

            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).getItem().getClass() != FilledMapItem.class) {
                    continue;
                }

                mapIdComponent = (MapIdComponent)inventory.getStack(i).get(DataComponentTypes.MAP_ID);
                mapId = mapIdComponent.id();
                mapState = FilledMapItem.getMapState(inventory.getStack(i), MinecraftClient.getInstance().world);

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
            boolean found = false;
            for(int k = 0; k < 9; k++){
                if(inventory.getStack(k).getItem().getClass() == EmptyMapItem.class){
                    moveOne(k+37, 2);
                    found = true;
                    break;
                }
            }
            if(!found){
                StalpoMapartHelper.LOGCHAT("Ran out of empty maps! Turning off auto copier...");
                StalpoMapartHelper.mapCopierToggled = false;
                return;
            }
            moveOne(i+10, 1);
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
            boolean found = false;
            for(int k = 0; k < 9; k++){
                if(inventory.getStack(k).getItem() == Items.GLASS_PANE){
                    moveOne(k+30, 1);
                    found = true;
                    break;
                }
            }
            if(!found){
                StalpoMapartHelper.LOGCHAT("Ran out of glass panes! Turning off auto locker...");
                StalpoMapartHelper.mapLockerToggled = false;
                return;
            }
            moveOne(i+3, 0);
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
                StalpoMapartHelper.LOGCHAT("Ran out of xp! turning off auto namer...");
                StalpoMapartHelper.mapNamerToggled = false;
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

        ImageHelper.initializeImagesSMI();

        Inventory inventory = MinecraftClient.getInstance().player.getInventory();
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }

            String smi = "";

            mapIdComponent = (MapIdComponent)inventory.getStack(i).get(DataComponentTypes.MAP_ID);
            mapId = mapIdComponent.id();
            mapState = FilledMapItem.getMapState(inventory.getStack(i), MinecraftClient.getInstance().world);
            downloadMap("maparts_dump", "map"+i+".png");

            File f = new File(new File(StalpoMapartHelper.modFolder, "maparts_dump"), getFileName(i));
            String name = ImageHelper.getSMI(f);

            if(name == null){
                smi = "none";
            }else{
                smi = name.substring(0, name.length() - 4);
            }

            String n = inventory.getStack(i+9).getName().getString();
            if (n.equals("Map")){
                n = "";
            }

            if(MinecraftClient.getInstance().player.experienceLevel == 0){
                StalpoMapartHelper.LOGCHAT("Ran out of xp! turning off auto namer...");
                StalpoMapartHelper.mapNamerToggled = false;
                return;
            }

            moveOne(i+3, 0);

            ((AnvilScreen)MinecraftClient.getInstance().currentScreen).onRenamed(n + " " + smi);

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
        File checkdir = new File(StalpoMapartHelper.modFolder, "maparts");
        int i = 1;
        while(new File(checkdir, getFileName(i)).isFile()){
            i++;
        }
        nextMap = i;
        checkdir = new File(StalpoMapartHelper.modFolder, "maparts_smi");
        i = 1;
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
        return ("SMI_"+i+"_"+(((i - 1) / 27) + 1)+"_"+(((i - 1) % 27) + 1)+".png");
    }

    private static int getNextMap(){
        File checkdir = new File(StalpoMapartHelper.modFolder, "maparts");
        int i = nextMap + 1;
        while(new File(checkdir, getFileName(i)).isFile()){
            i++;
        }
        return i;
    }

    private static int getNextSMIMap(){
        File checkdir = new File(StalpoMapartHelper.modFolder, "maparts_smi");
        int i = nextSMIMap + 1;
        while(new File(checkdir, getSMIFileName(i)).isFile()){
            i++;
        }
        return i;
    }

    public static void renderMaps(List<MapIdComponent> ids, List<MapState> states){
        for(int i = 0; i < ids.size(); i++){
            renderMap(ids.get(i), states.get(i));
        }
    }

    public static void renderMap(MapIdComponent id, MapState state){
        try {
            final MatrixStack matrixStack = new MatrixStack();

            // Background
            matrixStack.push();
            matrixStack.pop();

            // Map (can you tell this part is skidded yet?)
            matrixStack.push();
            matrixStack.translate(3.2F, 3.2F, 401);
            matrixStack.scale(0.45F, 0.45F, 1);
            MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(matrixStack, new VertexConsumerProvider() {
                @Override
                public VertexConsumer getBuffer(RenderLayer layer) {
                    return null;
                }
            }, id, state, true, 15728880);
            matrixStack.pop();
        } catch (Exception e){
            // doesn't need to actually work lmao just get into draw part
        }
    }
}
