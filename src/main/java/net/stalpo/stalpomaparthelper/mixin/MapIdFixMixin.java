package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.component.type.MapIdComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MapTextureManager.class)
public class MapIdFixMixin {
    @Redirect(method = "getMapTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/MapIdComponent;id()I"))
    private int redirectMapId(MapIdComponent mapId) {
        return mapId != null ? mapId.id() : -1;
    }
}
