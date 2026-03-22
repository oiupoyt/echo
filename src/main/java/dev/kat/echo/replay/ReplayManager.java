package dev.kat.echo.replay;

import dev.kat.echo.EchoClient;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ReplayManager {

    private static ReplaySession active = null;

    public static void openReplayBrowser(MinecraftClient client) {
        client.setScreen(new ReplayBrowserScreen(scan(client)));
    }

    public static List<Path> scan(MinecraftClient client) {
        Path dir = client.runDirectory.toPath().resolve("echo_recordings");
        List<Path> list = new ArrayList<>();
        if (!Files.exists(dir)) return list;
        try (Stream<Path> walk = Files.walk(dir, 2)) {
            walk.filter(p -> p.toString().endsWith(".rwd")).forEach(list::add);
        } catch (IOException e) {
            EchoClient.LOGGER.error("[Echo] Scan failed", e);
        }
        return list;
    }

    public static void openReplay(Path file) {
        try {
            active = ReplaySession.load(file);
            MinecraftClient.getInstance().setScreen(new ReplayViewerScreen(active));
        } catch (Exception e) {
            EchoClient.LOGGER.error("[Echo] Load failed: {}", file, e);
        }
    }

    public static void closeReplay() {
        if (active != null) { active.pause(); active = null; }
        MinecraftClient.getInstance().setScreen(null);
    }

    public static ReplaySession getActive() { return active; }
}
