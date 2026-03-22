package dev.kat.echo.util;

import dev.kat.echo.recording.RecordingSession;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

/**
 * .rwd binary format:
 *   [4]  magic 0x45434F52
 *   [4]  version 1
 *   [8]  startTimeMs
 *   [4]  packetCount
 *     per packet: [8] ts, [1] dir, [4] len, [len] data
 *   [4]  frameCount
 *     per frame:  [8] ts, [4] idx, [4] w, [4] h, [4] len, [len] zlib(pixels)
 */
public class RwdWriter {

    public static final int MAGIC   = 0x45434F52;
    public static final int VERSION = 1;

    public static void write(RecordingSession session, Path out,
                             Map<Integer, byte[]> framePixels) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(out.toFile())))) {

            dos.writeInt(MAGIC);
            dos.writeInt(VERSION);
            dos.writeLong(session.getStartTime().toEpochMilli());

            var pkts = session.getPacketEvents();
            dos.writeInt(pkts.size());
            for (var p : pkts) {
                dos.writeLong(p.timestampMs());
                dos.writeByte(p.direction() == RecordingSession.Direction.INBOUND ? 0 : 1);
                dos.writeInt(p.packetData().length);
                dos.write(p.packetData());
            }

            var frames = session.getFrameEntries();
            dos.writeInt(frames.size());
            for (var f : frames) {
                dos.writeLong(f.timestampMs());
                dos.writeInt(f.frameIndex());
                dos.writeInt(f.width());
                dos.writeInt(f.height());
                byte[] compressed = compress(framePixels.getOrDefault(f.frameIndex(), new byte[0]));
                dos.writeInt(compressed.length);
                dos.write(compressed);
            }
        }
    }

    private static byte[] compress(byte[] data) throws IOException {
        if (data.length == 0) return data;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DeflaterOutputStream def = new DeflaterOutputStream(bos)) { def.write(data); }
        return bos.toByteArray();
    }
}
