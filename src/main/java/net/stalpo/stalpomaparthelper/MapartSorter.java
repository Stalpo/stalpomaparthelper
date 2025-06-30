package net.stalpo.stalpomaparthelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.stalpo.stalpomaparthelper.sequence.NameSequence;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

// this is very complicated module, so I decided to make it separate to not overload MapartShulker
public class MapartSorter extends MapartShulker {
    public static final HashSet<Integer> doneMaps = new HashSet<>();

    public static NameSequence putSortSequence = new NameSequence();

    public static int currentSerial() {
        return putSortSequence.getSequenceSerialNumber(true);
    }

    public static int currentIndexInShulker() {
        return currentSerial() % 27;
    }

    public static int minSerial() {
        return currentSerial() - currentIndexInShulker();
    }

    public static int maxSerial() {
        return minSerial() + 26;
    }

    public static void sortMaps() {
        MinecraftClient mc = MinecraftClient.getInstance();
        cancelUpdatesSyncId = sh.syncId;

        int startSerial = currentSerial();

        try {
            updateIndexes();
            // trying to sort opened shulker or put something inside
            if (isOpenedShulkerFollowingSequence()) {
                if (currentSerial() - startSerial == 27) { // if the shulk is full then skip
                    cancelUpdatesSyncId = NO_SYNC_ID;
                    return;
                }

                boolean doItAgain;
                do {
                    doItAgain = false;

                    for (Map.Entry<Integer, Integer> map : getPossibleMaps().entrySet()) {
                        if (firstEmptySlotInShulker() == map.getKey() && map.getValue() >= 27) { // trying to reduce amount of clicks
                            quickMove(map.getValue());
                            putSortSequence.increment();
                        } else {
                            // this move can also change position of other map
                            swap(map.getValue(), map.getKey());
                            // my brain is melting, let's stop it and do it again until everything put/set/change
                            // im sure nobody will rewrite it ahahhaah
                            if (!sh.getSlot(map.getValue()).getStack().isEmpty()) {
                                doItAgain = true;
                                break;
                            }
                        }
                    }
                } while (mc.player.currentScreenHandler.syncId != 0 && doItAgain);
            }
            // trying to take something from this shulker
            else {
                takePossibleMaps();
            }
        } catch (NullPointerException ignored) {
        }

        updateIndexes();

        if (putSortSequence.reachedEnd()) {
            StalpoMapartHelper.LOGCHAT("§aDone sorting!§r");
        } else {
            StalpoMapartHelper.LOGCHAT("§eNext map name: §d" + putSortSequence.getCurrentMapName() + "§r");
        }
        cancelUpdatesSyncId = NO_SYNC_ID;
    }

    public static boolean isOpenedShulkerFollowingSequence() {
        // shulker has to either be empty or following the sequence
        // we also skip the maps before current index

        // if there's a missing map in between current index and map in the slot
        // like [0, 1, miss, 3, ...]

        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = sh.getSlot(slot).getStack();

            if (!stack.isEmpty()) {
                if (stack.getItem() != Items.FILLED_MAP) return false;

                if (!putSortSequence.isNameMatches(stack.getName().getString())) return false;

                int thisMapSerial = putSortSequence.getSequenceSerialNumber(false);

                if (thisMapSerial < minSerial() || thisMapSerial > maxSerial()) return false;
            }
        }

        return true; // that means we can put maps in this shulker!
    }

    public static TreeMap<Integer, Integer> getPossibleMaps() {
        // returns {indexInShulker: slot} - all maps that can be put in this shulker
        // skips have been already set
        TreeMap<Integer, Integer> map = new TreeMap<>();

        for (int slot = 0; slot < 63; slot++) {
            ItemStack stack = sh.getSlot(slot).getStack();

            if (stack.getItem() == Items.FILLED_MAP && putSortSequence.isNameMatches(stack.getName().getString())) {
                int thisSerial = putSortSequence.getSequenceSerialNumber(false);
                if (thisSerial < minSerial() || thisSerial > maxSerial()) continue;
                if (thisSerial % 27 == slot) continue;
                map.put(thisSerial % 27, slot);
                doneMaps.add(thisSerial);
            }
        }
        return map;
    }

    public static int firstEmptySlotInShulker() {
        for (int slot = 0; slot < 27; slot++) {
            if (sh.getSlot(slot).getStack().isEmpty()) return slot;
        }
        return -1;
    }

    public static void updateIndexes() {
        int thisX = putSortSequence.getCurrX();
        int thisY = putSortSequence.getCurrY();

        putSortSequence.carryOverSequence = true;

        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = sh.getSlot(slot).getStack();
            if (stack.isEmpty()) {
                // this shulk is a source shulk, not the shulk we put the maps into
                putSortSequence.setCurrX(thisX);
                putSortSequence.setCurrY(thisY);
                return;
            };

            if (stack.getItem() == Items.FILLED_MAP && putSortSequence.isFollowingSequence(stack.getName().getString())) {
                int thisSerial = putSortSequence.getSequenceSerialNumber(false);
                if (thisSerial % 27 != slot) {
                    // this shulk is a source shulk, not the shulk we put the maps into
                    putSortSequence.setCurrX(thisX);
                    putSortSequence.setCurrY(thisY);
                    return;
                }
                ;
                putSortSequence.increment();
            } else {
                // this shulk is a source shulk, not the shulk we put the maps into
                putSortSequence.setCurrX(thisX);
                putSortSequence.setCurrY(thisY);
                return;
            }
        }
    }

    public static void takePossibleMaps() {
        StalpoMapartHelper.LOG("takePossibleMaps");
        // you don't know how much time I spent calculating it
        // to make it ACTUALLY follow the sequence
        // excluding the maps that have been set ahead of the sequence
        // (maybe the shulker was resorted or desynced, whatever)
        HashSet<Integer> availableMapsToTake = new HashSet<>();
        HashSet<Integer> mapsInInventory = new HashSet<>();
        int emptySlots = 0;
        for (int slot = 27; slot < 63; slot++) {
            ItemStack stack = sh.getSlot(slot).getStack();
            if (stack.isEmpty()) emptySlots++;
            else if (stack.getItem() == Items.FILLED_MAP && putSortSequence.isNameMatches(stack.getName().getString())) {
                mapsInInventory.add(putSortSequence.getSequenceSerialNumber(false));
            }
        }

        if (emptySlots == 0) return;

        int needThis = putSortSequence.getSequenceSerialNumber(true);
        for (int counter = 0; counter < emptySlots; ) {
            if (needThis > putSortSequence.getMaxSerialNumber()) break;

            if (!mapsInInventory.contains(needThis)) {
                if (!doneMaps.contains(needThis)) counter++;
                availableMapsToTake.add(needThis);
            }
            needThis++;
        }

        for (int slot = 0; slot < 27; slot++) {
            ItemStack stack = sh.getSlot(slot).getStack();

            if (stack.getItem() == Items.FILLED_MAP && putSortSequence.isNameMatches(stack.getName().getString())) {
                int thisSerial = putSortSequence.getSequenceSerialNumber(false);
                if (availableMapsToTake.contains(thisSerial)) quickMove(slot);
            }
        }
    }
}
