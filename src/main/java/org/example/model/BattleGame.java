package org.example.model;

/**
 * Application-facing contract for one battle.
 *
 * <p>Controllers and other clients depend on this stable contract rather than
 * a particular rules implementation.</p>
 */
public interface BattleGame {
    /** Resets and starts a new battle. */
    void start();

    /** Attempts to play the card currently at the supplied hand index. */
    void playCard(int handIndex);

    /** Resolves the end of the player's turn. */
    void endTurn();

    /** Returns an immutable view of the current battle state. */
    BattleSnapshot snapshot();
}
