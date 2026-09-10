package org.example.controller;

import org.example.model.BattleGame;
import org.example.model.BattleSnapshot;
import org.example.model.Card;
import org.example.model.DefaultBattleGame;
import org.example.model.GameStatus;
import org.example.view.BattleViewPort;

import java.util.List;
import java.util.function.Consumer;

/** Verifies the controller can use a non-JavaFX implementation of its view port. */
public final class GameControllerSmokeTest {
    public static void main(String[] args) {
        FakeView view = new FakeView();
        BattleGame game = new DefaultBattleGame();
        new GameController(game, view);

        if (view.lastSnapshot.status() != GameStatus.READY) {
            throw new AssertionError("Controller did not render the initial state");
        }
        view.startHandler.run();
        if (view.lastSnapshot.status() != GameStatus.PLAYING || view.lastSnapshot.hand().size() != 5) {
            throw new AssertionError("Controller did not route the start action to the game");
        }
        System.out.println("GameController smoke test passed");
    }

    private static final class FakeView implements BattleViewPort {
        private Runnable startHandler = () -> { };
        private BattleSnapshot lastSnapshot;

        @Override public void setOnStart(Runnable startHandler) { this.startHandler = startHandler; }
        @Override public void setOnEndTurn(Runnable ignoredHandler) { }
        @Override public void setOnCardPlayed(Consumer<Integer> cardPlayedHandler) { }
        @Override public void setOnDrawPileViewed(Runnable ignoredHandler) { }
        @Override public void setOnDiscardPileViewed(Runnable ignoredHandler) { }
        @Override public void render(BattleSnapshot battleSnapshot) { lastSnapshot = battleSnapshot; }
        @Override public void showPile(String pileTitle, List<Card> pileCards) { }
    }
}
