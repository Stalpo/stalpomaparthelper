package net.stalpo.stalpomaparthelper.mixin;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.datafixer.fix.MapIdFix;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.List;

@Mixin(ChatHud.class)
public class ThreadsafeChatRenderMixin {
    @Shadow
    @Final
    private List<ChatHudLine> messages;

    @Final
    @Shadow
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    private void addVisibleMessage(ChatHudLine message) {
    }

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    synchronized public void refresh(CallbackInfo ci) {
        synchronized (this.messages) {
            this.visibleMessages.clear();

            for (ChatHudLine chatHudLine : Lists.reverse(this.messages)) {
                this.addVisibleMessage(chatHudLine);
            }
            ci.cancel();
        }
    }
}
