package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.stalpo.stalpomaparthelper.MapartShulker;
import net.stalpo.stalpomaparthelper.StalpoMapartHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.TimeUnit;

@Mixin(AbstractSoundInstance.class)
public class AnvilSoundSuppressMixin {
    @Final
    @Shadow
    protected Identifier id;

    @Inject(method = "getVolume()F", at = @At("HEAD"), cancellable = true)
    private void getVolume(CallbackInfoReturnable<Float> callback) {
        // remove annoying anvil use sound
        if (StalpoMapartHelper.mapNamerToggled & MinecraftClient.getInstance().currentScreen instanceof AnvilScreen) {
            if (id.toString().equals("minecraft:block.anvil.use")) {
                callback.setReturnValue(0.0F);
                callback.cancel();
            }

            // if the anvil has broken, then check the sequence of names, search for map and put the map back to its slot
            else if (id.toString().equals("minecraft:block.anvil.destroy")) {
                MapartShulker.anvilBroken = true;
                callback.setReturnValue(0.0F);
                callback.cancel();

                Util.getIoWorkerExecutor().execute(this::executeAfterAnvilBreak);
            }
        }
    }

    @Unique
    private void executeAfterAnvilBreak() {
        int thisRevision = MapartShulker.syncInventory(0);
        // waiting for the revision
        int somtehingWentWrong = 1000;
        int timeout = 10;
        while (MinecraftClient.getInstance().player.playerScreenHandler.getRevision() < thisRevision + 1) {
            try {TimeUnit.MILLISECONDS.sleep(timeout);} catch (InterruptedException ignored) {}
            somtehingWentWrong -= timeout;
            if (somtehingWentWrong <= 0) break;
        }
        Util.getIoWorkerExecutor().execute(MapartShulker::sortInventoryAfterRename);

        // if THE LAST map has been renamed, then play MapartShulker.RenameFinishedSound

        if (MapartShulker.mapsOrder.getLastMapid() == -1) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
            StalpoMapartHelper.LOGCHAT("§6Anvil has broken!");
            return;
        }

        int lastMapSlot = MapartShulker.findByMapId(MinecraftClient.getInstance().player.getInventory(), MapartShulker.mapsOrder.getLastMapid());

        // how is it possible??
        if (lastMapSlot == -1) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
            StalpoMapartHelper.LOGCHAT("§6Anvil has broken!");
        }

        if (MapartShulker.sequence.isFollowingSequence(MinecraftClient.getInstance().player.getInventory().getStack(lastMapSlot).getName().getString())) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.RenameFinishedSound, 1.0F, 2.0F));
            return;
        }

        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
        StalpoMapartHelper.LOGCHAT("§6Anvil has broken!");
    }
}
