package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(SoundSystem.class)
public class ThreadsafeSoundSystemMixin {

    @Shadow
    @Final
    private Map<SoundInstance, Channel.SourceManager> sources;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(SoundManager loader, GameOptions settings, ResourceFactory resourceFactory, CallbackInfo ci) {
        ((SoundSystemAccessor) this).setSources(new ConcurrentHashMap<>());
    }
}