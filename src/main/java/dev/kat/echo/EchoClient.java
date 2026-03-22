package dev.kat.echo;

import dev.kat.echo.editor.EchoHudOverlay;
import dev.kat.echo.keybind.EchoKeybinds;
import dev.kat.echo.recording.RecordingManager;
import dev.kat.echo.recording.RecordingSession;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoClient implements ClientModInitializer {

    public static final String MOD_ID = "echo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static EchoClient instance;
    private RecordingSession activeSession = null;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[Echo] Initializing...");
        RecordingManager.init();
        EchoKeybinds.register();
        EchoHudOverlay.register();
        LOGGER.info("[Echo] Ready. F9=record  F8=shadow  F7=replays");
    }

    public static EchoClient getInstance()                       { return instance; }
    public RecordingSession getActiveSession()                   { return activeSession; }
    public void setActiveSession(RecordingSession s)             { activeSession = s; }
    public boolean isRecording()                                 { return activeSession != null && activeSession.isActive(); }
}
