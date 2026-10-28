package de.royalcasino;

import net.minecraft.client.gui.DrawContext;

import java.util.*;

/** Europäisches Roulette (1 Null): Plein 35:1, Dutzend 2:1, einfache Chancen 1:1 -> RTP 97,3 %. */
public class RouletteScreen extends CasinoBase {
    static final Set<Integer> REDS = Set.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);
    static final int[] CHIPS = {10, 50, 100, 500, 1000};
    static final int GREEN = 0xFF0B6B2B, RED_NUM = 0xFFB01010, BLACK_NUM = 0xFF151515;
    static int chip = 10;

    private final Random rnd = new Random();
    private final Map<String, Integer> bets = new LinkedHashMap<>();
    private final Map<String, int[]> rects = new HashMap<>();
    private final List<GoldButton> all = new ArrayList<>();
    private final List<GoldButton> chipBtns = new ArrayList<>();
    private boolean spinning;
    private int ticks, result, shown = -1;
    private String msg = "Platziere deine Chips und drehe!";
    private int msgColor = Ui.GOLD;

    public RouletteScreen() {
        super("★ ROULETTE ★", 300, 216);
    }

    static int colorOf(int n) {
        return n == 0 ? GREEN : REDS.contains(n) ? RED_NUM : BLACK_NUM;
    }

    private GoldButton spot(String key, String label, int x, int y, int w, int h, int fill) {
        GoldButton b = addDrawableChild(new GoldButton(x, y, w, h, label, btn -> place(key), fill));
        rects.put(key, new int[]{x, y, w, h});
        all.add(b);
        return b;
    }

    @Override
    protected void build() {
        rects.clear();
        all.clear();
        chipBtns.clear();
        int x0 = left + 40, gy = top + 48;

        spot("N:0", "0", left + 20, gy, 18, 48, GREEN);
        for (int col = 0; col < 12; col++) {
            for (int row = 0; row < 3; row++) {
                int n = col * 3 + (3 - row);
                spot("N:" + n, String.valueOf(n), x0 + col * 20, gy + row * 16, 20, 16, colorOf(n));
            }
        }
        spot("D1", "1.–12. (2:1)", x0, top + 98, 80, 14, Ui.RED);
        spot("D2", "13.–24. (2:1)", x0 + 80, top + 98, 80, 14, Ui.RED);
        spot("D3", "25.–36. (2:1)", x0 + 160, top + 98, 80, 14, Ui.RED);
        String[] keys = {"LOW", "EVEN", "RED", "BLACK", "ODD", "HIGH"};
        String[] labels = {"1–18", "Gerade", "Rot", "Schwarz", "Ungerade", "19–36"};
        int[] fills = {Ui.RED, Ui.RED, RED_NUM, BLACK_NUM, Ui.RED, Ui.RED};
        for (int i = 0; i < 6; i++) spot(keys[i], labels[i], x0 + i * 40, top + 114, 40, 14, fills[i]);

        for (int i = 0; i < CHIPS.length; i++) {
            final int v = CHIPS[i];
            GoldButton b = addDrawableChild(new GoldButton(left + 40 + i * 48, top + 134, 44, 16, String.valueOf(v), x -> {
                chip = v;
                refresh();
            }));
            chipBtns.add(b);
            all.add(b);
        }
        all.add(addDrawableChild(new GoldButton(left + 40, top + 156, 110, 22, "DREHEN", b -> spin(), Ui.RED_LIGHT)));
        all.add(addDrawableChild(new GoldButton(left + 156, top + 156, 60, 22, "Löschen", b -> clearBets())));
        all.add(addDrawableChild(new GoldButton(left + 222, top + 156, 58, 22, "Zurück", b -> client.setScreen(new CasinoScreen()))));
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < chipBtns.size(); i++) chipBtns.get(i).highlight = CHIPS[i] == chip;
        for (GoldButton b : all) b.active = !spinning;
    }

    private void place(String key) {
        if (spinning) return;
        if (!CoinStore.take(chip)) {
            msg = "Nicht genug Coins!";
            msgColor = 0xFFFF5555;
            return;
        }
        bets.merge(key, chip, Integer::sum);
    }

    private long totalBet() {
        long t = 0;
        for (int v : bets.values()) t += v;
        return t;
    }

    private void clearBets() {
        if (spinning) return;
        CoinStore.give(totalBet());
        bets.clear();
    }

    private void spin() {
        if (spinning) return;
        if (bets.isEmpty()) {
            msg = "Setze zuerst mindestens einen Chip.";
            msgColor = 0xFFFF8888;
            return;
        }
        result = rnd.nextInt(37);
        spinning = true;
        ticks = 0;
        msg = "Rien ne va plus...";
        msgColor = Ui.GOLD;
        refresh();
    }

    @Override
    public void tick() {
        if (!spinning) return;
        ticks++;
        if ((ticks < 30 && ticks % 2 == 0) || (ticks >= 30 && ticks % 5 == 0)) shown = rnd.nextInt(37);
        if (ticks >= 46) {
            shown = result;
            resolve();
        }
    }

    /** Gesamtrückgabe-Faktor (Einsatz inklusive). 0 = verloren. */
    static int payoutFactor(String key, int r) {
        if (key.startsWith("N:")) return Integer.parseInt(key.substring(2)) == r ? 36 : 0;
        return switch (key) {
            case "RED" -> (r != 0 && REDS.contains(r)) ? 2 : 0;
            case "BLACK" -> (r != 0 && !REDS.contains(r)) ? 2 : 0;
            case "EVEN" -> (r != 0 && r % 2 == 0) ? 2 : 0;
            case "ODD" -> (r % 2 == 1) ? 2 : 0;
            case "LOW" -> (r >= 1 && r <= 18) ? 2 : 0;
            case "HIGH" -> (r >= 19 && r <= 36) ? 2 : 0;
            case "D1" -> (r >= 1 && r <= 12) ? 3 : 0;
            case "D2" -> (r >= 13 && r <= 24) ? 3 : 0;
            case "D3" -> (r >= 25 && r <= 36) ? 3 : 0;
            default -> 0;
        };
    }

    private void resolve() {
        long payout = 0, staked = totalBet();
        for (Map.Entry<String, Integer> e : bets.entrySet()) {
            payout += (long) e.getValue() * payoutFactor(e.getKey(), result);
        }
        CoinStore.give(payout);
        long net = payout - staked;
        String col = result == 0 ? "Grün" : REDS.contains(result) ? "Rot" : "Schwarz";
        if (net > 0) {
            msg = result + " (" + col + ") – Gewinn +" + Ui.fmt(net) + " Coins!";
            msgColor = 0xFF55FF55;
            Ui.winSound();
        } else if (net == 0) {
            msg = result + " (" + col + ") – Einsatz zurück.";
            msgColor = Ui.GOLD;
        } else {
            msg = result + " (" + col + ") – Verlust " + Ui.fmt(-net) + " Coins.";
            msgColor = 0xFFFF8888;
        }
        bets.clear();
        spinning = false;
        refresh();
    }

    @Override
    protected void draw(DrawContext c, int mx, int my, float delta) {
        // Chip-Markierungen
        for (Map.Entry<String, Integer> e : bets.entrySet()) {
            int[] r = rects.get(e.getKey());
            if (r == null) continue;
            int a = e.getValue();
            String s = a >= 1000 ? (a % 1000 == 0 ? (a / 1000) + "k" : String.format("%.1fk", a / 1000.0)) : String.valueOf(a);
            float sc = 0.6f;
            int bw = (int) (textRenderer.getWidth(s) * sc) + 3;
            int bx = r[0] + r[2] - bw - 1, by = r[1] + 1;
            c.fill(bx, by, bx + bw, by + 7, 0xFF000000);
            c.getMatrices().push();
            c.getMatrices().translate(bx + 1.5f, by + 1, 0);
            c.getMatrices().scale(sc, sc, 1f);
            c.drawText(textRenderer, s, 0, 0, Ui.GOLD, false);
            c.getMatrices().pop();
        }
        // Ergebniszahl
        int bx = left + 40, by = top + 183;
        if (shown >= 0) {
            c.fill(bx, by, bx + 24, by + 18, Ui.GOLD);
            c.fill(bx + 1, by + 1, bx + 23, by + 17, colorOf(shown));
            c.drawCenteredTextWithShadow(textRenderer, String.valueOf(shown), bx + 12, by + 5, 0xFFFFFFFF);
        }
        c.drawTextWithShadow(textRenderer, msg, bx + 30, by + 5, msgColor);
        Ui.small(c, textRenderer, "Gesetzt: " + Ui.fmt(totalBet()) + " · Chip: " + chip
                + " · Auszahlungsquote ≈ 97,3 % (Europäisch, 1 Null)", width / 2, top + 203, 0xFFE0C060, 0.65f);
    }
}
