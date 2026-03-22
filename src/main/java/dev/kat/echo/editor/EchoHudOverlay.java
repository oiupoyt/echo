package dev.kat.echo.editor;

import dev.kat.echo.EchoClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class EchoHudOverlay implements HudRenderCallback {

    public static void register() {
        HudRenderCallback.EVENT.register(new EchoHudOverlay());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!EchoClient.getInstance().isRecording()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int screenW = mc.getWindow().getScaledWidth();
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;

        int x = screenW - 48, y = 8;
        if (blink) context.fill(x, y, x + 8, y + 8, 0xFFFF3333);
        context.drawText(mc.textRenderer, "REC", x + 11, y, 0xFFFF3333, true);
    }
}
