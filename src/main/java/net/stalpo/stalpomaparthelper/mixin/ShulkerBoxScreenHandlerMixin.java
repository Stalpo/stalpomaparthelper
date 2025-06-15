package net.stalpo.stalpomaparthelper.mixin;

import net.stalpo.stalpomaparthelper.interfaces.InventoryExporter;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.screen.ShulkerBoxScreenHandler;

@Mixin(ShulkerBoxScreenHandler.class)
public class ShulkerBoxScreenHandlerMixin implements InventoryExporter {

    @Shadow @Final private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}