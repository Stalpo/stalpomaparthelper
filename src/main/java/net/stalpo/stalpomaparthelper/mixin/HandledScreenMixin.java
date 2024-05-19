package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Util;
import net.stalpo.stalpomaparthelper.interfaces.FakeMapRenderer;
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

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements SlotClicker, FakeMapRenderer {

    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    protected HandledScreenMixin() { super(null); }

    @Inject(method="keyPressed", at=@At("HEAD"))
    private void keyBindPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (StalpoMapartHelper.keyDownloadMaps.matchesKey(keyCode, scanCode)) {
            Util.getIoWorkerExecutor().execute(MapartShulker::downloadShulker);
        }else if(StalpoMapartHelper.keyFindDuplicates.matchesKey(keyCode, scanCode)){
            Util.getIoWorkerExecutor().execute(MapartShulker::findDuplicates);
        }//else if(StalpoMapartHelper.keyGetSMI.matchesKey(keyCode, scanCode)){
        //    Util.getIoWorkerExecutor().execute(MapartShulker::getSMI);
        //}else if(StalpoMapartHelper.keyFindNotLocked.matchesKey(keyCode, scanCode)){
        //    Util.getIoWorkerExecutor().execute(MapartShulker::findNotLocked);
        //}
    }

    @Override
    public void stalpomaparthelper$renderMaps(List<Integer> ids, List<MapState> states){
        for(int i = 0; i < ids.size(); i++){
            stalpomaparthelper$renderMap(ids.get(i), states.get(i));
        }
    }

    @Override
    public void stalpomaparthelper$renderMap(int id, MapState state){
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

    @Override
    public void StalpoMapartHelper$onMouseClick(Slot slot, int invSlot, int button, SlotActionType slotActionType){
        this.onMouseClick(slot, invSlot, button, slotActionType);
    }
}
