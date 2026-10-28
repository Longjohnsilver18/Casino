package de.royalcasino;

import net.minecraft.client.gui.DrawContext;

public class CasinoScreen extends CasinoBase {
    private GoldButton claimBtn;
    private String status = "";

    public CasinoScreen() {
        super("★ ROYAL CASINO ★", 260, 206);
    }

    @Override
    protected void build() {
        claimBtn = addDrawableChild(new GoldButton(left + 30, top + 48, pw - 60, 24, "", b -> {
            if (CoinStore.claim()) {
                status = "+" + Ui.fmt(CoinStore.DAILY) + " Coins erhalten!";
                Ui.winSound();
            }
            updateClaim();
        }, Ui.RED_LIGHT));
        addDrawableChild(new GoldButton(left + 30, top + 84, pw - 60, 22, "Slots", b -> client.setScreen(new SlotsScreen())));
        addDrawableChild(new GoldButton(left + 30, top + 110, pw - 60, 22, "Blackjack", b -> client.setScreen(new BlackjackScreen())));
        addDrawableChild(new GoldButton(left + 30, top + 136, pw - 60, 22, "Roulette", b -> client.setScreen(new RouletteScreen())));
        addDrawableChild(new GoldButton(left + 30, top + 170, pw - 60, 18, "Schließen", b -> close()));
        updateClaim();
    }

    private void updateClaim() {
        if (claimBtn == null) return;
        if (CoinStore.canClaim()) {
            claimBtn.setMessage(net.minecraft.text.Text.literal("Tägliche " + Ui.fmt(CoinStore.DAILY) + " Coins abholen"));
            claimBtn.active = true;
        } else {
            long s = CoinStore.millisUntilClaim() / 1000;
            claimBtn.setMessage(net.minecraft.text.Text.literal(
                    String.format("Nächste Abholung in %02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60)));
            claimBtn.active = false;
        }
    }

    @Override
    public void tick() {
        updateClaim();
    }

    @Override
    protected void draw(DrawContext c, int mx, int my, float delta) {
        if (!status.isEmpty()) {
            c.drawCenteredTextWithShadow(textRenderer, status, width / 2, top + 74, 0xFFFFFFFF);
        }
    }
}
