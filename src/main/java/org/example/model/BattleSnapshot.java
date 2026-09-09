package org.example.model;

import java.util.List;

/** Read-only state sent from the model to other layers. */
public record BattleSnapshot(
        GameStatus status,
        int playerHealth, int playerMaxHealth, int playerBlock,
        int monsterHealth, int monsterMaxHealth, int monsterBlock,
        int energy, int monsterIntentDamage, String monsterIntent, int turn,
        String message, List<Card> hand,
        int drawPileSize, int discardPileSize,
        List<Card> drawPile, List<Card> discardPile) {
}
