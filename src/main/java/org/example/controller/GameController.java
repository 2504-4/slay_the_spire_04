package org.example.controller;

import org.example.model.GameModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 游戏流程控制器：负责串联 Model 的动作、维护战斗日志、判定胜负。
 * <p>
 * View 只调用本类的公共方法，不直接触碰 Model 的状态。
 */
public class GameController {
    private static final int MAX_LOG_LINES = 100;

    private final GameModel model;
    private final GameModel.Player player;
    /** 当前关卡敌人。关卡推进时替换，玩家与牌组状态保持不变。 */
    private GameModel.Enemy enemy;
    private final List<String> battleLog;
    private List<GameModel.Card> rewardChoices;

    private static final EnemyLevel[] LEVELS = {
            new EnemyLevel("第1关 小怪：史莱姆", 25, 4),
            new EnemyLevel("第2关 小怪：骷髅士兵", 40, 6),
            new EnemyLevel("第3关 小怪：哥布林队长", 60, 8),
            new EnemyLevel("第4关 最终BOSS：大魔王", 90, 12)
    };
    private int currentLevelIndex;

    private boolean gameOver;
    private boolean playerWin;
    private boolean playerTurn;
    private boolean rewardPending;

    public GameController() {
        this.model = new GameModel();
        this.player = model.new Player();
        this.currentLevelIndex = 0;
        this.enemy = createCurrentLevelEnemy();
        this.battleLog = new ArrayList<>();
        this.rewardChoices = Collections.emptyList();
        this.gameOver = false;
        this.playerWin = false;
        this.playerTurn = true;
        this.rewardPending = false;
        startGame();
    }

    /** 开局：初始化日志并让玩家进入第一回合 */
    private void startGame() {
        battleLog.clear();
        log("⚔ 你进入第 " + getCurrentLevel() + "/" + getTotalLevels()
                + " 关，遇到了 " + enemy.getName() + "！");
        player.startTurn();
        log("— 回合 1 开始：能量 " + player.getEnergy()
                + "，手牌 " + player.getHand().size() + " 张 —");
    }

    // ==================== 玩家动作 ====================

