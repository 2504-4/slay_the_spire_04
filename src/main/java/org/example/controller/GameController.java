package org.example.controller;

import org.example.model.BattleGame;
import org.example.view.BattleViewPort;

/** Coordinates UI actions and pure game rules. */
public final class GameController {
    private static final String DRAW_PILE_TITLE = "抽牌堆";
    private static final String DISCARD_PILE_TITLE = "弃牌堆";

    private final BattleGame battleGame;
    private final BattleViewPort battleView;

    /** Wires a game-use-case port to a view port selected by the composition root. */
    public GameController(BattleGame battleGame, BattleViewPort battleView) {
        this.battleGame = battleGame;
        this.battleView = battleView;
        bindViewHandlers();
        renderBattle();
    }

    private void bindViewHandlers() {
        battleView.setOnStart(this::startGame);
        battleView.setOnEndTurn(this::endTurn);
        battleView.setOnCardPlayed(this::playCard);
        battleView.setOnDrawPileViewed(this::showDrawPile);
        battleView.setOnDiscardPileViewed(this::showDiscardPile);
    }

    private void startGame() {
        battleGame.start();
        renderBattle();
    }

    private void playCard(int handCardIndex) {
        battleGame.playCard(handCardIndex);
        renderBattle();
    }

    private void endTurn() {
        battleGame.endTurn();
        renderBattle();
    }

    private void showDrawPile() {
        battleView.showPile(DRAW_PILE_TITLE, battleGame.snapshot().drawPile());
    }

    private void showDiscardPile() {
        battleView.showPile(DISCARD_PILE_TITLE, battleGame.snapshot().discardPile());
    }

    private void renderBattle() {
        battleView.render(battleGame.snapshot());
    }
}