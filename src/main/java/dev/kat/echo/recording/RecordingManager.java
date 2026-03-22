package dev.kat.echo.recording;

import dev.kat.echo.EchoClient;
import dev.kat.echo.util.RwdWriter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RecordingManager {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Shadow buffer is NULL by default - only created when user presses F8
    private static ShadowBuffer shadowBuffer = null;

    public static void init() {
        // Do NOT start shadow buffer on init - too memory-heavy
        // It starts only when the user presses F8
        EchoClient.LOGGER.info("[Echo] Initialized. Shadow recording is off by default.");
    }

    public static void toggleRecording() {
        if (EchoClient.getInstance().isRecording()) stopRecording();
        else startRecording();
    }

    public static void startRecording() {
        EchoClient echo = EchoClient.getInstance();
        if (echo.isRecording()) return;
        try {
            Path dir = recordingsDir().resolve(FMT.format(LocalDateTime.now()));
            Files.createDirectories(dir);
            RecordingSession session = new RecordingSession("echo_" + System.currentTimeMillis(), dir);
            echo.setActiveSession(session);
            session.start();
            msg("\u00a7a[Echo] Recording started \u2014 F9 to stop.");
        } catch (Exception e) {
            EchoClient.LOGGER.error("[Echo] Failed to start", e);
        }
    }

    public static void stopRecording() {
        EchoClient echo = EchoClient.getInstance();
        RecordingSession session = echo.getActiveSession();
        if (session == null) return;
        session.stop();
        echo.setActiveSession(null);

        Map<Integer, byte[]> pixels = new HashMap<>(FrameCapturer.sessionPixels);
        FrameCapturer.clearSessionPixels();

        CompletableFuture.runAsync(() -> {
            try {
                RwdWriter.write(session, session.getOutputDir().resolve("recording.rwd"), pixels);
                EchoClient.LOGGER.info("[Echo] Saved: {}", session.getOutputDir());
            } catch (Exception e) {
                EchoClient.LOGGER.error("[Echo] Save failed", e);
            }
        });
        msg("\u00a7e[Echo] Recording saved.");
    }

    public static void saveShadow() {
        // Shadow save: only captures from the active recording session if one exists
        EchoClient echo = EchoClient.getInstance();
        if (!echo.isRecording()) {
            msg("\u00a7c[Echo] Start a recording first (F9), then F8 to save a clip.");
            return;
        }
        RecordingSession session = echo.getActiveSession();
        if (session == null) return;

        Path dir = recordingsDir().resolve("clip_" + FMT.format(LocalDateTime.now()));
        Map<Integer, byte[]> pixels = new HashMap<>(FrameCapturer.sessionPixels);

        CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(dir);
                RwdWriter.write(session, dir.resolve("clip.rwd"), pixels);
                EchoClient.LOGGER.info("[Echo] Clip saved: {}", dir);
            } catch (Exception e) {
                EchoClient.LOGGER.error("[Echo] Clip save failed", e);
            }
        });
        msg("\u00a7b[Echo] Clip saved.");
    }

    // Shadow buffer always returns null now - disabled
    public static ShadowBuffer getShadowBuffer() { return shadowBuffer; }

    private static Path recordingsDir() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("echo_recordings");
    }

    private static void msg(String s) {
        var p = MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal(s), true);
    }
}
