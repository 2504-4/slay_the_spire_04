package org.example.model;

/** Mutable combat unit, owned only by BattleGame. */
final class Combatant {
    private final int maxHealth;
    private int health;
    private int block;

    Combatant(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    void reset() { health = maxHealth; block = 0; }
    void gainBlock(int amount) { block += amount; }
    void clearBlock() { block = 0; }
    void takeDamage(int amount) {
        int remaining = Math.max(0, amount - block);
        block = Math.max(0, block - amount);
        health = Math.max(0, health - remaining);
    }
    boolean isDead() { return health <= 0; }
    int health() { return health; }
    int maxHealth() { return maxHealth; }
    int block() { return block; }
}
