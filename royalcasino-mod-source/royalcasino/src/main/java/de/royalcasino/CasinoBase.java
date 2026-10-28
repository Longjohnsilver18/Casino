package de.royalcasino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class CasinoBase extends Screen {
    protected final int pw, ph;
    protected int left, top;

    protected CasinoBase(String title, int pw, int ph) {
        super(Text.literal(title));
        this.pw = pw;
        this.ph = ph;
    }

    @Override
    protected void init() {
        left = (width - pw) / 2;
        top = (height - ph) / 2;
        build();
    }

    protected abstract void build();

    /** Overlay, das nach den Buttons gezeichnet wird. */
    protected abstract void draw(DrawContext c, int mx, int my, float delta);

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0xC0100000);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        renderBackground(c, mx, my, delta);
        Ui.panel(c, left, top, pw, ph);
        c.drawCenteredTextWithShadow(textRenderer, title.copy().formatted(Formatting.BOLD), width / 2, top + 10, Ui.GOLD);
        String s = Ui.fmt(CoinStore.coins()) + " Coins";
        int w = textRenderer.getWidth(s);
        int cx = width / 2;
        c.drawItem(new ItemStack(Items.GOLD_INGOT), cx - w / 2 - 20, top + 20);
        c.drawTextWithShadow(textRenderer, s, cx - w / 2, top + 25, Ui.GOLD);
        super.render(c, mx, my, delta);
        draw(c, mx, my, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (RoyalCasinoClient.OPEN_KEY != null && RoyalCasinoClient.OPEN_KEY.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
