package org.example.model;

/** A small standalone proof that the model runs without JavaFX. */
public final class GameConsoleDemo {
    public static void main(String[] args) {
        BattleGame game = new DefaultBattleGame();
        game.start();
        game.playCard(0);
        game.playCard(0);
        BattleSnapshot state = game.snapshot();
        System.out.printf("玩家: %d/%d, 怪物: %d/%d, 能量: %d%n",
                state.playerHealth(), state.playerMaxHealth(),
                state.monsterHealth(), state.monsterMaxHealth(), state.energy());
    }
}
