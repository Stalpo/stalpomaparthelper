package net.stalpo.stalpomaparthelper;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.stalpo.stalpomaparthelper.interfaces.InventoryExporter;
import net.stalpo.stalpomaparthelper.interfaces.SlotClicker;
import net.stalpo.stalpomaparthelper.mixin.MapRendererAccessor;
import net.stalpo.stalpomaparthelper.mixin.MapTextureAccessor;
import net.stalpo.stalpomaparthelper.mixin.MapTextureManagerAccessor;
import net.stalpo.stalpomaparthelper.sequence.NameSequence;
import org.apache.commons.lang3.math.Fraction;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MapartShulker {
    public static final int NO_SYNC_ID = -10;

    public static ScreenHandler sh;
    public static Map<Integer, Runnable> callSoon = new HashMap<>(); // syncId: callable function
    private static MapState mapState;
    private static int mapId; // Stalpo? Delete this maybe??
    private static MapIdComponent mapIdComponent;
    private static int nextMap;

    private static List<MapState> states;

    // rename maparts
    public static NameSequence sequence = new NameSequence();
    public static int lastMapId = -1;
    public static boolean anvilBroken = false;

    public static SoundEvent AnvilBrokenSound = SoundEvents.ENTITY_VILLAGER_NO;
    public static SoundEvent RenameFinishedSound = SoundEvents.BLOCK_AMETHYST_BLOCK_STEP;
    public static Pattern namePattern = null;
    public static HashMap<Integer, Integer> mapsSequence;

    public static int delay = 65;  // 26.05.2025 - haosemaster is mad

    // Grim sends slot updates, they break inventory syncing
    // 0 = inventory
    // -1 and -2 also inventory in different states (server-side)
    // each opened screen (shulker, chest, etc) has its own sync id
    public static int cancelUpdatesSyncId = NO_SYNC_ID;


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

    public static List<MapState> getStates() {
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

                downloadMap("maparts", getFileName(nextMap));
                nextMap = getNextMap();
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

    private static boolean downloadMap(String DirName, String FileName){

        MapRenderer mapRenderer = MinecraftClient.getInstance().getMapRenderer();

        MapTextureManager mapTextureManager = ((MapRendererAccessor) mapRenderer).getTextureManager();

        Int2ObjectMap<?> texturesByMapId = ((MapTextureManagerAccessor) mapTextureManager).getTexturesByMapId();
        Object mapTextureObj = texturesByMapId.get(mapIdComponent.id());

        if (mapTextureObj == null) {
            StalpoMapartHelper.ERROR("Map texture is null for given mapIdComponent and mapState");
            return false;
        }

        NativeImageBackedTexture txt = ((MapTextureAccessor) mapTextureObj).getTexture();

        File screensDir = new File(StalpoMapartHelper.modFolder, DirName);
        if(!screensDir.exists() && !screensDir.mkdir()) {
            StalpoMapartHelper.ERROR("Could not create directory " + screensDir.getAbsolutePath() + " cannot continue!");
            return false;
        }

        File map = new File(screensDir, FileName);

        try {

            NativeImage image = txt.getImage();
            if (image == null)
            {
                StalpoMapartHelper.ERROR("Map image is null — cannot write to file.");
                return false;
            }

            image.writeTo(map);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        ImageHelper.addImage(map);

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

    public static void putTakeCheck() {
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();

        for (int i = 9; i < 36; i++) {
            if (inventory.getStack(i).getItem().getClass() == FilledMapItem.class) {
                putShulker();
                return;
            }
        }
        if (StalpoMapartHelper.mapNamerToggled) renameTakeShulker();
        else takeShulker();
    }

    private static void takeShulker() {
        StalpoMapartHelper.LOGCHAT("Taking shulker");
        Inventory inventory = ((InventoryExporter) sh).getInventory();

        cancelUpdatesSyncId = sh.syncId;
        for (int i = 0; i < inventory.size(); i++) {
            if (sh.getSlot(i).getStack().getItem() != Items.FILLED_MAP) {
                continue;
            }
            moveOne(i, i + 27);
        }
        cancelUpdatesSyncId = NO_SYNC_ID;
    }

    private static void putShulker(){
        StalpoMapartHelper.LOGCHAT("Putting shulker");
        Inventory inventory = MinecraftClient.getInstance().player.getInventory();

        cancelUpdatesSyncId = sh.syncId;
        for(int i = 0; i < 27; i++){
            if(inventory.getStack(i+9).getItem().getClass() != FilledMapItem.class){
                continue;
            }
            moveOne(i+27, i);
        }
        cancelUpdatesSyncId = NO_SYNC_ID;
    }


    private static void renameTakeShulker() {
        // this function takes maps that don't match the sequence
        // or doesn't take at all but update the sequence
        StalpoMapartHelper.LOGCHAT("Taking shulker for renaming");

        sequence.carryOverSequence = true;  // isFirstMap (that doesn't match the sequence)

        cancelUpdatesSyncId = sh.syncId;
        for (int i = 0; i < 27; i++) {
            if (sequence.reachedEnd()) {
                StalpoMapartHelper.LOGCHAT("§2Nothing to take - finished naming shulk!");
                break;
            }

            if (sh.getSlot(i).getStack().getItem() != Items.FILLED_MAP) {
                continue;
            }

            if (sequence.isFollowingSequence(sh.getSlot(i).getStack().getName().getString())) {
                if (sequence.carryOverSequence) sequence.carryOverSequence = false;
                sequence.increment(); // next potential map
                continue;
            }

            moveStack(i, i + 27);
        }
        cancelUpdatesSyncId = NO_SYNC_ID;
    }


    public static void copyMaps() {
        StalpoMapartHelper.LOGCHAT("Copying maps");
        MinecraftClient mc = MinecraftClient.getInstance();

        int filledMapsCount = 0;
        int emptyMapsCount = 0;

        Queue<List<Integer>> emptyMaps = new LinkedList<>(); // [slot, count]

        cancelUpdatesSyncId = sh.syncId;

        for (int slot = 10; slot < 46; slot++) {
            ItemStack stack = sh.getSlot(slot).getStack();
            if (stack.getItem() == Items.MAP) {
                emptyMapsCount += stack.getCount();
                emptyMaps.add(Arrays.asList(slot, stack.getCount()));
            }
            // 38-45 = hotbar
            else if (stack.getItem() == Items.FILLED_MAP && slot < 37) {
                filledMapsCount++;
            }
        }

        if (emptyMapsCount < filledMapsCount) {
            StalpoMapartHelper.LOGCHAT("Ran out of empty maps! Turning off auto copier...");
            StalpoMapartHelper.mapCopierToggled = false;
            return;
        }

        List<Integer> emptyMapSlot = emptyMaps.poll();
        int emptyMapsStackCount = emptyMapSlot.get(1);
        quickMove(emptyMapSlot.get(0));

        for (int slot = 10; slot < 37; slot++) {
            ItemStack filledMap = sh.getSlot(slot).getStack();
            int stackCount = filledMap.getCount();

            if (filledMap.getItem() == Items.FILLED_MAP) {
                if (emptyMapsStackCount == 0) {
                    emptyMapSlot = emptyMaps.poll();
                    emptyMapsStackCount = emptyMapSlot.get(1);
                    quickMove(emptyMapSlot.get(0));
                }

                // if we are ignoring server-side updates,
                // then we miss the result item.
                // recreating it on the client!

                // yep, we can just send click packet to grab the item
                // but we will face client-side inventory desync

                ItemStack resultStack = filledMap.copy();
                resultStack.setCount(2);

                // reduce amount of click packets
                if (stackCount == 1) {
                    quickMove(slot);
                    sh.setStackInSlot(0, sh.getRevision(), resultStack);
                    moveStack(0, slot);
                } else {
                    moveOne(slot, 2);
                    sh.setStackInSlot(0, sh.getRevision(), resultStack);
                    quickMove(0);
                }

                // default
//                moveOne(slot, 2);
//                sh.setStackInSlot(0, sh.getRevision(), resultStack);
//                moveStack(0, slot);

                emptyMapsStackCount--;
            }
        }

        // previous inventory syncing (it's possible to click any crafting slot, even if it's empty)
        // ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, 1, 0, SlotActionType.QUICK_MOVE);

        // i think we can for now allow server-side updates
        // because we've done our client-side work
        // if we enable them, then we will avoid ping-related issues
        // because the player will see how's it going
        // I don't know exactly, needs more tests
        // This statement applies to taking and putting the shulkers too
        // because now we ignore all inv packets with current syncId
        // will see if it's good enough!
        cancelUpdatesSyncId = NO_SYNC_ID;

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

    public static int findByMapId(PlayerInventory inventory, int mapId) {
        int timer = 0;
        do {
            for (int slot = 0; slot < 45; slot++) {
                if (inventory.getStack(slot).getItem().getClass() != FilledMapItem.class) {
                    StalpoMapartHelper.LOG("[debug] slot: " + slot + "; item: " + inventory.getStack(slot).getItem().getName().getString());
                    continue;
                }
                StalpoMapartHelper.LOG("[debug] slot: " + slot + "; map id: " + inventory.getStack(slot).get(DataComponentTypes.MAP_ID).id()
                        + " required: " + mapId + "; result: " + (inventory.getStack(slot).get(DataComponentTypes.MAP_ID).id() == mapId));
                if (inventory.getStack(slot).get(DataComponentTypes.MAP_ID).id() == mapId) {
                    return slot;
                }
            }
            timer++;

            try { TimeUnit.MILLISECONDS.sleep(50); } catch (InterruptedException ignored) { }
            StalpoMapartHelper.LOG("Tryng to find a map. Times: " + timer);

            // INVENTORY CAN BE DESYNCED OR NOT UPDATED YET WE ARE WAITING FOR SYNCHRONIZE DON'T ASK ME PLEASE IT WORKS AFTER ALL
            // TODO: trust the server :)
        } while (timer < 50);

        return -1;
    }

    public static void sortInventoryAfterRename() {
        // This function checks for the right map sequence after an anvil breaks. Please don't use it somewhere else.
        // Things we know for now (no need to check them):
        // 1. An anvil has broken, so our gui is closed
        // 2. We DO have maps provided in "MapartShulker.mapsSequence" in the inventory

        StalpoMapartHelper.LOG("sorting inventory");
        do { try { TimeUnit.MILLISECONDS.sleep(5); } catch (InterruptedException ignored) { }
        } while (MinecraftClient.getInstance().player.currentScreenHandler instanceof AnvilScreenHandler);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.player.currentScreenHandler = MinecraftClient.getInstance().player.playerScreenHandler;
        sh = MinecraftClient.getInstance().player.currentScreenHandler;
        int syncId = MinecraftClient.getInstance().player.playerScreenHandler.syncId;

        // after renaming, a map can be inside crafting slots
        // now i check maps if they aren't in their slot. This approach can easily deal with desyncs
        for (int slot = 0; slot < 45; slot++) {
            if (sh.getSlot(slot).getStack().getItem().getClass() != FilledMapItem.class) continue;

            int currentMapId = sh.getSlot(slot).getStack().get(DataComponentTypes.MAP_ID).id();
            if (!mapsSequence.containsKey(currentMapId)) continue;

            if (slot != mapsSequence.get(currentMapId)) {
                mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
                sleep();
                mc.interactionManager.clickSlot(syncId, mapsSequence.get(currentMapId), 0, SlotActionType.PICKUP, mc.player);
                sleep();

                // THEN we synchronize previous and next slots
                if (slot - 1 > 8) {
                    mc.interactionManager.clickSlot(syncId, slot - 1, 0, SlotActionType.PICKUP, mc.player);
                    sleep();
                    mc.interactionManager.clickSlot(syncId, slot - 1, 0, SlotActionType.PICKUP, mc.player);
                    sleep();
                }
                if (slot + 1 < 45) {
                    mc.interactionManager.clickSlot(syncId, slot + 1, 0, SlotActionType.PICKUP, mc.player);
                    sleep();
                    mc.interactionManager.clickSlot(syncId, slot + 1, 0, SlotActionType.PICKUP, mc.player);
                    sleep();
                }
            }
        }
    }

    public static void nameMaps() {
        anvilBroken = false;
        lastMapId = -1;
        // this sequence should be in "net.stalpo.stalpomaparthelper.sequence" folder as it is like mapId: mapSlot.
        // We probably will use it in the future more frequently.
        mapsSequence = new HashMap<>();
        cancelUpdatesSyncId = sh.syncId;

        MinecraftClient mc = MinecraftClient.getInstance();
        int currentExpLevel = mc.player.experienceLevel;

        sequence.carryOverSequence = true;

        // searching for last map in inventory. Check AnvilSoundSuppressMixin
        for (int i = 30; i > 2; i--) {
            if (sh.getSlot(i).getStack().getItem().getClass() == FilledMapItem.class) {
                lastMapId = sh.getSlot(i).getStack().get(DataComponentTypes.MAP_ID).id();
                break;
            }
        }

        // 0-2 - anvil slots
        // 3-29 - inventory
        // 30-38 - hotbar
        for (int i = 3; i < 30; i++) {
            if (anvilBroken) return;
            if (sequence.reachedEnd()) break;

            if (sh.getSlot(i).getStack().getItem().getClass() != FilledMapItem.class) continue;

            mapsSequence.put(sh.getSlot(i).getStack().get(DataComponentTypes.MAP_ID).id(), i + 6); // the slot index should be as in playerScreenHandler

            // skip already renamed maps
            if (sequence.isFollowingSequence(sh.getSlot(i).getStack().getName().getString())) {
                if (sequence.carryOverSequence) {
                    StalpoMapartHelper.LOGCHAT("Previous indexes were updated.\nx: " + sequence.getCurrX() + ",  y: " + sequence.getCurrY());
                    sequence.carryOverSequence = false;
                }
                sequence.increment();
                continue;
            }

            // I decided to calculate player exp level locally because server sometimes tells the player has 1 exp, but they don't
            if (currentExpLevel == 0) {
                StalpoMapartHelper.LOGCHAT("§6Ran out of xp!");
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
                return;
            }

            String newName = sequence.getCurrentMapName();
            quickMove(i);

            ((AnvilScreenHandler) sh).setNewItemName(newName);

            mc.player.networkHandler.sendPacket(new RenameItemC2SPacket(newName));

            moveStack(2, i);
            // fix client-side desync (for some reason this slot doesn't update as fast as we need)
            sh.setStackInSlot(0, sh.getRevision(), ItemStack.EMPTY);

            currentExpLevel--;
            sequence.increment();
        }

        cancelUpdatesSyncId = NO_SYNC_ID;

        // here in 99% cases each map has been renamed and the anvil hasn't broken
        // so, we wait some time (ping-related thing. It's better to wait for a specific revision)
        try { TimeUnit.MILLISECONDS.sleep(150); } catch (InterruptedException ignored) { }
        if (anvilBroken) return;

        // I want to play another sound if ALL maps have been renamed
        // we can face a broken anvil on the last renamed map, so we are waiting for anvil gui to close
        // if it closed, then check AnvilGuiClosedMixin
        // if it didn't close, then anvil didn't break, so we can play a sound
        mc.getSoundManager().play(PositionedSoundInstance.master(RenameFinishedSound, 1.0F, 2.0F));
        StalpoMapartHelper.LOGCHAT("§2Finished naming shulk!");
    }
    
    public static void sleep() {
        try { TimeUnit.MILLISECONDS.sleep(delay); } catch (InterruptedException ignored) { }
    }

    synchronized protected static void pickUp(int slot, int button) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // don't click if the player got kicked
        if (mc.player == null) return;

        // prevent clicks if gui closed
        if (mc.player.currentScreenHandler == null) return;

        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, slot, button, SlotActionType.PICKUP);
        sleep();
    }

    protected static void moveOne(int from, int to) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // don't click if the player got kicked
        if (mc.player == null) return;

        // prevent clicks if gui closed
        if (mc.player.currentScreenHandler == null) return;

        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
        sleep();
        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, to, 1, SlotActionType.PICKUP);
        sleep();
        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
        sleep();
    }


    protected static void swap(int from, int to) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // don't click if the player got kicked
        if (mc.player == null) return;

        // prevent clicks if gui closed
        if (mc.player.currentScreenHandler == null) return;
        boolean destinationIsEmpty = mc.player.currentScreenHandler.getSlot(to).getStack().isEmpty();

        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
        sleep();
        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, to, 0, SlotActionType.PICKUP);
        sleep();

        if (!destinationIsEmpty) { // reduce amount of packets if possible!
            ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
            sleep();
        }
    }

    protected static void moveStack(int from, int to) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // don't click if the player got kicked
        if (mc.player == null) return;

        // prevent clicks if gui closed
        if (mc.player.currentScreenHandler.syncId == mc.player.playerScreenHandler.syncId) return;

        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, from, 0, SlotActionType.PICKUP);
        sleep();

        ((SlotClicker)MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, to, 0, SlotActionType.PICKUP);
        sleep();

    }
    protected static void quickMove(int slot) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // don't click if the player got kicked
        if (mc.player == null) return;

        // prevent clicks if gui closed
        if (mc.player.currentScreenHandler.syncId == mc.player.playerScreenHandler.syncId) return;

        ((SlotClicker) MinecraftClient.getInstance().currentScreen).StalpoMapartHelper$onMouseClick(null, slot, 0, SlotActionType.QUICK_MOVE);
        sleep();
    }

    public static void setNextMap(){
        File checkdir = new File(StalpoMapartHelper.modFolder, "maparts");
        int i = 1;
        while(new File(checkdir, getFileName(i)).isFile()){
            i++;
        }
        nextMap = i;
        StalpoMapartHelper.LOG("nextMap: " + nextMap);
    }

    private static String getFileName(int i){
        return ("map"+i+".png");
    }

    private static int getNextMap(){
        File checkdir = new File(StalpoMapartHelper.modFolder, "maparts");
        int i = nextMap + 1;
        while(new File(checkdir, getFileName(i)).isFile()){
            i++;
        }
        return i;
    }

    public static void renderMaps(List<MapIdComponent> ids, List<MapState> states){
        for(int i = 0; i < ids.size(); i++){
            MapRenderState renderState = new MapRenderState();
            renderMap(true, renderState);
        }
    }

    public static void renderMap(Boolean showDecorations, MapRenderState renderState){
        try {
            final MatrixStack matrixStack = new MatrixStack();

            // Background
            matrixStack.push();
            matrixStack.pop();

            // Map (can you tell this part is skidded yet?)
            matrixStack.push();
            matrixStack.translate(3.2F, 3.2F, 401);
            matrixStack.scale(0.45F, 0.45F, 1);

            // updated this, gamerender shit got moved not just in the client class
            MinecraftClient.getInstance().getMapRenderer().draw(
                    renderState,
                    matrixStack,
                    MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
                    showDecorations,
                    15728880
            );

            matrixStack.pop();
        } catch (Exception e){
            // doesn't need to actually work lmao just get into draw part
        }
    }
}
