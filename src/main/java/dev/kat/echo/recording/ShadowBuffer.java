package dev.kat.echo.recording;

import dev.kat.echo.EchoClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class ShadowBuffer {

    private final long maxDurationMs;
    private boolean running = false;

    private final Deque<RecordingSession.PacketEvent> packets = new ArrayDeque<>();
    private final Deque<RecordingSession.FrameEntry>  frames  = new ArrayDeque<>();

    public ShadowBuffer(long maxDurationMs) { this.maxDurationMs = maxDurationMs; }

    public void start() { running = true; }
    public void stop()  { running = false; }

    public synchronized void pushPacket(RecordingSession.PacketEvent e) {
        if (!running) return;
        packets.addLast(e);
        long cutoff = e.timestampMs() - maxDurationMs;
        while (!packets.isEmpty() && packets.peekFirst().timestampMs() < cutoff) packets.pollFirst();
    }

    public synchronized void pushFrame(RecordingSession.FrameEntry e) {
        if (!running) return;
        frames.addLast(e);
        long cutoff = e.timestampMs() - maxDurationMs;
        while (!frames.isEmpty() && frames.peekFirst().timestampMs() < cutoff) frames.pollFirst();
    }

    public synchronized void flush(Path outputDir) {
        try {
            Files.createDirectories(outputDir);
            EchoClient.LOGGER.info("[Echo] Shadow flushed: {} packets, {} frames",
                packets.size(), frames.size());
        } catch (Exception e) {
            EchoClient.LOGGER.error("[Echo] Shadow flush failed", e);
        }
    }

    public Deque<RecordingSession.PacketEvent> getPackets() { return packets; }
    public Deque<RecordingSession.FrameEntry>  getFrames()  { return frames; }
}
