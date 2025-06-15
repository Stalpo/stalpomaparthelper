package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.render.MapRenderer;

@Mixin(targets = "net.minecraft.client.texture.MapTextureManager$MapTexture")
public interface MapTextureAccessor {

    @Accessor("texture")
    NativeImageBackedTexture getTexture();
}