package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.texture.MapTextureManager;

import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MapTextureManager.class)
public interface MapRendererInvoker {
//for some fucking reason .MapTexture does not want to be recognized by the ide
//because its "private" but Im using the Accesswidener ;)
    @Invoker("getMapTexture")
    MapTextureManager.MapTexture invokerGetMapTexture(MapIdComponent id, MapState state);
}

