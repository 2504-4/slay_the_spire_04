package org.example.model;

/** Dependency-free smoke test; run as a normal Java main method. */
public final class BattleGameSmokeTest {
    public static void main(String[] args) {
        BattleGame game = new DefaultBattleGame();
        game.start();
        if (game.snapshot().status() != GameStatus.PLAYING || game.snapshot().hand().size() != 5) {
            throw new AssertionError("Game did not start correctly");
        }
        if (game.snapshot().drawPileSize() != 5 || game.snapshot().discardPileSize() != 0) {
            throw new AssertionError("Starting deck was not drawn into the expected piles");
        }
        int monsterHealth = game.snapshot().monsterHealth();
        int attackIndex = findAttackCard(game);
        game.playCard(attackIndex);
        if (game.snapshot().monsterHealth() >= monsterHealth) {
            throw new AssertionError("Attack card did not damage monster");
        }
        if (game.snapshot().discardPileSize() != 1) {
            throw new AssertionError("Played card was not moved to the discard pile");
        }

        game.endTurn();
        if (game.snapshot().hand().size() != 5 || game.snapshot().discardPileSize() != 5) {
            throw new AssertionError("Unused cards were not discarded at end of turn");
        }
        game.endTurn();
        if (game.snapshot().hand().size() != 5 || game.snapshot().drawPileSize() != 5
                || game.snapshot().discardPileSize() != 0) {
            throw new AssertionError("Discard pile was not shuffled back into draw pile");
        }
        System.out.println("BattleGame smoke test passed");
    }

    private static int findAttackCard(BattleGame game) {
        for (int i = 0; i < game.snapshot().hand().size(); i++) {
            if (game.snapshot().hand().get(i).type() == CardType.ATTACK) {
                return i;
            }
        }
        throw new AssertionError("Expected at least one attack in the starting hand");
    }
}
