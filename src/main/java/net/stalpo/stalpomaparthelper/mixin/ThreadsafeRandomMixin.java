package net.stalpo.stalpomaparthelper.mixin;

import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.thread.LockHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(CheckedRandom.class)
public class ThreadsafeRandomMixin {
    @Shadow
    @Final
    private AtomicLong seed;

    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    synchronized void next(int bits, CallbackInfoReturnable<Integer> cir) {
        long l = this.seed.get();
        long m = l * 25214903917L + 11L & 281474976710655L;
        if (!this.seed.compareAndSet(l, m)) {
            throw LockHelper.crash("LegacyRandomSource", (Thread) null);
        }
        cir.setReturnValue((int) (m >> (48 - bits)));
        cir.cancel();
    }
}