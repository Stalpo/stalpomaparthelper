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

import java.util.regex.Matcher;

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
        Util.getIoWorkerExecutor().execute(MapartShulker::sortInventoryAfterRename);

        // if THE LAST map has been renamed, then play MapartShulker.RenameFinishedSound

        if (MapartShulker.lastMapId == -1) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
            StalpoMapartHelper.LOGCHAT("§6Anvil has broken!");
            return;
        }

        int lastMapSlot = MapartShulker.findByMapId(MinecraftClient.getInstance().player.getInventory(), MapartShulker.lastMapId);
        // how is it possible?? Well, I just left it here!
        if (lastMapSlot == -1) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
            StalpoMapartHelper.LOGCHAT("§6Anvil has broken!");
            return;
        }

        Matcher checkName = MapartShulker.namePattern.matcher(MinecraftClient.getInstance().player.getInventory().getStack(lastMapSlot).getName().getString());

        if (checkName.matches()) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.RenameFinishedSound, 1.0F, 2.0F));
            return;
        }

        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(MapartShulker.AnvilBrokenSound, 1.0F));
        StalpoMapartHelper.LOGCHAT("§6Anvil has broken!");
    }

}
