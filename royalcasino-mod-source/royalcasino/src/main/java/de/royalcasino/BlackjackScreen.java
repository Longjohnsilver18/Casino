package de.royalcasino;

import net.minecraft.client.gui.DrawContext;

import java.util.*;

/**
 * 6 Decks, Dealer steht auf allen 17, Blackjack zahlt 3:2, Verdoppeln auf jede Starthand.
 * Kein Split, keine Versicherung. Hausvorteil ca. 0,6 % bei optimaler Strategie.
 */
public class BlackjackScreen extends CasinoBase {
    enum State { BETTING, PLAYER, DEALER }

    record Card(int rank, int suit) {}

    static final int[] BETS = {10, 50, 100, 250, 500, 1000};
    static final String[] SUITS = {"♥", "♦", "♣", "♠"};
    static final String[] RANKS = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    static int bet = 10;
    static final List<Card> shoe = new ArrayList<>();

    private final Random rnd = new Random();
    private final List<Card> player = new ArrayList<>(), dealer = new ArrayList<>();
    private final List<GoldButton> chipBtns = new ArrayList<>();
    private State state = State.BETTING;
    private int wager, delay;
    private String msg = "Setze deinen Einsatz.";
    private int msgColor = Ui.GOLD;

    public BlackjackScreen() {
        super("♠ BLACKJACK ♠", 330, 232);
        if (shoe.isEmpty()) newShoe();
    }

    private void newShoe() {
        shoe.clear();
        for (int d = 0; d < 6; d++)
            for (int s = 0; s < 4; s++)
                for (int r = 1; r <= 13; r++) shoe.add(new Card(r, s));
        Collections.shuffle(shoe, rnd);
    }

    private Card draw() {
        if (shoe.isEmpty()) newShoe();
        return shoe.remove(shoe.size() - 1);
    }

    static int value(List<Card> h) {
        int t = 0, aces = 0;
        for (Card c : h) {
            if (c.rank() == 1) { aces++; t += 11; } else t += Math.min(c.rank(), 10);
        }
        while (t > 21 && aces > 0) { t -= 10; aces--; }
        return t;
    }

    @Override
    protected void build() {
        chipBtns.clear();
        if (state == State.BETTING) {
            for (int i = 0; i < BETS.length; i++) {
                final int v = BETS[i];
                GoldButton b = addDrawableChild(new GoldButton(left + 33 + i * 44, top + 168, 42, 18, String.valueOf(v), x -> {
                    bet = v;
                    for (int k = 0; k < chipBtns.size(); k++) chipBtns.get(k).highlight = BETS[k] == bet;
                }));
                b.highlight = v == bet;
                chipBtns.add(b);
            }
            addDrawableChild(new GoldButton(left + 60, top + 192, 130, 22, "AUSTEILEN", b -> deal(), Ui.RED_LIGHT));
            addDrawableChild(new GoldButton(left + 200, top + 192, 70, 22, "Zurück", b -> client.setScreen(new CasinoScreen())));
        } else if (state == State.PLAYER) {
            addDrawableChild(new GoldButton(left + 30, top + 176, 80, 22, "Karte", b -> hit()));
            addDrawableChild(new GoldButton(left + 125, top + 176, 80, 22, "Halten", b -> stand()));
            GoldButton dbl = addDrawableChild(new GoldButton(left + 220, top + 176, 80, 22, "Verdoppeln", b -> doubleDown()));
            dbl.active = player.size() == 2 && CoinStore.coins() >= wager;
        }
    }

    private void deal() {
        if (!CoinStore.take(bet)) {
            msg = "Nicht genug Coins!";
            msgColor = 0xFFFF5555;
            return;
        }
        if (shoe.size() < 78) newShoe();
        wager = bet;
        player.clear();
        dealer.clear();
        player.add(draw());
        dealer.add(draw());
        player.add(draw());
        dealer.add(draw());
        boolean pbj = value(player) == 21, dbj = value(dealer) == 21;
        if (pbj || dbj) {
            if (pbj && dbj) {
                CoinStore.give(wager);
                msg = "Beide Blackjack – Push.";
                msgColor = Ui.GOLD;
            } else if (pbj) {
                CoinStore.give(wager * 5L / 2);
                msg = "BLACKJACK! 3:2 → +" + Ui.fmt(wager * 3L / 2);
                msgColor = 0xFF55FF55;
                Ui.winSound();
            } else {
                msg = "Dealer hat Blackjack. -" + Ui.fmt(wager);
                msgColor = 0xFFFF8888;
            }
            state = State.BETTING;
        } else {
            msg = "Karte, Halten oder Verdoppeln?";
            msgColor = Ui.GOLD;
            state = State.PLAYER;
        }
        clearAndInit();
    }

