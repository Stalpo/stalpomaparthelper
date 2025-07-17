package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.component.type.MapIdComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MapTextureManager.class)
public class MapIdFixMixin {
    @ModifyVariable(method = "getTextureId", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private MapIdComponent fixMapComponentIfNull(MapIdComponent mapIdComponent) {
        if (mapIdComponent == null) return new MapIdComponent(-1);
        return mapIdComponent;
    }
}
