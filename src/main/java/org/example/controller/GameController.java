package org.example.controller;

import org.example.model.BattleGame;
import org.example.view.BattleViewPort;

/** Coordinates UI actions and pure game rules. */
public final class GameController {
    private final BattleGame game;
    private final BattleViewPort view;

    /**
     * Wires a game-use-case port to a view port. Concrete implementations are
     * selected by the composition root, not by this controller.
     */
    public GameController(BattleGame game, BattleViewPort view) {
        this.game = game;
        this.view = view;
        view.setOnStart(this::startGame);
        view.setOnEndTurn(this::endTurn);
        view.setOnCardPlayed(this::playCard);
        view.setOnDrawPileViewed(this::showDrawPile);
        view.setOnDiscardPileViewed(this::showDiscardPile);
        render();
    }

    private void startGame() { game.start(); render(); }
    private void playCard(int index) { game.playCard(index); render(); }
    private void endTurn() { game.endTurn(); render(); }
    private void showDrawPile() { view.showPile("抽牌堆", game.snapshot().drawPile()); }
    private void showDiscardPile() { view.showPile("弃牌堆", game.snapshot().discardPile()); }
    private void render() { view.render(game.snapshot()); }
}
