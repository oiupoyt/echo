package dev.kat.echo.recording;

import dev.kat.echo.EchoClient;
import net.minecraft.network.packet.Packet;
import java.nio.charset.StandardCharsets;

public class PacketRecorder {

    public static void onInbound(Packet<?> packet)  { capture(packet, RecordingSession.Direction.INBOUND); }
    public static void onOutbound(Packet<?> packet) { capture(packet, RecordingSession.Direction.OUTBOUND); }

    private static void capture(Packet<?> packet, RecordingSession.Direction dir) {
        RecordingSession session = EchoClient.getInstance().getActiveSession();
        ShadowBuffer shadow      = RecordingManager.getShadowBuffer();

        byte[] data = packet.getClass().getSimpleName().getBytes(StandardCharsets.UTF_8);
        long now    = System.currentTimeMillis();
        RecordingSession.PacketEvent event = new RecordingSession.PacketEvent(now, data, dir);

        if (session != null && session.isActive()) session.addPacketEvent(event);
        if (shadow  != null)                       shadow.pushPacket(event);
    }
}
