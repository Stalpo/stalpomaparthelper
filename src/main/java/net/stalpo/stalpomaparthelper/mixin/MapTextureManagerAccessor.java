package net.stalpo.stalpomaparthelper.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.texture.MapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapTextureManager.class)
public interface MapTextureManagerAccessor {
    @Accessor("texturesByMapId")
    Int2ObjectMap<?> getTexturesByMapId();
}
