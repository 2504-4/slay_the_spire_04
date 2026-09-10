package org.example.view;

import org.example.model.BattleSnapshot;
import org.example.model.Card;

import java.util.List;
import java.util.function.Consumer;

/** UI boundary consumed by the controller, independent of JavaFX. */
public interface BattleViewPort {
    void setOnStart(Runnable startHandler);
    void setOnEndTurn(Runnable endTurnHandler);
    void setOnCardPlayed(Consumer<Integer> cardPlayedHandler);
    void setOnDrawPileViewed(Runnable drawPileViewedHandler);
    void setOnDiscardPileViewed(Runnable discardPileViewedHandler);
    void render(BattleSnapshot battleSnapshot);
    void showPile(String pileTitle, List<Card> pileCards);
}