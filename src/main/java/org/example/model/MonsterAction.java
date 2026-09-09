package org.example.model;

/** The action the monster will resolve when the player ends the current turn. */
public enum MonsterAction {
    ATTACK("攻击"),
    STRENGTHEN("强化攻击"),
    DEFEND("获得防御");

    private final String displayName;

    MonsterAction(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
