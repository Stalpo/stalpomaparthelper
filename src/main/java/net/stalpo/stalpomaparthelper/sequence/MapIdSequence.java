package net.stalpo.stalpomaparthelper.sequence;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.List;

// save maps as they were in the shulker/inventory
public class MapIdSequence {
    final HashMap<Integer, Integer> maps = new HashMap<>(27); // slot: mapId

    public MapIdSequence() {
    }


    public MapIdSequence(int offset, List<ItemStack> container) {
        for (int counter = offset; counter < Math.min(offset + 27, container.size()); counter++) {
            ItemStack stack = container.get(counter);
            if (stack.getItem() == Items.FILLED_MAP) {
                maps.put(counter - offset, stack.get(DataComponentTypes.MAP_ID).id());
            }
        }
    }

    public int getLastMapid() {
        if (maps.isEmpty()) return -1;
        for (int key = 26; key >= 0; key--) {
            if (maps.containsKey(key)) return maps.get(key);
        }
        return -1;
    }

    public boolean containsAllMaps(List<ItemStack> container) {
        List<Integer> mapIdsInContainer = container
                .stream()
                .filter(itemStack -> itemStack.getItem() == Items.FILLED_MAP)
                .map(itemStack -> itemStack.get(DataComponentTypes.MAP_ID).id())
                .sorted()
                .toList();
        List<Integer> mapIdsSaved = maps.values().stream().sorted().toList();
        return mapIdsSaved.equals(mapIdsInContainer);
    }

    public int getSlotByMapId(int mapId) {
        for (int key : maps.keySet()) {
            if (maps.get(key) == mapId) return key;
        }
        return -1;
    }
}
