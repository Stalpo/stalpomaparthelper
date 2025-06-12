package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//same thing but should work
@Mixin(MapTextureManager.MapTexture.class)
public interface MapTextureAccessor {

    @Accessor("texture")
    NativeImageBackedTexture getTexture();
}

