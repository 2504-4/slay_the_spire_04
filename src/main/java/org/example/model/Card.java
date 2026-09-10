package org.example.model;

/** Immutable card data. This class deliberately has no UI dependency. */
public record Card(CardType type, String name, int cost, int value, String description) {
    private static final int STANDARD_CARD_COST = 1;
    private static final int ATTACK_DAMAGE = 8;
    private static final int DEFEND_BLOCK = 6;

    public static Card attack() {
        return new Card(CardType.ATTACK, "打击", STANDARD_CARD_COST, ATTACK_DAMAGE,
                "造成 " + ATTACK_DAMAGE + " 点伤害");
    }

    public static Card defend() {
        return new Card(CardType.DEFEND, "防御", STANDARD_CARD_COST, DEFEND_BLOCK,
                "获得 " + DEFEND_BLOCK + " 点格挡");
    }
}