    private void hit() {
        player.add(draw());
        int v = value(player);
        if (v > 21) {
            msg = "Bust mit " + v + "! -" + Ui.fmt(wager);
            msgColor = 0xFFFF8888;
            state = State.BETTING;
            clearAndInit();
        } else if (v == 21) {
            stand();
        } else {
            clearAndInit();
        }
    }

    private void stand() {
        state = State.DEALER;
        delay = 12;
        msg = "Dealer ist dran...";
        msgColor = Ui.GOLD;
        clearAndInit();
    }

    private void doubleDown() {
        if (!CoinStore.take(wager)) return;
        wager *= 2;
        player.add(draw());
        if (value(player) > 21) {
            msg = "Bust mit " + value(player) + "! -" + Ui.fmt(wager);
            msgColor = 0xFFFF8888;
            state = State.BETTING;
            clearAndInit();
        } else {
            stand();
        }
    }

    @Override
    public void tick() {
        if (state != State.DEALER) return;
        if (--delay > 0) return;
        if (value(dealer) < 17) {
            dealer.add(draw());
            delay = 14;
        } else {
            finishRound();
        }
    }

    private void finishRound() {
        int pv = value(player), dv = value(dealer);
        if (dv > 21 || pv > dv) {
            CoinStore.give(wager * 2L);
            msg = (dv > 21 ? "Dealer bust! " : pv + " schlägt " + dv + ". ") + "Gewinn +" + Ui.fmt(wager);
            msgColor = 0xFF55FF55;
            Ui.winSound();
        } else if (pv == dv) {
            CoinStore.give(wager);
            msg = "Push (" + pv + ") – Einsatz zurück.";
            msgColor = Ui.GOLD;
        } else {
            msg = "Dealer gewinnt mit " + dv + ". -" + Ui.fmt(wager);
            msgColor = 0xFFFF8888;
        }
        state = State.BETTING;
        clearAndInit();
    }

    private void drawCard(DrawContext c, int x, int y, Card card, boolean hidden) {
        c.fill(x, y, x + 30, y + 42, Ui.GOLD);
        if (hidden) {
            c.fill(x + 2, y + 2, x + 28, y + 40, Ui.RED_DARK);
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 4; j++)
                    if ((i + j) % 2 == 0) c.fill(x + 4 + i * 8, y + 4 + j * 9, x + 11 + i * 8, y + 12 + j * 9, Ui.GOLD_DARK);
            return;
        }
        c.fill(x + 1, y + 1, x + 29, y + 41, 0xFFF5EBD0);
        int col = card.suit() < 2 ? 0xFFC01010 : 0xFF111111;
        c.drawText(textRenderer, RANKS[card.rank()], x + 3, y + 3, col, false);
        c.getMatrices().push();
        c.getMatrices().translate(x + 8, y + 17, 0);
        c.getMatrices().scale(1.6f, 1.6f, 1f);
        c.drawText(textRenderer, SUITS[card.suit()], 0, 0, col, false);
        c.getMatrices().pop();
    }

    private void drawHand(DrawContext c, List<Card> hand, int y, boolean hideSecond) {
        int n = hand.size();
        int sx = left + (pw - (n * 34 - 4)) / 2;
        for (int i = 0; i < n; i++) drawCard(c, sx + i * 34, y, hand.get(i), hideSecond && i == 1);
    }

    @Override
    protected void draw(DrawContext c, int mx, int my, float delta) {
        boolean hide = state == State.PLAYER;
        if (!dealer.isEmpty()) {
            int dv = hide ? value(dealer.subList(0, 1)) : value(dealer);
            c.drawTextWithShadow(textRenderer, "Dealer: " + dv, left + 14, top + 36, 0xFFFFFFFF);
            drawHand(c, dealer, top + 48, hide);
        }
        if (!player.isEmpty()) {
            c.drawTextWithShadow(textRenderer, "Du: " + value(player), left + 14, top + 96, 0xFFFFFFFF);
            drawHand(c, player, top + 108, false);
        }
        String w = "Einsatz: " + Ui.fmt(state == State.BETTING ? bet : wager);
        c.drawTextWithShadow(textRenderer, w, left + pw - 14 - textRenderer.getWidth(w), top + 96, Ui.GOLD);
        c.drawCenteredTextWithShadow(textRenderer, msg, width / 2, top + 156, msgColor);
        Ui.small(c, textRenderer, "6 Decks · Blackjack 3:2 · Dealer steht auf 17 · Kein Split", width / 2, top + 220, 0xFFE0C060, 0.65f);
    }
}
