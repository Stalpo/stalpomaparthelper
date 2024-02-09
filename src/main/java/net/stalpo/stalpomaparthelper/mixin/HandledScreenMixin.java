package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.stalpo.stalpomaparthelper.StalpoMapartHelper;
import net.stalpo.stalpomaparthelper.MapartShulker;
import net.minecraft.client.gui.screen.Screen;
import net.stalpo.stalpomaparthelper.interfaces.SlotClicker;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements SlotClicker {

    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    protected HandledScreenMixin() { super(null); }

    @Inject(method="keyPressed", at=@At("HEAD"), cancellable = true)
    private void keyBindPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (StalpoMapartHelper.keyDownloadMaps.matchesKey(keyCode, scanCode)) {
            MapartShulker.downloadShulker(this.client);
        }else if(StalpoMapartHelper.keyFindDuplicates.matchesKey(keyCode, scanCode)){
            MapartShulker.findDuplicates(this.client);
        }else if(StalpoMapartHelper.keyGetSMI.matchesKey(keyCode, scanCode)){
            MapartShulker.getSMI(this.client);
        }else if(StalpoMapartHelper.keyFindNotLocked.matchesKey(keyCode, scanCode)){
            MapartShulker.findNotLocked(client);
        }
    }

    @Override
    public void StalpoMapartHelper$onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType){
        this.onMouseClick(slot, invSlot, button, slotActionType);
    }
}
