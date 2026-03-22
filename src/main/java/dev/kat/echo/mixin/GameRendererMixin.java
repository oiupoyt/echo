package dev.kat.echo.mixin;

import dev.kat.echo.recording.FrameCapturer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void echo$captureFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        FrameCapturer.onFrameEnd();
    }
}
