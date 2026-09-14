package org.example.controller;

import org.example.model.GameModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameController 单元测试：覆盖出牌、回合结束、胜负判定、日志等核心流程。
 */
class GameControllerTest {

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController();
    }

    // ========== 初始化 ==========

    @Test
    @DisplayName("Controller初始化：玩家80HP, 敌人50HP, 玩家回合, 游戏未结束")
    void initialization() {
        assertNotNull(controller.getPlayer());
        assertNotNull(controller.getEnemy());
        assertEquals(80, controller.getPlayer().getHp());
        assertEquals(50, controller.getEnemy().getHp());
        assertTrue(controller.isPlayerTurn());
        assertFalse(controller.isGameOver());
        assertFalse(controller.isPlayerWin());
        assertFalse(controller.isPlayerLose());
        assertFalse(controller.getBattleLog().isEmpty());
        assertEquals(5, controller.getPlayer().getHand().size());
    }

    // ========== playCard ==========

    @Test
    @DisplayName("playCard正常：打出攻击卡后敌人HP减少")
    void playCardAttack() {
        // 找一张攻击卡（打击卡）
        int attackIdx = findCardIndex(GameModel.Card.CardType.ATTACK);
        if (attackIdx >= 0) {
            int enemyHpBefore = controller.getEnemy().getHp();
            assertTrue(controller.playCard(attackIdx));
            assertTrue(controller.getEnemy().getHp() < enemyHpBefore);
        }
    }

    @Test
    @DisplayName("playCard正常：打出技能卡后玩家格挡增加")
    void playCardSkill() {
        int skillIdx = findCardIndex(GameModel.Card.CardType.SKILL);
        if (skillIdx >= 0) {
            int blockBefore = controller.getPlayer().getBlock();
            assertTrue(controller.playCard(skillIdx));
            assertTrue(controller.getPlayer().getBlock() > blockBefore);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 100, -999})
    @DisplayName("playCard异常：无效索引返回false")
    void playCardInvalidIndex(int index) {
        assertFalse(controller.playCard(index));
    }

    @Test
    @DisplayName("playCard正常：打出后手牌减少1张, 能量减少")
    void playCardReducesHandAndEnergy() {
        int handBefore = controller.getPlayer().getHand().size();
        int energyBefore = controller.getPlayer().getEnergy();
        controller.playCard(0);
        assertEquals(handBefore - 1, controller.getPlayer().getHand().size());
        assertTrue(controller.getPlayer().getEnergy() < energyBefore);
    }

    // ========== endTurn ==========

    @Test
    @DisplayName("endTurn正常：玩家弃牌后敌人行动, 进入新回合")
    void endTurnNormal() {
        controller.endTurn();
        // 新回合开始，玩家有手牌和能量
        assertTrue(controller.isPlayerTurn());
        assertTrue(controller.getPlayer().getHand().size() > 0);
        assertTrue(controller.getBattleLog().size() > 2);
    }

    @Test
    @DisplayName("endTurn：玩家手牌全部进入弃牌堆")
    void endTurnDiscardsHand() {
        controller.endTurn();
        // 新回合开始时手牌是新抽的，弃牌堆里有上一轮的牌
        assertTrue(controller.getPlayer().getDiscardPile().size() > 0);
    }

    // ========== 胜负判定 ==========

    @Test
    @DisplayName("胜利判定：击败敌人后gameOver=true, playerWin=true")
    void victoryCondition() {
        // 持续攻击直到敌人死亡
        for (int turn = 0; turn < 50 && !controller.isGameOver(); turn++) {
            for (int i = controller.getPlayer().getHand().size() - 1; i >= 0; i--) {
                if (!controller.isGameOver()) {
                    controller.playCard(i);
                }
            }
            if (!controller.isGameOver()) {
                controller.endTurn();
            }
        }
        assertTrue(controller.isGameOver());
        assertTrue(controller.isPlayerWin());
        assertFalse(controller.isPlayerLose());
    }

    @Test
    @DisplayName("游戏结束后无法再出牌或结束回合")
    void noActionsAfterGameOver() {
        // 强制模拟游戏结束
        for (int turn = 0; turn < 50 && !controller.isGameOver(); turn++) {
            for (int i = controller.getPlayer().getHand().size() - 1; i >= 0; i--) {
                if (!controller.isGameOver()) controller.playCard(i);
            }
            if (!controller.isGameOver()) controller.endTurn();
        }
        if (controller.isGameOver()) {
            assertFalse(controller.playCard(0));
            // endTurn 应该静默返回不报错
            controller.endTurn();
        }
    }

    // ========== 日志 ==========

    @Test
    @DisplayName("战斗日志：初始化包含遭遇信息, 出牌后日志增长")
    void battleLogContent() {
        List<String> log = controller.getBattleLog();
        assertTrue(log.get(0).contains("打击者"));

        int sizeBefore = log.size();
        controller.playCard(0);
        assertTrue(controller.getBattleLog().size() > sizeBefore);
    }

    @Test
    @DisplayName("getMessage：返回日志最后一条")
    void getMessageReturnsLast() {
        String msg = controller.getMessage();
        assertNotNull(msg);
        assertFalse(msg.isEmpty());
        List<String> log = controller.getBattleLog();
        assertEquals(log.get(log.size() - 1), msg);
    }

    // ========== canPlayCard ==========

    @Test
    @DisplayName("canPlayCard：有能量时返回true, null返回false")
    void canPlayCardCheck() {
        List<GameModel.Card> hand = controller.getPlayer().getHand();
        if (!hand.isEmpty()) {
            assertTrue(controller.canPlayCard(hand.get(0)));
        }
        assertFalse(controller.canPlayCard(null));
    }

    // ========== 辅助方法 ==========

    private int findCardIndex(GameModel.Card.CardType type) {
        List<GameModel.Card> hand = controller.getPlayer().getHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getType() == type) return i;
        }
        return -1;
    }
}
