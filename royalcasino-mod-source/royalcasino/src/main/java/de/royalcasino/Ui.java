package de.royalcasino;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.Locale;

public final class Ui {
    public static final int GOLD = 0xFFFFD700;
    public static final int GOLD_DARK = 0xFFB8860B;
    public static final int RED = 0xFF8B0000;
    public static final int RED_DARK = 0xFF4A0000;
    public static final int RED_LIGHT = 0xFFB01818;

    /** Roter Block-Rahmen mit abwechselnd goldenen Klötzchen. */
    public static void panel(DrawContext c, int x, int y, int w, int h) {
        c.fill(x, y, x + w, y + h, RED_DARK);
        int s = 6;
        for (int i = 0; i * s < w; i++) {
            int sw = Math.min(s, w - i * s);
            int a = (i % 2 == 0) ? GOLD : GOLD_DARK;
            int b = (i % 2 == 0) ? GOLD_DARK : GOLD;
            c.fill(x + i * s, y, x + i * s + sw, y + s, a);
            c.fill(x + i * s, y + h - s, x + i * s + sw, y + h, b);
        }
        for (int j = 0; j * s < h; j++) {
            int sh = Math.min(s, h - j * s);
            int a = (j % 2 == 0) ? GOLD : GOLD_DARK;
            int b = (j % 2 == 0) ? GOLD_DARK : GOLD;
            c.fill(x, y + j * s, x + s, y + j * s + sh, a);
            c.fill(x + w - s, y + j * s, x + w, y + j * s + sh, b);
        }
        c.fill(x + s, y + s, x + w - s, y + h - s, GOLD_DARK);
        c.fill(x + s + 1, y + s + 1, x + w - s - 1, y + h - s - 1, RED);
    }

    public static String fmt(long n) {
        return String.format(Locale.GERMANY, "%,d", n);
    }

    public static void small(DrawContext c, TextRenderer tr, String text, int cx, int y, int color, float scale) {
        int w = tr.getWidth(text);
        c.getMatrices().push();
        c.getMatrices().translate(cx - w * scale / 2f, y, 0);
        c.getMatrices().scale(scale, scale, 1f);
        c.drawText(tr, text, 0, 0, color, false);
        c.getMatrices().pop();
    }

    public static void winSound() {
        MinecraftClient.getInstance().getSoundManager()
                .play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f));
    }

    private Ui() {}
}
