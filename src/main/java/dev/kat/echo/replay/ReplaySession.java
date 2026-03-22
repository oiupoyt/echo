package dev.kat.echo.replay;

import dev.kat.echo.recording.RecordingSession;
import dev.kat.echo.util.RwdReader;

import java.nio.file.Path;
import java.util.List;

public class ReplaySession {

    private final Path sourceFile;
    private final RwdReader.ReplayData data;

    private long wallStart    = -1;
    private long offsetMs     = 0;
    private boolean playing   = false;
    private boolean freeCam   = false;

    public ReplaySession(Path sourceFile, RwdReader.ReplayData data) {
        this.sourceFile = sourceFile;
        this.data       = data;
    }

    public static ReplaySession load(Path file) throws Exception {
        return new ReplaySession(file, RwdReader.read(file));
    }

    public void play()  { wallStart = System.currentTimeMillis() - offsetMs; playing = true; }
    public void pause() { offsetMs = positionMs(); playing = false; }

    public void seekTo(long ms) {
        offsetMs = ms;
        if (playing) wallStart = System.currentTimeMillis() - ms;
    }

    public long positionMs() {
        return playing ? System.currentTimeMillis() - wallStart : offsetMs;
    }

    public long durationMs() {
        long lastPkt   = data.packets().isEmpty() ? 0 :
            data.packets().get(data.packets().size()-1).timestampMs() - data.startTimeMs();
        long lastFrame = data.frames().isEmpty()  ? 0 :
            data.frames().get(data.frames().size()-1).timestampMs()  - data.startTimeMs();
        return Math.max(lastPkt, lastFrame);
    }

    public RecordingSession.FrameEntry currentFrame() {
        long target = positionMs() + data.startTimeMs();
        RecordingSession.FrameEntry best = null;
        for (var f : data.frames()) {
            if (f.timestampMs() <= target) best = f;
            else break;
        }
        return best;
    }

    public byte[] framePixels(int idx)          { return data.framePixels().get(idx); }
    public List<RecordingSession.PacketEvent> packetsUpTo(long ms) {
        long cutoff = ms + data.startTimeMs();
        return data.packets().stream().filter(p -> p.timestampMs() <= cutoff).toList();
    }

    public boolean isPlaying()              { return playing; }
    public boolean isFreeCam()              { return freeCam; }
    public void setFreeCam(boolean v)       { freeCam = v; }
    public Path getSourceFile()             { return sourceFile; }
    public RwdReader.ReplayData getData()   { return data; }
}
