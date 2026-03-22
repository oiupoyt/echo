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
    private static ByteBuffer reusableBuffer = null;
    private static int lastBufferSize = 0;
    private static long lastCaptureMs = 0;
    private static final long MIN_FRAME_INTERVAL_MS = 33; // cap at ~30fps

    public static final Map<Integer, byte[]> sessionPixels = new ConcurrentHashMap<>();

    private static final ExecutorService encoder = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "echo-encoder");
        t.setDaemon(true);
        return t;
    });

    public static void onFrameEnd() {
        EchoClient echo = EchoClient.getInstance();
        if (echo == null) return;

        // Only capture when actively recording - no background capture at all
        if (!echo.isRecording()) return;

        // Throttle to 30fps max to limit memory usage during long recordings
        long now = System.currentTimeMillis();
        if (now - lastCaptureMs < MIN_FRAME_INTERVAL_MS) return;
        lastCaptureMs = now;

        MinecraftClient mc = MinecraftClient.getInstance();
        int width  = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        int index  = frameIndex++;

        byte[] pixels = readPixels(width, height);
        if (pixels == null) return;

        final long ts = now;
        encoder.submit(() -> {
            RecordingSession session = EchoClient.getInstance().getActiveSession();
            if (session != null && session.isActive()) {
                session.addFrameEntry(new RecordingSession.FrameEntry(ts, index, width, height));
                sessionPixels.put(index, pixels);

                // Keep session pixel map bounded - drop oldest if over 900 frames (~30s at 30fps)
                if (sessionPixels.size() > 900) {
                    sessionPixels.keySet().stream()
                        .sorted().limit(60).forEach(sessionPixels::remove);
                }
            }
        });
    }

    private static byte[] readPixels(int width, int height) {
        try {
            int needed = width * height * 4;
            if (reusableBuffer == null || lastBufferSize != needed) {
                reusableBuffer = ByteBuffer.allocateDirect(needed);
                lastBufferSize = needed;
            }
            reusableBuffer.clear();
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, reusableBuffer);
            byte[] bytes = new byte[needed];
            reusableBuffer.get(bytes);
            return bytes;
        } catch (Exception e) {
            EchoClient.LOGGER.error("[Echo] readPixels failed", e);
            return null;
        }
    }

    public static void clearSessionPixels() {
        sessionPixels.clear();
        frameIndex = 0;
    }
}
