package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Default in-memory implementation of the rules for one battle. */
public final class DefaultBattleGame implements BattleGame {
    private static final int PLAYER_HEALTH = 40;
    private static final int MONSTER_HEALTH = 36;
    private static final int MONSTER_DAMAGE = 7;
    private static final int MONSTER_STRENGTH_AMOUNT = 3;
    private static final int MONSTER_BLOCK_AMOUNT = 6;
    private static final int MAX_ENERGY = 3;
    private static final int HAND_SIZE = 5;
    private static final int FIRST_TURN_NUMBER = 1;
    private static final int STARTING_CARD_PAIR_COUNT = 4;
    private static final int FIRST_HAND_CARD_INDEX = 0;
    private static final int LAST_CARD_OFFSET = 1;
    private static final int NO_INTENT_DAMAGE = 0;
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
    private String message = "Start a battle.";
    @Override public void start() {
        player.reset(); monster.reset(); hand.clear(); drawPile.clear(); discardPile.clear();
        createStartingDeck(); shuffleDrawPile(); status = GameStatus.PLAYING;
        turn = FIRST_TURN_NUMBER; monsterDamage = MONSTER_DAMAGE; monsterAction = MonsterAction.ATTACK;
        beginPlayerTurn("Turn " + FIRST_TURN_NUMBER + ": choose a card.");
    }
    @Override public void playCard(int handCardIndex) {
        if (!canPlayCard(handCardIndex)) return;
        Card playedCard = hand.remove(handCardIndex);
        energy -= playedCard.cost(); discardPile.add(playedCard);
        if (playedCard.type() == CardType.ATTACK) resolveAttack(playedCard); else resolveDefend(playedCard);
    }
    @Override public void endTurn() {
        if (status != GameStatus.PLAYING) return;
        discardPile.addAll(hand); hand.clear(); monster.clearBlock();
        String result = resolveMonsterAction(); player.clearBlock();
        if (player.isDead()) { status = GameStatus.DEFEAT; message = "Defeat."; return; }
        turn++; chooseNextMonsterAction(); beginPlayerTurn(result + " Turn " + turn + ".");
    }
    @Override public BattleSnapshot snapshot() {
        return new BattleSnapshot(status, player.health(), player.maxHealth(), player.block(), monster.health(), monster.maxHealth(), monster.block(), energy, intentDamage(), intentDescription(), turn, message, List.copyOf(hand), drawPile.size(), discardPile.size(), List.copyOf(drawPile), List.copyOf(discardPile));
    }
    private boolean canPlayCard(int handCardIndex) {
        if (status != GameStatus.PLAYING) { message = "Battle is not active."; return false; }
        if (handCardIndex < FIRST_HAND_CARD_INDEX || handCardIndex >= hand.size()) { message = "Invalid card."; return false; }
        if (energy < hand.get(handCardIndex).cost()) { message = "Not enough energy."; return false; }
        return true;
    }
    private void resolveAttack(Card playedCard) { monster.takeDamage(playedCard.value()); message = "Attack dealt " + playedCard.value() + " damage."; if (monster.isDead()) { status = GameStatus.VICTORY; message = "Victory."; } }
    private void resolveDefend(Card playedCard) { player.gainBlock(playedCard.value()); message = "Defense gained " + playedCard.value() + " block."; }
    private void beginPlayerTurn(String turnMessage) { energy = MAX_ENERGY; drawCards(HAND_SIZE); message = turnMessage; }
    private void createStartingDeck() { for (int pairIndex = 0; pairIndex < STARTING_CARD_PAIR_COUNT; pairIndex++) { drawPile.add(Card.attack()); drawPile.add(Card.defend()); } drawPile.add(Card.attack()); drawPile.add(Card.attack()); }
    private void drawCards(int requestedCardCount) { if (drawPile.size() < requestedCardCount) { drawPile.addAll(discardPile); discardPile.clear(); shuffleDrawPile(); } int drawableCardCount = Math.min(requestedCardCount, drawPile.size()); for (int drawnCardIndex = 0; drawnCardIndex < drawableCardCount; drawnCardIndex++) hand.add(drawPile.remove(drawPile.size() - LAST_CARD_OFFSET)); }
    private void shuffleDrawPile() { Collections.shuffle(drawPile, random); }
    private String resolveMonsterAction() { return switch (monsterAction) { case ATTACK -> { player.takeDamage(monsterDamage); yield "Monster attacked for " + monsterDamage + "."; } case STRENGTHEN -> { monsterDamage += MONSTER_STRENGTH_AMOUNT; yield "Monster strengthened."; } case DEFEND -> { monster.gainBlock(MONSTER_BLOCK_AMOUNT); yield "Monster gained block."; } }; }
    private void chooseNextMonsterAction() { MonsterAction[] availableActions = MonsterAction.values(); monsterAction = availableActions[random.nextInt(availableActions.length)]; }
    private int intentDamage() { return monsterAction == MonsterAction.ATTACK ? monsterDamage : NO_INTENT_DAMAGE; }
    private String intentDescription() { return switch (monsterAction) { case ATTACK -> monsterAction.displayName() + " " + monsterDamage; case STRENGTHEN -> monsterAction.displayName(); case DEFEND -> monsterAction.displayName() + " " + MONSTER_BLOCK_AMOUNT; }; }
}