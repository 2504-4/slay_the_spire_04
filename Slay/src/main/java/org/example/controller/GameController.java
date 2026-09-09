// controller/GameController.java
package org.example.controller;

import org.example.model.Gamemodel;

public class GameController {
    private Gamemodel model;
    private Gamemodel.Player player;
    private Gamemodel.Enemy enemy;
    private boolean gameOver;
    private boolean playerTurn;
    private String message;

    public GameController() {
        model = new Gamemodel();
        player = model.new Player();
        enemy = model.new Enemy("打击者", 50, 8);
        gameOver = false;
        playerTurn = true;
        message = "游戏开始！";
    }

    public void startGame() {
        player.startTurn();
        message = "你遇到了敌人：" + enemy.getName();
    }

    public boolean playCard(int handIndex) {
        if (!playerTurn || gameOver) return false;

        if (handIndex >= 0 && handIndex < player.getHand().size()) {
            Gamemodel.Card card = player.getHand().get(handIndex);
            boolean played = player.playCard(card, enemy);

            if (played) {
                message = "你使用了" + card.getName();

                if (!enemy.isAlive()) {
                    gameOver = true;
                    message = "你击败了敌人！胜利！";
                    return true;
                }
                return true;
            } else {
                message = "能量不足！";
            }
        }
        return false;
    }

    public void endTurn() {
        if (!playerTurn || gameOver) return;

        playerTurn = false;

        // 敌人回合 - 先让敌人攻击，此时格挡还在
        if (enemy.isAlive()) {
            int damage = enemy.getAttackDamage();
            enemy.attack(player);
            message = "敌人攻击了你，造成" + damage + "点伤害";

            if (!player.isAlive()) {
                gameOver = true;
                message = "你被击败了...";
                return;
            }
        }

        // 清理玩家手牌和格挡
        player.endTurn();

        // 新回合
        playerTurn = true;
        player.startTurn();
        message = "新回合开始！";
    }

    public boolean canPlayCard(Gamemodel.Card card) {
        return playerTurn && !gameOver && player.getEnergy() >= card.getCost();
    }

    // Getters
    public Gamemodel.Player getPlayer() { return player; }
    public Gamemodel.Enemy getEnemy() { return enemy; }
    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerTurn() { return playerTurn; }
    public String getMessage() { return message; }
}