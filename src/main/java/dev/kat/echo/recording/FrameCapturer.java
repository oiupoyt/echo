package dev.kat.echo.recording;

import dev.kat.echo.EchoClient;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrameCapturer {

    private static int frameIndex = 0;

    public static final Map<Integer, byte[]> sessionPixels = new ConcurrentHashMap<>();
    public static final Map<Integer, byte[]> shadowPixels  = new ConcurrentHashMap<>();

    private static final ExecutorService encoder = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "echo-encoder");
        t.setDaemon(true);
        return t;
    });

    public static void onFrameEnd() {
        EchoClient echo = EchoClient.getInstance();
        if (echo == null) return;
        boolean sessionActive = echo.isRecording();
        boolean shadowActive  = RecordingManager.getShadowBuffer() != null;
        if (!sessionActive && !shadowActive) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int width  = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        long now   = System.currentTimeMillis();
        int index  = frameIndex++;

        byte[] pixels = readPixels(width, height);
        encoder.submit(() -> processFrame(pixels, width, height, now, index, sessionActive, shadowActive));
    }

    private static byte[] readPixels(int width, int height) {
        ByteBuffer buf   = ByteBuffer.allocateDirect(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }

    private static void processFrame(byte[] pixels, int width, int height,
                                     long tsMs, int index,
                                     boolean sessionActive, boolean shadowActive) {
        RecordingSession session = EchoClient.getInstance().getActiveSession();
        ShadowBuffer shadow      = RecordingManager.getShadowBuffer();
        RecordingSession.FrameEntry entry = new RecordingSession.FrameEntry(tsMs, index, width, height);

        if (sessionActive && session != null && session.isActive()) {
            session.addFrameEntry(entry);
            sessionPixels.put(index, pixels);
        }
        if (shadowActive && shadow != null) {
            shadow.pushFrame(entry);
            shadowPixels.put(index, pixels);
            if (shadowPixels.size() > 18000)
                shadowPixels.keySet().stream().sorted().limit(100).forEach(shadowPixels::remove);
        }
    }

    public static void clearSessionPixels() {
        sessionPixels.clear();
        frameIndex = 0;
    }
}
