package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Util;
import net.stalpo.stalpomaparthelper.MapartShulker;
import net.stalpo.stalpomaparthelper.StalpoMapartHelper;
import net.stalpo.stalpomaparthelper.interfaces.FakeMapRenderer;
import net.stalpo.stalpomaparthelper.interfaces.SlotClicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements SlotClicker, FakeMapRenderer {

    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    protected HandledScreenMixin() { super(null); }

    @Inject(method="keyPressed", at=@At("HEAD"))
    private void keyBindPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (StalpoMapartHelper.keyDownloadMaps.matchesKey(keyCode, scanCode)) {
            Util.getIoWorkerExecutor().execute(MapartShulker::downloadShulker);
        } else if (StalpoMapartHelper.keyFindDuplicates.matchesKey(keyCode, scanCode)) {
            Util.getIoWorkerExecutor().execute(MapartShulker::findDuplicates);
        } else if (StalpoMapartHelper.keyFindNotLocked.matchesKey(keyCode, scanCode)) {
            Util.getIoWorkerExecutor().execute(MapartShulker::findNotLocked);
        } else if (StalpoMapartHelper.keyPutInTheBundle.matchesKey(keyCode, scanCode)) {
            Util.getIoWorkerExecutor().execute(MapartShulker::putMapsToBundle);
        } else if (StalpoMapartHelper.keyPullOutOfTheBundle.matchesKey(keyCode, scanCode)) {
            Util.getIoWorkerExecutor().execute(MapartShulker::pullMapsOutOfBundle);
        }
    }

    @Override
    public void StalpoMapartHelper$onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType){
        this.onMouseClick(slot, invSlot, button, slotActionType);
    }
}
