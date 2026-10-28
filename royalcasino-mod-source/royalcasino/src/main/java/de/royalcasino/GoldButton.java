package de.royalcasino;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class GoldButton extends ButtonWidget {
    public boolean highlight = false;
    public int fill;
    public int textColor = Ui.GOLD;

    public GoldButton(int x, int y, int w, int h, String label, PressAction action) {
        this(x, y, w, h, label, action, Ui.RED);
    }

    public GoldButton(int x, int y, int w, int h, String label, PressAction action, int fill) {
        super(x, y, w, h, Text.literal(label), action, DEFAULT_NARRATION_SUPPLIER);
        this.fill = fill;
    }

    @Override
    public void renderWidget(DrawContext c, int mx, int my, float delta) {
        int x = getX(), y = getY();
        c.fill(x, y, x + width, y + height, highlight ? 0xFFFFFFFF : Ui.GOLD);
        c.fill(x + 1, y + 1, x + width - 1, y + height - 1, Ui.GOLD_DARK);
        c.fill(x + 2, y + 2, x + width - 2, y + height - 2, fill);
        c.fill(x + 2, y + 2, x + width - 2, y + 3, 0x40FFFFFF);
        if (active && isHovered()) c.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0x35FFFFFF);
        if (!active) c.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x90000000);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int tw = Math.max(1, tr.getWidth(getMessage()));
        float sc = Math.min(1f, (width - 4f) / tw);
        c.getMatrices().push();
        c.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
        c.getMatrices().scale(sc, sc, 1f);
        c.drawText(tr, getMessage(), -tw / 2, -4, active ? textColor : 0xFF888888, true);
        c.getMatrices().pop();
    }
}
