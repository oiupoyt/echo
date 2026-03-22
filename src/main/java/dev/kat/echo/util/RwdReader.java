package dev.kat.echo.util;

import dev.kat.echo.recording.RecordingSession;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.InflaterInputStream;

public class RwdReader {

    public record ReplayData(
        long startTimeMs,
        List<RecordingSession.PacketEvent> packets,
        List<RecordingSession.FrameEntry>  frames,
        Map<Integer, byte[]>               framePixels
    ) {}

    public static ReplayData read(Path file) throws IOException {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file.toFile())))) {

            if (dis.readInt() != RwdWriter.MAGIC)   throw new IOException("Not a .rwd file");
            if (dis.readInt() != RwdWriter.VERSION)  throw new IOException("Unsupported version");
            long startMs = dis.readLong();

            int pc = dis.readInt();
            List<RecordingSession.PacketEvent> packets = new ArrayList<>(pc);
            for (int i = 0; i < pc; i++) {
                long ts  = dis.readLong();
                int  dir = dis.readByte() & 0xFF;
                byte[] d = dis.readNBytes(dis.readInt());
                packets.add(new RecordingSession.PacketEvent(ts, d,
                    dir == 0 ? RecordingSession.Direction.INBOUND : RecordingSession.Direction.OUTBOUND));
            }

            int fc = dis.readInt();
            List<RecordingSession.FrameEntry> frames = new ArrayList<>(fc);
            Map<Integer, byte[]> pixels = new HashMap<>();
            for (int i = 0; i < fc; i++) {
                long ts  = dis.readLong();
                int  idx = dis.readInt(), w = dis.readInt(), h = dis.readInt();
                byte[] px = decompress(dis.readNBytes(dis.readInt()));
                frames.add(new RecordingSession.FrameEntry(ts, idx, w, h));
                pixels.put(idx, px);
            }

            return new ReplayData(startMs, packets, frames, pixels);
        }
    }

    private static byte[] decompress(byte[] data) throws IOException {
        if (data.length == 0) return data;
        try (InflaterInputStream inf = new InflaterInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            inf.transferTo(bos);
            return bos.toByteArray();
        }
    }
}
