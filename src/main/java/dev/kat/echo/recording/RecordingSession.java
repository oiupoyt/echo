package dev.kat.echo.recording;

import dev.kat.echo.EchoClient;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RecordingSession {

    private final String id;
    private final Instant startTime;
    private final Path outputDir;
    private boolean active = false;

    private final List<PacketEvent> packetEvents = new ArrayList<>();
    private final List<FrameEntry>  frameEntries  = new ArrayList<>();

    public RecordingSession(String id, Path outputDir) {
        this.id = id;
        this.startTime = Instant.now();
        this.outputDir = outputDir;
    }

    public void start() {
        active = true;
        EchoClient.LOGGER.info("[Echo] Recording started: {}", id);
    }

    public void stop() {
        active = false;
        EchoClient.LOGGER.info("[Echo] Recording stopped: {} ({} packets, {} frames)",
            id, packetEvents.size(), frameEntries.size());
    }

    public void addPacketEvent(PacketEvent e) { if (active) packetEvents.add(e); }
    public void addFrameEntry(FrameEntry e)   { if (active) frameEntries.add(e); }

    public boolean isActive()                  { return active; }
    public String getId()                      { return id; }
    public Instant getStartTime()              { return startTime; }
    public Path getOutputDir()                 { return outputDir; }
    public List<PacketEvent> getPacketEvents() { return packetEvents; }
    public List<FrameEntry>  getFrameEntries() { return frameEntries; }

    public record PacketEvent(long timestampMs, byte[] packetData, Direction direction) {}
    public enum Direction { INBOUND, OUTBOUND }
    public record FrameEntry(long timestampMs, int frameIndex, int width, int height) {}
}
