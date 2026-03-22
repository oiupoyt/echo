package dev.kat.echo.mixin;

import dev.kat.echo.recording.PacketRecorder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    // Outbound (send) removed — method signature changed in 1.21.11
    // Only inbound packet capture is active

    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void echo$onReceive(
            Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        PacketRecorder.onInbound(packet);
    }
}
