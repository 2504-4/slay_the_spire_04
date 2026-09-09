// org/example/model/Gamemodel.java
package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Gamemodel {
    // 卡牌基类
    public abstract class Card {
        protected String name;
        protected String description;
        protected int cost;
        protected CardType type;

        public enum CardType {
            ATTACK, SKILL
        }

        public Card(String name, String description, int cost, CardType type) {
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.type = type;
        }

        public abstract void play(Player player, Enemy enemy);

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getCost() { return cost; }
        public CardType getType() { return type; }
    }

    // 打击卡
    public class StrikeCard extends Card {
        public StrikeCard() {
            super("打击", "造成6点伤害", 1, CardType.ATTACK);
        }

        @Override
        public void play(Player player, Enemy enemy) {
            enemy.takeDamage(6);
        }
    }

    // 防御卡
    public class DefendCard extends Card {
        public DefendCard() {
            super("防御", "获得5点格挡", 1, CardType.SKILL);
        }

        @Override
        public void play(Player player, Enemy enemy) {
            player.gainBlock(5);
        }
    }

    // 玩家模型
    public class Player {
        private int maxHp;
        private int hp;
        private int block;
        private int energy;
        private int maxEnergy;
        private List<Card> deck;
        private List<Card> hand;
        private List<Card> discardPile;
        private List<Card> drawPile;

        public Player() {
            this.maxHp = 80;
            this.hp = maxHp;
            this.block = 0;
            this.maxEnergy = 3;
            this.energy = maxEnergy;
            this.deck = new ArrayList<>();
            this.hand = new ArrayList<>();
            this.discardPile = new ArrayList<>();
            this.drawPile = new ArrayList<>();

            // 初始卡组：5张打击，4张防御
            for (int i = 0; i < 5; i++) {
                deck.add(new StrikeCard());
            }
            for (int i = 0; i < 4; i++) {
                deck.add(new DefendCard());
            }
            drawPile.addAll(deck);
            Collections.shuffle(drawPile);
        }

        public void startTurn() {
            energy = maxEnergy;
            block = 0;  // 回合开始时清空格挡

            // 抽牌阶段
            for (int i = 0; i < 5; i++) {
                drawCard();
            }
        }

        public void drawCard() {
            if (drawPile.isEmpty()) {
                drawPile.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(drawPile);
            }

            if (!drawPile.isEmpty()) {
                Card card = drawPile.remove(0);
                hand.add(card);
            }
        }

        public boolean playCard(Card card, Enemy enemy) {
            if (energy >= card.getCost()) {
                energy -= card.getCost();
                card.play(this, enemy);
                hand.remove(card);
                discardPile.add(card);
                return true;
            }
            return false;
        }

        public void endTurn() {
            discardPile.addAll(hand);
            hand.clear();
            block = 0;  // 回合结束时清空格挡
        }

        public void takeDamage(int damage) {
            // 先用格挡吸收伤害
            if (block > 0) {
                int blocked = Math.min(block, damage);
                block -= blocked;
                damage -= blocked;
                System.out.println("格挡吸收了 " + blocked + " 点伤害");
            }

            // 剩余伤害扣血
            if (damage > 0) {
                hp -= damage;
                System.out.println("受到 " + damage + " 点伤害");
            }

            if (hp < 0) hp = 0;
            System.out.println("当前HP: " + hp + ", 格挡: " + block);
        }

        public void gainBlock(int amount) {
            block += amount;
            System.out.println("获得 " + amount + " 点格挡，当前格挡: " + block);
        }

        public boolean isAlive() { return hp > 0; }

        // Getters
        public int getHp() { return hp; }
        public int getMaxHp() { return maxHp; }
        public int getBlock() { return block; }
        public int getEnergy() { return energy; }
        public int getMaxEnergy() { return maxEnergy; }
        public List<Card> getHand() { return hand; }
        public List<Card> getDeck() { return deck; }
        public List<Card> getDrawPile() { return drawPile; }
        public List<Card> getDiscardPile() { return discardPile; }
    }

    // 敌人模型
    public class Enemy {
        private String name;
        private int maxHp;
        private int hp;
        private int block;
        private int attackDamage;

        public Enemy(String name, int maxHp, int attackDamage) {
            this.name = name;
            this.maxHp = maxHp;
            this.hp = maxHp;
            this.block = 0;
            this.attackDamage = attackDamage;
        }

        public void takeDamage(int damage) {
            // 先用格挡吸收伤害
            if (block > 0) {
                int blocked = Math.min(block, damage);
                block -= blocked;
                damage -= blocked;
                System.out.println("敌人格挡吸收了 " + blocked + " 点伤害");
            }

            // 剩余伤害扣血
            if (damage > 0) {
                hp -= damage;
                System.out.println("敌人受到 " + damage + " 点伤害");
            }

            if (hp < 0) hp = 0;
            System.out.println("敌人当前HP: " + hp + ", 格挡: " + block);
        }

        public void attack(Player player) {
            player.takeDamage(attackDamage);
        }

        public void gainBlock(int amount) {
            block += amount;
        }

        public boolean isAlive() { return hp > 0; }

        // Getters
        public String getName() { return name; }
        public int getHp() { return hp; }
        public int getMaxHp() { return maxHp; }
        public int getBlock() { return block; }
        public int getAttackDamage() { return attackDamage; }
    }
}