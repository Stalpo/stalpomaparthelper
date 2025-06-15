package net.stalpo.stalpomaparthelper.mixin;


import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.MapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapRenderer.class)
public interface MapRendererAccessor {
    @Accessor("textureManager")
    MapTextureManager getTextureManager();
}
