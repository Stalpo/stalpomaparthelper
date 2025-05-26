package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.stalpo.stalpomaparthelper.MapartShulker;
import net.stalpo.stalpomaparthelper.StalpoMapartHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreens.class)
public class GuiContainerMixin {

    @Inject(method="open", at=@At("HEAD"), cancellable = true)
    private static void checkChestScreen(ScreenHandlerType type, MinecraftClient client, int syncId, Text component, CallbackInfo ci) {
        StalpoMapartHelper.LOG("Trying to open container: "+type+" with name "+component.getString());
        assert client.player != null;
        if (type == ScreenHandlerType.SHULKER_BOX) {
            ShulkerBoxScreenHandler container = ScreenHandlerType.SHULKER_BOX.create(syncId, client.player.getInventory());
            client.player.currentScreenHandler = container;
            MapartShulker.sh = container;
            ShulkerBoxScreen screen = new ShulkerBoxScreen(container, client.player.getInventory(), component);
            client.setScreen(screen);

            if(StalpoMapartHelper.mapCopierToggled || StalpoMapartHelper.mapNamerToggled){
                MapartShulker.callSoon.put(syncId, MapartShulker::putTakeCheck);
            }else if(StalpoMapartHelper.mapLockerToggled){
                MapartShulker.callSoon.put(syncId, MapartShulker::lockShulkerCheck);
            }
            ci.cancel();
        } else if(type == ScreenHandlerType.CRAFTING){
            CraftingScreenHandler container = ScreenHandlerType.CRAFTING.create(syncId, client.player.getInventory());
            client.player.currentScreenHandler = container;
            MapartShulker.sh = container;
            CraftingScreen screen = new CraftingScreen(container, client.player.getInventory(), component);
            client.setScreen(screen);

            if(StalpoMapartHelper.mapCopierToggled){
                MapartShulker.callSoon.put(syncId, MapartShulker::copyMaps);
            }
            ci.cancel();
        } else if(type == ScreenHandlerType.CARTOGRAPHY_TABLE){
            CartographyTableScreenHandler container = ScreenHandlerType.CARTOGRAPHY_TABLE.create(syncId, client.player.getInventory());
            client.player.currentScreenHandler = container;
            MapartShulker.sh = container;
            CartographyTableScreen screen = new CartographyTableScreen(container, client.player.getInventory(), component);
            client.setScreen(screen);

            if(StalpoMapartHelper.mapLockerToggled){
                MapartShulker.callSoon.put(syncId, MapartShulker::lockMaps);
            }
            ci.cancel();
        } else if(type == ScreenHandlerType.ANVIL){
            AnvilScreenHandler container = ScreenHandlerType.ANVIL.create(syncId, client.player.getInventory());
            client.player.currentScreenHandler = container;
            MapartShulker.sh = container;
            AnvilScreen screen = new AnvilScreen(container, client.player.getInventory(), component);
            client.setScreen(screen);

            if(StalpoMapartHelper.mapNamerToggled){
                MapartShulker.callSoon.put(syncId, MapartShulker::nameMaps);
            }
            ci.cancel();
        }
    }
}
