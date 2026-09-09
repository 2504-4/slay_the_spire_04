package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Single-fight rules. It is pure Java: run GameConsoleDemo without JavaFX.
 */
/** Default in-memory implementation of the single-battle rules. */
public final class DefaultBattleGame implements BattleGame {
    private static final int PLAYER_HEALTH = 40;
    private static final int MONSTER_HEALTH = 36;
    private static final int MONSTER_DAMAGE = 7;
    private static final int MONSTER_STRENGTH_AMOUNT = 3;
    private static final int MONSTER_BLOCK_AMOUNT = 6;
    private static final int MAX_ENERGY = 3;
    private static final int HAND_SIZE = 5;

    private final Combatant player = new Combatant(PLAYER_HEALTH);
    private final Combatant monster = new Combatant(MONSTER_HEALTH);
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> drawPile = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();
    private final Random random = new Random();
    private GameStatus status = GameStatus.READY;
    private int energy;
    private int turn;
    private int monsterDamage = MONSTER_DAMAGE;
    private MonsterAction monsterAction = MonsterAction.ATTACK;
    private String message = "点击开始战斗";

    public void start() {
        player.reset();
        monster.reset();
        hand.clear();
        drawPile.clear();
        discardPile.clear();
        createStartingDeck();
        shuffleDrawPile();
        status = GameStatus.PLAYING;
        turn = 1;
        monsterDamage = MONSTER_DAMAGE;
        monsterAction = MonsterAction.ATTACK;
        beginPlayerTurn("第 1 回合：选择卡牌");
    }

    /** Plays a card in the current hand. Invalid calls do not change combat state. */
    public void playCard(int handIndex) {
        if (status != GameStatus.PLAYING) {
            message = "战斗尚未开始或已经结束";
            return;
        }
        if (handIndex < 0 || handIndex >= hand.size()) {
            message = "无效的卡牌";
            return;
        }
        Card card = hand.get(handIndex);
        if (energy < card.cost()) {
            message = "能量不足";
            return;
        }
        energy -= card.cost();
        hand.remove(handIndex);
        discardPile.add(card);
        if (card.type() == CardType.ATTACK) {
            monster.takeDamage(card.value());
            message = "你使用打击，造成 " + card.value() + " 点伤害";
            if (monster.isDead()) {
                status = GameStatus.VICTORY;
                message = "胜利！怪物被击败";
            }
        } else {
            player.gainBlock(card.value());
            message = "你使用防御，获得 " + card.value() + " 点格挡";
        }
    }

    /** Ends the player phase, resolves one monster attack, then begins a new turn. */
    public void endTurn() {
        if (status != GameStatus.PLAYING) {
            return;
        }
        discardHand();
        // Monster block protects it during the player's turn and expires when that turn ends.
        monster.clearBlock();
        String monsterResult = resolveMonsterAction();
        player.clearBlock();
        if (player.isDead()) {
            status = GameStatus.DEFEAT;
            message = "失败！你被怪物击败";
            return;
        }
        turn++;
        chooseNextMonsterAction();
        beginPlayerTurn(monsterResult + "。第 " + turn + " 回合");
    }

    public BattleSnapshot snapshot() {
        return new BattleSnapshot(status, player.health(), player.maxHealth(), player.block(),
                monster.health(), monster.maxHealth(), monster.block(), energy, intentDamage(),
                intentDescription(), turn, message, List.copyOf(hand), drawPile.size(), discardPile.size(),
                List.copyOf(drawPile), List.copyOf(discardPile));
    }

    private void beginPlayerTurn(String turnMessage) {
        energy = MAX_ENERGY;
        drawCards(HAND_SIZE);
        message = turnMessage;
    }

    /** The fixed ten-card starting deck: six attacks and four defenses. */
    private void createStartingDeck() {
        for (int i = 0; i < 4; i++) {
            drawPile.add(Card.attack());
            drawPile.add(Card.defend());
        }
        drawPile.add(Card.attack());
        drawPile.add(Card.attack());
    }

    private void drawCards(int count) {
        if (drawPile.size() < count) {
            drawPile.addAll(discardPile);
            discardPile.clear();
            shuffleDrawPile();
        }
        int actualCount = Math.min(count, drawPile.size());
        for (int i = 0; i < actualCount; i++) {
            hand.add(drawPile.remove(drawPile.size() - 1));
        }
    }

    private void discardHand() {
        discardPile.addAll(hand);
        hand.clear();
    }

    private void shuffleDrawPile() {
        Collections.shuffle(drawPile, random);
    }

    private String resolveMonsterAction() {
        return switch (monsterAction) {
            case ATTACK -> {
                player.takeDamage(monsterDamage);
                yield "怪物攻击了 " + monsterDamage + " 点";
            }
            case STRENGTHEN -> {
                monsterDamage += MONSTER_STRENGTH_AMOUNT;
                yield "怪物强化攻击，之后的攻击提高 " + MONSTER_STRENGTH_AMOUNT + " 点";
            }
            case DEFEND -> {
                monster.gainBlock(MONSTER_BLOCK_AMOUNT);
                yield "怪物获得 " + MONSTER_BLOCK_AMOUNT + " 点格挡";
            }
        };
    }

    private void chooseNextMonsterAction() {
        MonsterAction[] actions = MonsterAction.values();
        monsterAction = actions[random.nextInt(actions.length)];
    }

    private int intentDamage() {
        return monsterAction == MonsterAction.ATTACK ? monsterDamage : 0;
    }

    private String intentDescription() {
        return switch (monsterAction) {
            case ATTACK -> MonsterAction.ATTACK.displayName() + " " + monsterDamage;
            case STRENGTHEN -> MonsterAction.STRENGTHEN.displayName()
                    + "（之后攻击 +" + MONSTER_STRENGTH_AMOUNT + "）";
            case DEFEND -> MonsterAction.DEFEND.displayName()
                    + " " + MONSTER_BLOCK_AMOUNT;
        };
    }
}
