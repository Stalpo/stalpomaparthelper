package net.stalpo.stalpomaparthelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;
import java.util.stream.StreamSupport;

public class Bundles extends MapartShulker {
    public static List<Item> bundles = List.of(
            Items.BUNDLE,
            Items.WHITE_BUNDLE,
            Items.BLACK_BUNDLE,
            Items.BLUE_BUNDLE,
            Items.BROWN_BUNDLE,
            Items.CYAN_BUNDLE,
            Items.GRAY_BUNDLE,
            Items.GREEN_BUNDLE,
            Items.LIME_BUNDLE,
            Items.MAGENTA_BUNDLE,
            Items.LIGHT_BLUE_BUNDLE,
            Items.LIGHT_GRAY_BUNDLE,
            Items.ORANGE_BUNDLE,
            Items.PINK_BUNDLE,
            Items.PURPLE_BUNDLE,
            Items.RED_BUNDLE,
            Items.YELLOW_BUNDLE);

    private static boolean bundleProcessActive = false;  // we are doing something with bundles

    public static int getBundleSlot(int firstSlot, int secondSlot, boolean backwards, boolean allowEmptyBundle) {
        // returns the number of the slot with a bundle that empty or contains only filled maps
        // backwards - from a bigger slot to a lower slot (45 -> 36)
        // allowEmptyBundle - allow to return a slot contains an empty bundle
        // returns -1 if nothing found

        if (backwards) {
            firstSlot *= -1;
            secondSlot *= -1;
        }

        int fromSlot = Math.min(firstSlot, secondSlot);
        int toSlot = Math.max(firstSlot, secondSlot);

        System.out.println("from " + fromSlot + " to " + toSlot);

        for (; fromSlot <= toSlot; fromSlot++) {
            int slot = Math.abs(fromSlot);

            ItemStack stack = sh.getSlot(slot).getStack();
            System.out.println(slot + " " + stack);

            if (!bundles.contains(stack.getItem())) continue;

            BundleContentsComponent bundleComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);

            if (bundleComponent == null) continue; // should never happen
            Fraction bundleSize = bundleComponent.getOccupancy();

            if (bundleSize.equals(Fraction.ONE)) continue; // the bundle is full

            if (bundleComponent.isEmpty() && !allowEmptyBundle) continue;

            // check if there are filled maps only (or the bundle is empty)
            System.out.println(StreamSupport.stream(bundleComponent.iterateCopy().spliterator(), false)
                    .allMatch(item -> item.getItem() == Items.FILLED_MAP));
            if (!StreamSupport.stream(bundleComponent.iterateCopy().spliterator(), false)
                    .allMatch(item -> item.getItem() == Items.FILLED_MAP)) continue;

            return slot;
        }
        return -1;
    }

    public static int canFitUntilItBursts(int bundleSlot) {
        // returns how many maps can be put in this bundle
        if (bundleSlot == -1) return 0;

        ItemStack bundle = sh.getSlot(bundleSlot).getStack();
        BundleContentsComponent bundleComponent = bundle.get(DataComponentTypes.BUNDLE_CONTENTS);

        if (bundleComponent == null) return 0; // should never happen

        Fraction available = Fraction.ONE.subtract(bundleComponent.getOccupancy());
        return 64 / available.getDenominator() * available.getNumerator();
    }

    public static int canDrainUntilDrained(int bundleSlot) {
        // returns how many maps can be pulled out of this bundle
        if (bundleSlot == -1) return 0;

        ItemStack bundle = sh.getSlot(bundleSlot).getStack();
        BundleContentsComponent bundleComponent = bundle.get(DataComponentTypes.BUNDLE_CONTENTS);

        if (bundleComponent == null) return 0; // should never happen

        return bundleComponent.size();
    }

    public static void putMapsToBundle() {
        StalpoMapartHelper.LOGCHAT("[1] putting maps into a bundle");
        if (bundleProcessActive) return;
        StalpoMapartHelper.LOGCHAT("[2] putting maps into a bundle");

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        sh = (player.currentScreenHandler == null || player.currentScreenHandler.syncId == 0)
                ? player.playerScreenHandler
                : player.currentScreenHandler;
        player.currentScreenHandler = sh;

        bundleProcessActive = true;

        int screenSize = sh.syncId == 0 ? 44 : sh.getStacks().size() - 1; // ignore offhand in the inventory
        int inventory = screenSize - 36;  // inventory starts from this slot
        int hotbar = screenSize - 9;  // hotbar starts from this slot
        int startSlot = inventory - 27 >= -1 ? 0 : inventory + 1;  // take/put from/to a shulker/chest
        int endSlot = inventory - 27 >= -1 ? inventory : hotbar;
        boolean bundleTaken = false;

        try {
            int bundleSlot = getBundleSlot(hotbar, screenSize, false, true);
            int canPutCount = canFitUntilItBursts(bundleSlot);
            StalpoMapartHelper.LOGCHAT( bundleSlot + " " + canPutCount);

            if (bundleSlot == -1) {
                bundleProcessActive = false;
                return;
            }

            for (int slot = startSlot; slot <= endSlot; slot++) {
                if (canPutCount == 0) break;

                ItemStack stack = sh.getSlot(slot).getStack();

                if (stack.getItem() != Items.FILLED_MAP) continue;

                if (!bundleTaken) {
                    pickUp(bundleSlot, 0);
                    bundleTaken = true;
                }

                pickUp(slot, 0);
                canPutCount--;
            }

            if (bundleTaken) pickUp(bundleSlot, 0);
        } catch (Exception e) {
            bundleProcessActive = false;
            throw new RuntimeException(e);
        }
        bundleProcessActive = false;
    }

    public static void pullMapsOutOfBundle() {
        if (bundleProcessActive) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        sh = (player.currentScreenHandler == null || player.currentScreenHandler.syncId == 0)
                ? player.playerScreenHandler
                : player.currentScreenHandler;
        player.currentScreenHandler = sh;

        bundleProcessActive = true;

        int screenSize = sh.syncId == 0 ? 44 : sh.getStacks().size() - 1; // ignore offhand in the inventory
        int inventory = screenSize - 36;  // inventory starts from this slot
        int hotbar = screenSize - 9;  // hotbar starts from this slot
        int startSlot = inventory - 27 >= -1 ? inventory : hotbar;  // take/put from/to a shulker/chest
        int endSlot = inventory - 27 >= -1 ? 0 : inventory + 1;

        try {
            int bundleSlot = getBundleSlot(hotbar, screenSize, true, false);
            int canTakeCount = canDrainUntilDrained(bundleSlot);
            System.out.println(bundleSlot + " " + canTakeCount);

            if (bundleSlot == 0) {
                bundleProcessActive = false;
                return;
            }

            boolean bundleTaken = false;
            for (int slot = startSlot; slot >= endSlot; slot--) {
                if (canTakeCount == 0) break;

                if (!sh.getSlot(slot).getStack().isEmpty()) continue;

                if (!bundleTaken) {
                    pickUp(bundleSlot, 0);
                    bundleTaken = true;
                }

                pickUp(slot, 1);
                canTakeCount--;
            }

            if (bundleTaken) pickUp(bundleSlot, 0);
        } catch (Exception e) {
            bundleProcessActive = false;
            throw new RuntimeException(e);
        }
        bundleProcessActive = false;
    }
}
