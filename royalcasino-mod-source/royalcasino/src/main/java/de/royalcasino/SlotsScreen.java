package de.royalcasino;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3 Walzen, je 20 Stopps: Stern 1, Diamant 2, Glocke 4, Gold 5, Apfel 8.
 * 3 Gleiche: Stern 400x, Diamant 80x, Glocke 20x, Gold 8x, Apfel 4x. Genau 2 Äpfel: 1x.
 * Auszahlungsquote (RTP) = 95,9 %.
 */
public class SlotsScreen extends CasinoBase {
    static final Item[] ITEMS = {Items.NETHER_STAR, Items.DIAMOND, Items.BELL, Items.GOLD_INGOT, Items.APPLE};
    static final int[] WEIGHTS = {1, 2, 4, 5, 8};
    static final int[] MULT = {400, 80, 20, 8, 4};
    static final int[] BETS = {10, 50, 100, 250, 500, 1000};
    static final int[] STOP = {16, 28, 40};
    static int bet = 10;

    private final Random rnd = new Random();
    private final int[] reels = {0, 1, 4};
    private final int[] result = new int[3];
    private int ticks;
    private boolean spinning;
    private String msg = "Viel Glück!";
    private int msgColor = Ui.GOLD;
    private final List<GoldButton> betBtns = new ArrayList<>();
    private GoldButton spinBtn, backBtn;

    public SlotsScreen() {
        super("★ SLOTS ★", 300, 200);
    }

    @Override
    protected void build() {
        betBtns.clear();
        for (int i = 0; i < BETS.length; i++) {
            final int v = BETS[i];
            GoldButton b = addDrawableChild(new GoldButton(left + 30 + i * 40, top + 144, 38, 18, String.valueOf(v), x -> {
                bet = v;
                refresh();
            }));
            betBtns.add(b);
        }
        spinBtn = addDrawableChild(new GoldButton(left + 30, top + 168, 140, 22, "DREHEN", b -> spin(), Ui.RED_LIGHT));
        backBtn = addDrawableChild(new GoldButton(left + 180, top + 168, 90, 22, "Zurück", b -> client.setScreen(new CasinoScreen())));
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < betBtns.size(); i++) {
            betBtns.get(i).highlight = BETS[i] == bet;
            betBtns.get(i).active = !spinning;
        }
        spinBtn.active = !spinning;
        backBtn.active = !spinning;
    }

    private int pick() {
        int r = rnd.nextInt(20);
        for (int i = 0; i < WEIGHTS.length; i++) {
            r -= WEIGHTS[i];
            if (r < 0) return i;
        }
        return 4;
    }

    private void spin() {
        if (spinning) return;
        if (!CoinStore.take(bet)) {
            msg = "Nicht genug Coins!";
            msgColor = 0xFFFF5555;
            return;
        }
        for (int i = 0; i < 3; i++) result[i] = pick();
        spinning = true;
        ticks = 0;
        msg = "...";
        msgColor = Ui.GOLD;
        refresh();
    }

    @Override
    public void tick() {
        if (!spinning) return;
        ticks++;
        for (int i = 0; i < 3; i++) {
            if (ticks < STOP[i]) {
                if (ticks % 2 == 0) reels[i] = rnd.nextInt(ITEMS.length);
            } else {
                reels[i] = result[i];
            }
        }
        if (ticks >= STOP[2] + 6) finish();
    }

    private void finish() {
        long win = 0;
        if (result[0] == result[1] && result[1] == result[2]) {
            win = (long) bet * MULT[result[0]];
        } else {
            int apples = 0;
            for (int r : result) if (r == 4) apples++;
            if (apples == 2) win = bet;
        }
        if (win > 0) CoinStore.give(win);
        if (win > bet) {
            msg = "GEWINN! +" + Ui.fmt(win - bet) + " Coins";
            msgColor = 0xFF55FF55;
            Ui.winSound();
        } else if (win == bet) {
            msg = "2 Äpfel – Einsatz zurück";
            msgColor = Ui.GOLD;
        } else {
            msg = "Leider verloren. -" + Ui.fmt(bet);
            msgColor = 0xFFFF8888;
        }
        spinning = false;
        refresh();
    }

    @Override
    protected void draw(DrawContext c, int mx, int my, float delta) {
        int x0 = left + 60, y0 = top + 42;
        for (int i = 0; i < 3; i++) {
            int x = x0 + i * 62;
            c.fill(x, y0, x + 56, y0 + 56, Ui.GOLD);
            c.fill(x + 2, y0 + 2, x + 54, y0 + 54, 0xFF1A0000);
            c.getMatrices().push();
            c.getMatrices().translate(x + 4, y0 + 4, 0);
            c.getMatrices().scale(3f, 3f, 1f);
            c.drawItem(new ItemStack(ITEMS[reels[i]]), 0, 0);
            c.getMatrices().pop();
        }
        // Gewinnlinien-Pfeile
        c.fill(left + 50, y0 + 26, left + 58, y0 + 30, Ui.GOLD);
        c.fill(left + 242, y0 + 26, left + 250, y0 + 30, Ui.GOLD);

        c.drawCenteredTextWithShadow(textRenderer, msg, width / 2, top + 104, msgColor);
        Ui.small(c, textRenderer, "Stern 400x · Diamant 80x · Glocke 20x · Gold 8x · Apfel 4x", width / 2, top + 119, 0xFFE0C060, 0.7f);
        Ui.small(c, textRenderer, "Genau 2 Äpfel: Einsatz zurück · Auszahlungsquote ≈ 95,9 %", width / 2, top + 129, 0xFFE0C060, 0.7f);
        Ui.small(c, textRenderer, "Einsatz: " + Ui.fmt(bet), width / 2, top + 136, 0xFFFFFFFF, 0.7f);
    }
}
