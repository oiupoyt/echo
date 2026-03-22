package dev.kat.echo.keybind;

import dev.kat.echo.recording.RecordingManager;
import dev.kat.echo.replay.ReplayManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class EchoKeybinds {

    public static KeyBinding toggleRecording;
    public static KeyBinding saveShadow;
    public static KeyBinding openReplay;

    public static void register() {
        KeyBinding.Category category = new KeyBinding.Category(
            Identifier.of("echo", "keys"),
            100  // sort order - higher = lower in the list
        );

        toggleRecording = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.echo.toggle_recording", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, category));

        saveShadow = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.echo.save_shadow", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, category));

        openReplay = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.echo.open_replay", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleRecording.wasPressed()) RecordingManager.toggleRecording();
            if (saveShadow.wasPressed())       RecordingManager.saveShadow();
            if (openReplay.wasPressed())       ReplayManager.openReplayBrowser(client);
        });
    }
}
