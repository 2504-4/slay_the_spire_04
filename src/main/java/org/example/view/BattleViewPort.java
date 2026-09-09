package org.example.view;

import org.example.model.BattleSnapshot;
import org.example.model.Card;

import java.util.List;
import java.util.function.Consumer;

/** UI boundary consumed by the controller, independent of JavaFX. */
public interface BattleViewPort {
    void setOnStart(Runnable action);
    void setOnEndTurn(Runnable action);
    void setOnCardPlayed(Consumer<Integer> action);
    void setOnDrawPileViewed(Runnable action);
    void setOnDiscardPileViewed(Runnable action);
    void render(BattleSnapshot state);
    void showPile(String title, List<Card> cards);
}
