package de.royalcasino;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RoyalCasinoClient implements ClientModInitializer {
    // Die "Ö"-Taste einer deutschen Tastatur sitzt auf der Position von GLFW_KEY_SEMICOLON.
    public static KeyBinding OPEN_KEY;

    @Override
    public void onInitializeClient() {
        CoinStore.load();
        OPEN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.royalcasino.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, "category.royalcasino"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_KEY.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new CasinoScreen());
                }
            }
        });
    }
}
