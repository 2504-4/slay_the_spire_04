package org.example.model;

/** Immutable card data. This class deliberately has no UI dependency. */
public record Card(CardType type, String name, int cost, int value, String description) {
    public static Card attack() {
        return new Card(CardType.ATTACK, "打击", 1, 8, "造成 8 点伤害");
    }

    public static Card defend() {
        return new Card(CardType.DEFEND, "防御", 1, 6, "获得 6 点格挡");
    }
}
