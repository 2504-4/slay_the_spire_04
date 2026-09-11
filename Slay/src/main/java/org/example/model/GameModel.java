package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameModel {

    // ==================== 卡牌基类 ====================
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

    // ==================== 打击卡 ====================
    public class StrikeCard extends Card {
        public static final int DAMAGE = 6;

        public StrikeCard() {
            super("打击", "造成 " + DAMAGE + " 点伤害", 1, CardType.ATTACK);
        }

        @Override
        public void play(Player player, Enemy enemy) {
            enemy.takeDamage(DAMAGE);
        }
    }

    // ==================== 防御卡 ====================
    public class DefendCard extends Card {
        public static final int BLOCK = 5;

        public DefendCard() {
            super("防御", "获得 " + BLOCK + " 点格挡", 1, CardType.SKILL);
        }

        @Override
        public void play(Player player, Enemy enemy) {
            player.gainBlock(BLOCK);
        }
    }

    // ==================== 玩家 ====================
    public class Player {
        public static final int MAX_HAND_SIZE = 10;
        public static final int DRAW_PER_TURN = 5;

        private final int maxHp;
        private int hp;
        private int block;
        private int energy;
        private final int maxEnergy;
        private final List<Card> deck;         // 卡组模板（用于展示"卡组总数"）
        private final List<Card> hand;
        private final List<Card> discardPile;
        private final List<Card> drawPile;

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

            // 初始卡组 10 张：5 打击 + 5 防御
            for (int i = 0; i < 5; i++) deck.add(new StrikeCard());
            for (int i = 0; i < 5; i++) deck.add(new DefendCard());
            drawPile.addAll(deck);
            Collections.shuffle(drawPile);
        }

        public void startTurn() {
            energy = maxEnergy;
            block = 0;  // 回合开始时清空上一回合的格挡
            for (int i = 0; i < DRAW_PER_TURN; i++) drawCard();
        }

        public void drawCard() {
            if (hand.size() >= MAX_HAND_SIZE) return;         // 手牌上限
            if (drawPile.isEmpty()) {
                if (discardPile.isEmpty()) return;             // 两个牌堆都空 → 无法抽牌
                drawPile.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(drawPile);
            }
            hand.add(drawPile.remove(0));
        }

        /**
         * 打出一张手牌。
         * @return true 表示成功；false 表示 card 不在手牌中或能量不足。
         */
        public boolean playCard(Card card, Enemy enemy) {
            if (card == null) return false;
            if (!hand.contains(card)) return false;            // 必须在手牌里，防止复制卡漏洞
            if (energy < card.getCost()) return false;

            energy -= card.getCost();
            card.play(this, enemy);
            hand.remove(card);
            discardPile.add(card);
            return true;
        }

        public void endTurn() {
            discardPile.addAll(hand);
            hand.clear();
        }

        /** @return 实际扣到 HP 的伤害（格挡吸收后剩余） */
        public int takeDamage(int damage) {
            if (block > 0) {
                int blocked = Math.min(block, damage);
                block -= blocked;
                damage -= blocked;
            }
            if (damage > 0) hp = Math.max(0, hp - damage);
            return damage;
        }

        public void gainBlock(int amount) {
            block += amount;
        }

        public boolean isAlive() { return hp > 0; }

        // ===== Getters =====
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

    // ==================== 敌人 ====================
    public class Enemy {
        /** 敌人下回合的意图 */
        public enum Intent {
            ATTACK("攻击"),
            DEFEND("防御"),
            ATTACK_DEFEND("攻防");

            public final String label;
            Intent(String label) { this.label = label; }
        }

        private final String name;
        private final int maxHp;
        private int hp;
        private int block;
        private final int attackDamage;
        private final int defendBlock;
        private Intent nextIntent;

        public Enemy(String name, int maxHp, int attackDamage) {
            this.name = name;
            this.maxHp = maxHp;
            this.hp = maxHp;
            this.block = 0;
            this.attackDamage = attackDamage;
            this.defendBlock = 6;
            this.nextIntent = Intent.ATTACK;   // 第一回合固定攻击，让玩家能预判
        }

        /** @return 实际扣到 HP 的伤害 */
        public int takeDamage(int damage) {
            if (block > 0) {
                int blocked = Math.min(block, damage);
                block -= blocked;
                damage -= blocked;
            }
            if (damage > 0) hp = Math.max(0, hp - damage);
            return damage;
        }

        /** 敌人执行当前意图（不消耗能量，无手牌概念） */
        public void act(Player player) {
            switch (nextIntent) {
                case ATTACK -> player.takeDamage(attackDamage);
                case DEFEND -> gainBlock(defendBlock);
                case ATTACK_DEFEND -> {
                    player.takeDamage(attackDamage / 2);
                    gainBlock(defendBlock / 2);
                }
            }
        }

        /** 敌人回合开始：清空上一轮的格挡（格挡只持续一个玩家回合） */
        public void startTurn() {
            block = 0;
        }

        /** 敌人回合结束：只决定下回合意图；格挡保留到玩家回合用于吸收伤害 */
        public void endTurn() {
            nextIntent = rollNextIntent();
        }

        private Intent rollNextIntent() {
            // 简单 AI：70% 攻击、20% 防御、10% 攻防
            double r = Math.random();
            if (r < 0.70) return Intent.ATTACK;
            if (r < 0.90) return Intent.DEFEND;
            return Intent.ATTACK_DEFEND;
        }

        public void gainBlock(int amount) { block += amount; }
        public boolean isAlive() { return hp > 0; }

        /** 给玩家看的意图数值描述 */
        public String getIntentDescription() {
            return switch (nextIntent) {
                case ATTACK -> "⚔ " + attackDamage;
                case DEFEND -> "🛡 " + defendBlock;
                case ATTACK_DEFEND -> "⚔ " + (attackDamage / 2) + " 🛡 " + (defendBlock / 2);
            };
        }

        // ===== Getters =====
        public String getName() { return name; }
        public int getHp() { return hp; }
        public int getMaxHp() { return maxHp; }
        public int getBlock() { return block; }
        public int getAttackDamage() { return attackDamage; }
        public Intent getNextIntent() { return nextIntent; }
    }
}
