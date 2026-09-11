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
    private final GameModel.Enemy enemy;
    private final List<String> battleLog;

    private boolean gameOver;
    private boolean playerWin;
    private boolean playerTurn;

    public GameController() {
        this.model = new GameModel();
        this.player = model.new Player();
        this.enemy = model.new Enemy("打击者", 50, 8);
        this.battleLog = new ArrayList<>();
        this.gameOver = false;
        this.playerWin = false;
        this.playerTurn = true;
        startGame();
    }

    /** 开局：初始化日志并让玩家进入第一回合 */
    private void startGame() {
        battleLog.clear();
        log("⚔ 你遇到了 " + enemy.getName() + "！");
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
        if (!playerTurn || gameOver) return false;
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

        if (!enemy.isAlive()) {
            gameOver = true;
            playerWin = true;
            log("🎉 你击败了 " + enemy.getName() + "，胜利！");
        }
        return true;
    }

    /** 结束当前玩家回合：玩家弃牌 → 敌人行动 → 新回合开始 */
    public void endTurn() {
        if (!playerTurn || gameOver) return;

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

    // ==================== 查询接口 ====================

    public boolean canPlayCard(GameModel.Card card) {
        return playerTurn && !gameOver && card != null && player.getEnergy() >= card.getCost();
    }

    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerWin() { return playerWin; }
    public boolean isPlayerLose() { return gameOver && !playerWin; }
    public boolean isPlayerTurn() { return playerTurn; }

    public GameModel getModel() { return model; }
    public GameModel.Player getPlayer() { return player; }
    public GameModel.Enemy getEnemy() { return enemy; }

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
}
