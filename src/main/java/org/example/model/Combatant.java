package org.example.model;

/** Mutable combat unit, owned only by BattleGame. */
final class Combatant {
    private static final int MINIMUM_VALUE = 0;

    private final int maxHealth;
    private int health;
    private int block;

    Combatant(int maximumHealth) {
        this.maxHealth = maximumHealth;
        this.health = maximumHealth;
    }

    void reset() {
        health = maxHealth;
        block = MINIMUM_VALUE;
    }

    void gainBlock(int blockAmount) {
        block += blockAmount;
    }

    void clearBlock() {
        block = MINIMUM_VALUE;
    }

    void takeDamage(int damageAmount) {
        int unblockedDamage = Math.max(MINIMUM_VALUE, damageAmount - block);
        block = Math.max(MINIMUM_VALUE, block - damageAmount);
        health = Math.max(MINIMUM_VALUE, health - unblockedDamage);
    }

    boolean isDead() {
        return health <= MINIMUM_VALUE;
    }

    int health() {
        return health;
    }

    int maxHealth() {
        return maxHealth;
    }

    int block() {
        return block;
    }
}