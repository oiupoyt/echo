package dev.kat.echo.replay;

import dev.kat.echo.recording.RecordingSession;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.ByteBuffer;

public class ReplayViewerScreen extends Screen {

    private static final Identifier HUD_TEX = Identifier.of("echo", "replay_hud");

    private final ReplaySession session;
    private ButtonWidget playPauseBtn;
    private NativeImageBackedTexture hudTexture;
    private boolean hudLoaded = false;
    private int lastFrame     = -1;

    public ReplayViewerScreen(ReplaySession session) {
        super(Text.literal("Echo \u2014 Replay Viewer"));
        this.session = session;
    }

    @Override
    protected void init() {
        int cy = this.height - 50, cx = this.width / 2;

        playPauseBtn = ButtonWidget.builder(Text.literal("Pause"), b -> togglePlay())
            .dimensions(cx - 55, cy, 50, 20).build();
        addDrawableChild(playPauseBtn);

        addDrawableChild(ButtonWidget.builder(Text.literal("Free Cam"),
            b -> session.setFreeCam(!session.isFreeCam()))
            .dimensions(cx + 5, cy, 70, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> {
            ReplayManager.closeReplay(); this.close();
        }).dimensions(cx + 85, cy, 60, 20).build());

        session.play();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.renderBackground(ctx, mx, my, delta);

        // Update HUD texture if frame changed
        RecordingSession.FrameEntry frame = session.currentFrame();
        if (frame != null && frame.frameIndex() != lastFrame) {
            byte[] px = session.framePixels(frame.frameIndex());
            if (px != null) {
                uploadFrame(px, frame.width(), frame.height());
                lastFrame = frame.frameIndex();
            }
        }

        // Draw captured HUD frame as fullscreen overlay
        if (hudLoaded) {
            ctx.drawTexture(
                net.minecraft.client.render.RenderLayer::getGuiTextured,
                HUD_TEX,
                0, 0, 0f, 0f,
                this.width, this.height,
                this.width, this.height
            );
        }

        drawTimeline(ctx);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 6, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal(fmt(session.positionMs()) + " / " + fmt(session.durationMs())),
            width / 2, height - 65, 0xCCCCCC);

        super.render(ctx, mx, my, delta);
    }

    private void drawTimeline(DrawContext ctx) {
        int bx = 40, by = height - 28, bw = width - 80, bh = 6;
        ctx.fill(bx, by, bx + bw, by + bh, 0x88000000);
        long dur = session.durationMs();
        if (dur > 0) {
            int pw = (int)(bw * Math.min((float) session.positionMs() / dur, 1f));
            ctx.fill(bx, by, bx + pw, by + bh, 0xFF55AAFF);
        }
    }

    private void togglePlay() {
        if (session.isPlaying()) {
            session.pause();
            playPauseBtn.setMessage(Text.literal("Play"));
        } else {
            session.play();
            playPauseBtn.setMessage(Text.literal("Pause"));
        }
    }

    private void uploadFrame(byte[] rgba, int w, int h) {
        try {
            NativeImage img = new NativeImage(NativeImage.Format.RGBA, w, h, false);
            ByteBuffer buf  = ByteBuffer.wrap(rgba);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int off = ((h - 1 - y) * w + x) * 4;
                    int r = buf.get(off) & 0xFF, g = buf.get(off+1) & 0xFF,
                        b = buf.get(off+2) & 0xFF, a = buf.get(off+3) & 0xFF;
                    img.setColor(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            if (hudTexture == null) {
                hudTexture = new NativeImageBackedTexture(() -> "echo:replay_hud", img);
                MinecraftClient.getInstance().getTextureManager().registerTexture(HUD_TEX, hudTexture);
            } else {
                hudTexture.setImage(img);
                hudTexture.upload();
            }
            hudLoaded = true;
        } catch (Exception ignored) {}
    }

    private String fmt(long ms) {
        long s = ms / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    @Override
    public void close() {
        if (hudTexture != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(HUD_TEX);
            hudTexture = null;
        }
        super.close();
    }
}