    /**
     * 打出手牌中第 handIndex 张卡。
     * @return true 表示成功打出。
     */
    public boolean playCard(int handIndex) {
        try {
            if (!playerTurn || gameOver || rewardPending) return false;
            if (handIndex < 0 || handIndex >= player.getHand().size()) return false;

            GameModel.Card card = player.getHand().get(handIndex);

            // 记录行动前状态，用于生成日志
            int enemyHpBefore = enemy.getHp();
            int enemyBlockBefore = enemy.getBlock();
            int playerBlockBefore = player.getBlock();

            boolean played = player.playCard(card, enemy);
            if (!played) {
                log("✖ 无法使用【" + card.getName() + "】");
                return false;
            }

            log("▶ 你使用了【" + card.getName() + "】");
            if (enemy.getBlock() < enemyBlockBefore) {
                log("   敌人格挡吸收了 " + (enemyBlockBefore - enemy.getBlock()) + " 点伤害");
            }
            if (enemy.getHp() < enemyHpBefore) {
                log("   对敌人造成 " + (enemyHpBefore - enemy.getHp()) + " 点伤害");
            }
            if (enemy.getBlock() > enemyBlockBefore) {
                log("   敌人获得 " + (enemy.getBlock() - enemyBlockBefore) + " 点格挡");
            }
            if (player.getBlock() > playerBlockBefore) {
                log("   你获得 " + (player.getBlock() - playerBlockBefore) + " 点格挡");
            }

            if (!enemy.isAlive()) onEnemyDefeated();
            return true;
        } catch (RuntimeException ex) {
            log("✖ 出牌异常: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /** 结束当前玩家回合：玩家弃牌 → 敌人行动 → 新回合开始 */
    public void endTurn() {
        try {
            if (!playerTurn || gameOver || rewardPending) return;

            // 1) 玩家回合结束
            player.endTurn();
            playerTurn = false;

            // 2) 敌人回合（若还活着）
            if (enemy.isAlive()) {
                enemyTurn();
            }

            // 3) 玩家死亡判定
            if (!player.isAlive()) {
                gameOver = true;
                playerWin = false;
                log("💀 你被击败了…");
                return;
            }

            // 4) 新回合开始
            beginNewTurn();
        } catch (RuntimeException ex) {
            log("✖ 回合结算异常: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** 敌人回合：清上回合格挡 → 按当前意图行动 → 决定下一回合意图 */
    private void enemyTurn() {
        GameModel.Enemy.Intent intent = enemy.getNextIntent();
        int playerHpBefore = player.getHp();
        int playerBlockBefore = player.getBlock();
        int enemyBlockLastRound = enemy.getBlock();

        log("— 敌方回合：" + enemy.getName() + " 意图【" + intent.label + "】 —");

        // 敌人回合开始：清空上回合残留的格挡
        enemy.startTurn();
        if (enemyBlockLastRound > 0) {
            log("   敌人上回合的 " + enemyBlockLastRound + " 点格挡消散");
        }

        enemy.act(player);

        if (player.getBlock() > playerBlockBefore) {
            // 敌人几乎不会给玩家加格挡，保留分支防止未来扩展漏日志
            log("   你获得 " + (player.getBlock() - playerBlockBefore) + " 点格挡");
        }
        if (player.getHp() < playerHpBefore) {
            log("   你受到 " + (playerHpBefore - player.getHp()) + " 点伤害");
        }
        if (player.getBlock() < playerBlockBefore) {
            log("   格挡吸收了 " + (playerBlockBefore - player.getBlock()) + " 点伤害");
        }
        if (enemy.getBlock() > 0) {
            log("   敌人获得 " + enemy.getBlock() + " 点格挡（将持续到你的回合结束）");
        }

        enemy.endTurn();
    }

    /** 玩家新回合开始：重置能量、清格挡、抽牌 */
    private void beginNewTurn() {
        playerTurn = true;
        player.startTurn();
        log("— 新回合开始：能量 " + player.getEnergy()
                + "，手牌 " + player.getHand().size() + " 张 —");
    }

    /** 击败小怪后先选择奖励；击败最后一关大魔王才判定整局胜利。 */
    private void onEnemyDefeated() {
        log("🎉 你击败了 " + enemy.getName() + "！");
        if (currentLevelIndex == LEVELS.length - 1) {
            gameOver = true;
            playerWin = true;
            log("🏆 大魔王已被击败，全部关卡通关！");
            return;
        }

        rewardChoices = createRewardChoices();
        rewardPending = true;
        log("🎁 请从三张奖励卡中选择一张加入牌组，再进入下一关。");
    }

    /** 选择一张小怪奖励卡并开始下一关。 */
    public boolean selectReward(int rewardIndex) {
        if (!rewardPending || rewardIndex < 0 || rewardIndex >= rewardChoices.size()) return false;

        GameModel.Card selected = rewardChoices.get(rewardIndex);
        player.addCard(selected);
        rewardChoices = Collections.emptyList();
        rewardPending = false;
        log("✅ 你获得了奖励卡【" + selected.getName() + "】！");

        currentLevelIndex++;
        enemy = createCurrentLevelEnemy();
        // 新关卡从完整洗牌后的抽牌堆开始，弃牌堆不保留上一关的卡。
        player.prepareForNewEncounter();
        log("➡ 进入第 " + getCurrentLevel() + "/" + getTotalLevels()
                + " 关：" + enemy.getName() + "！");
        beginNewTurn();
        return true;
    }

    /** 从全部奖励中无重复随机抽取三张。 */
    private List<GameModel.Card> createRewardChoices() {
        List<GameModel.Card> allRewards = new ArrayList<>(List.of(
                model.new IronSlashCard(),
                model.new HeavyHammerCard(),
                model.new WallCard(),
                model.new UltimateStrikeCard(),
                model.new UltimateDefendCard()
        ));
        Collections.shuffle(allRewards);
        return new ArrayList<>(allRewards.subList(0, 3));
    }

    private GameModel.Enemy createCurrentLevelEnemy() {
        EnemyLevel level = LEVELS[currentLevelIndex];
        return model.new Enemy(level.name(), level.maxHp(), level.attackDamage());
    }

    // ==================== 查询接口 ====================

    public boolean canPlayCard(GameModel.Card card) {
        return playerTurn && !gameOver && !rewardPending && card != null && player.getEnergy() >= card.getCost();
    }

    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerWin() { return playerWin; }
    public boolean isPlayerLose() { return gameOver && !playerWin; }
    public boolean isPlayerTurn() { return playerTurn; }
    public boolean isRewardPending() { return rewardPending; }

    public int getCurrentLevel() { return currentLevelIndex + 1; }
    public int getTotalLevels() { return LEVELS.length; }
    public boolean isBossLevel() { return currentLevelIndex == LEVELS.length - 1; }

    public GameModel getModel() { return model; }
    public GameModel.Player getPlayer() { return player; }
    public GameModel.Enemy getEnemy() { return enemy; }
    public List<GameModel.Card> getRewardChoices() {
        return Collections.unmodifiableList(rewardChoices);
    }

    /** 完整战斗日志（只读） */
    public List<String> getBattleLog() {
        return Collections.unmodifiableList(battleLog);
    }

    /** 兼容旧接口：最后一条消息 */
    public String getMessage() {
        return battleLog.isEmpty() ? "" : battleLog.get(battleLog.size() - 1);
    }

    // ==================== 内部日志 ====================

    private void log(String msg) {
        battleLog.add(msg);
        // 环形裁剪，防止无限增长
        while (battleLog.size() > MAX_LOG_LINES) {
            battleLog.remove(0);
        }
    }

    /** 关卡仅配置敌人的基础属性，便于后续继续添加关卡而无需改战斗流程。 */
    private record EnemyLevel(String name, int maxHp, int attackDamage) { }
}
