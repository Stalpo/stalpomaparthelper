package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.Util;
import net.stalpo.stalpomaparthelper.MapartShulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// this mixin blocks inventory sync packets from the server
// inventory state on the client perfectly matches real state
// so it doesn't need to be force-synced
//
// Grim sends updates in 5 ticks after any action
// we should block them all or handle the very last update only
// otherwise some slots can be desynced
// also probably ping-related issue too...
//
// I suggest to implement something better than this
// It's good enough for copying maps I guess...
// Probably it's better to use events instead of mixins


@Mixin(ClientPlayNetworkHandler.class)
public class SlotUpdateMixin {
    @Inject(method = "onInventory", at = @At("HEAD"), cancellable = true)
    private void onSlotUpdate(InventoryS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.currentScreen == null) return;
        if (mc.player.currentScreenHandler.syncId != packet.getSyncId()) return;

        if (MapartShulker.cancelUpdatesSyncId == packet.getSyncId()) {
            ci.cancel();
        } else if (packet.getRevision() == 1 && MapartShulker.callSoon.containsKey(packet.getSyncId())) {
            mc.player.currentScreenHandler.updateSlotStacks(1, packet.getContents(), packet.getCursorStack());

            Util.getIoWorkerExecutor().execute(MapartShulker.callSoon.get(packet.getSyncId()));
            MapartShulker.callSoon.remove(packet.getSyncId());

            ci.cancel();
        }
    }
// other methods related to the inventory and slots updates
// probably they will be helpful on other servers without grim

//    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"))
//    private void onSetSlot(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
//        System.out.println("[SetSlot] Window: " + packet.getSyncId()
//                + " Slot: " + packet.getSlot()
//                + " Stack: " + packet.getStack());
//    }
//
//    @Inject(method = "onScreenHandlerPropertyUpdate", at = @At("HEAD"))
//    private void onProperty(ScreenHandlerPropertyUpdateS2CPacket packet, CallbackInfo ci) {
//        System.out.println("[PropertyUpdate] Window: " + packet.getSyncId()
//                + " Property: " + packet.getPropertyId()
//                + " Value: " + packet.getValue());
//    }
//
//    @Inject(method = "onInventory", at = @At("HEAD"))
//    private void onContentUpdate(InventoryS2CPacket packet, CallbackInfo ci) {
//        System.out.println("[FullContentUpdate] Window: " + packet.getSyncId());
//        var items = packet.getContents();
//        for (int i = 0; i < items.size(); i++) {
//            System.out.println("  Slot " + i + ": " + items.get(i));
//        }
//    }
}
