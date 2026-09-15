package org.example.controller;

import org.controller.GameController;
import org.model.GameModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 控制器回归测试：使用确定的前置状态，不依赖随机抽牌或随机敌人意图。 */
class GameControllerTest {
    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController();
    }

    @Test
    @DisplayName("初始化：第一关、五张手牌、玩家回合")
    void initialization() {
        assertNotNull(controller.getPlayer());
        assertNotNull(controller.getEnemy());
        assertEquals(80, controller.getPlayer().getHp());
        assertEquals(1, controller.getCurrentLevel());
        assertEquals(4, controller.getTotalLevels());
        assertEquals(25, controller.getEnemy().getHp());
        assertTrue(controller.isPlayerTurn());
        assertFalse(controller.isGameOver());
        assertFalse(controller.isPlayerWin());
        assertFalse(controller.isPlayerLose());
        assertFalse(controller.getBattleLog().isEmpty());
        assertEquals(5, controller.getPlayer().getHand().size());
        assertCardConservation();
    }

    @Test
    void playCardAttack() {
        int index = putInitialCardInHand(GameModel.StrikeCard.class);
        int hp = controller.getEnemy().getHp();
        assertTrue(controller.playCard(index));
        assertEquals(hp - GameModel.StrikeCard.DAMAGE, controller.getEnemy().getHp());
        assertCardConservation();
    }

    @Test
    void playCardSkill() {
        int index = putInitialCardInHand(GameModel.DefendCard.class);
        int block = controller.getPlayer().getBlock();
        assertTrue(controller.playCard(index));
        assertEquals(block + GameModel.DefendCard.BLOCK, controller.getPlayer().getBlock());
        assertCardConservation();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 100, -999})
    void playCardInvalidIndex(int index) {
        int energy = controller.getPlayer().getEnergy();
        List<GameModel.Card> hand = new ArrayList<>(controller.getPlayer().getHand());
        assertFalse(controller.playCard(index));
        assertEquals(energy, controller.getPlayer().getEnergy());
        assertEquals(hand, controller.getPlayer().getHand());
    }

    @Test
    void playCardReducesHandAndEnergy() {
        GameModel.Player player = controller.getPlayer();
        GameModel.Card card = player.getHand().get(0);
        int handBefore = player.getHand().size();
        int energyBefore = player.getEnergy();
        assertTrue(controller.playCard(0));
        assertEquals(handBefore - 1, player.getHand().size());
        assertEquals(energyBefore - card.getCost(), player.getEnergy());
        assertTrue(player.getDiscardPile().contains(card));
        assertCardConservation();
    }

    @Test
    @DisplayName("小怪奖励：等待选择、加入牌组、进入第二关")
    void rewardCardAfterMinorEnemy() {
        defeatCurrentEnemy();
        assertTrue(controller.isRewardPending());
        assertFalse(controller.isGameOver());
        assertEquals(3, controller.getRewardChoices().size());
        GameModel.Card selected = controller.getRewardChoices().get(0);
        int hp = controller.getPlayer().getHp();
        assertTrue(controller.selectReward(0));
        assertFalse(controller.isRewardPending());
        assertTrue(controller.getRewardChoices().isEmpty());
        assertEquals(2, controller.getCurrentLevel());
        assertEquals(40, controller.getEnemy().getHp());
        assertEquals(hp, controller.getPlayer().getHp());
        assertEquals(11, controller.getPlayer().getDeck().size());
        assertEquals(0, controller.getPlayer().getDiscardPile().size());
        assertEquals(5, controller.getPlayer().getHand().size());
        assertEquals(3, controller.getPlayer().getEnergy());
        assertTrue(controller.getPlayer().getDeck().contains(selected));
        assertFalse(controller.selectReward(0), "不能重复领取奖励");
        assertCardConservation();
    }

    @Test
    @DisplayName("奖励等待期间不能继续战斗，也不能用非法索引跳关")
    void rewardPendingBlocksBattleActions() {
        defeatCurrentEnemy();
        List<String> log = new ArrayList<>(controller.getBattleLog());
        List<GameModel.Card> hand = new ArrayList<>(controller.getPlayer().getHand());
        int energy = controller.getPlayer().getEnergy();
        int hp = controller.getPlayer().getHp();
        assertFalse(controller.playCard(0));
        assertFalse(controller.canPlayCard(hand.get(0)));
        controller.endTurn();
        assertFalse(controller.selectReward(-1));
        assertFalse(controller.selectReward(3));
        assertTrue(controller.isRewardPending());
        assertFalse(controller.isGameOver());
        assertEquals(1, controller.getCurrentLevel());
        assertEquals(10, controller.getPlayer().getDeck().size());
        assertEquals(energy, controller.getPlayer().getEnergy());
        assertEquals(hp, controller.getPlayer().getHp());
        assertEquals(hand, controller.getPlayer().getHand());
        assertEquals(log, controller.getBattleLog());
    }

    @Test
    void endTurnNormal() {
        controller.endTurn(); // 敌人首次意图固定为攻击，伤害为 4。
        assertTrue(controller.isPlayerTurn());
        assertEquals(76, controller.getPlayer().getHp());
        assertEquals(3, controller.getPlayer().getEnergy());
        assertEquals(5, controller.getPlayer().getHand().size());
        assertTrue(controller.getBattleLog().size() > 2);
        assertCardConservation();
    }

    @Test
    void endTurnDiscardsHand() {
        List<GameModel.Card> oldHand = new ArrayList<>(controller.getPlayer().getHand());
        controller.endTurn();
        assertEquals(oldHand, controller.getPlayer().getDiscardPile());
        assertTrue(controller.getPlayer().getHand().stream().noneMatch(oldHand::contains));
        assertCardConservation();
    }

    @Test
    @DisplayName("完整通关：前三关逐次选择奖励，第四关才进入胜利终局")
    void victoryCondition() {
        winAllLevels();
        assertTrue(controller.isGameOver());
        assertTrue(controller.isPlayerWin());
        assertFalse(controller.isPlayerLose());
        assertEquals(4, controller.getCurrentLevel());
        assertTrue(controller.isBossLevel());
        assertFalse(controller.isRewardPending());
        assertTrue(controller.getRewardChoices().isEmpty());
        assertEquals(13, controller.getPlayer().getDeck().size());
        assertCardConservation();
    }

    @Test
    void noActionsAfterGameOver() {
        winAllLevels();
        assertTerminalActionsAreIgnored();
    }

    @Test
    @DisplayName("玩家被击败后进入失败终局，不再接受操作")
    void defeatCondition() {
        controller.getPlayer().takeDamage(79);
        controller.endTurn();
        assertEquals(0, controller.getPlayer().getHp());
        assertTrue(controller.isPlayerLose());
        assertFalse(controller.isPlayerWin());
        assertTerminalActionsAreIgnored();
    }

    @Test
    void battleLogContent() {
        List<String> log = controller.getBattleLog();
        assertTrue(log.get(0).contains("第 1/4 关"));
        int before = log.size();
        assertTrue(controller.playCard(0));
        assertTrue(controller.getBattleLog().size() > before);
    }

    @Test
    void getMessageReturnsLast() {
        String message = controller.getMessage();
        assertNotNull(message);
        assertFalse(message.isEmpty());
        List<String> log = controller.getBattleLog();
        assertEquals(log.get(log.size() - 1), message);
    }

    @Test
    void canPlayCardCheck() {
        List<GameModel.Card> hand = controller.getPlayer().getHand();
        assertFalse(hand.isEmpty());
        assertTrue(controller.canPlayCard(hand.get(0)));
        assertFalse(controller.canPlayCard(null));
    }

    @Test
    void insufficientEnergyRejectsCard() {
        // 首回合全是 1 费基础牌，三次出牌最多造成 18 点伤害，不会击败小怪。
        for (int i = 0; i < 3; i++) assertTrue(controller.playCard(0));
        assertEquals(0, controller.getPlayer().getEnergy());
        List<GameModel.Card> hand = new ArrayList<>(controller.getPlayer().getHand());
        int hp = controller.getEnemy().getHp();
        assertFalse(controller.canPlayCard(hand.get(0)));
        assertFalse(controller.playCard(0));
        assertEquals(hand, controller.getPlayer().getHand());
        assertEquals(hp, controller.getEnemy().getHp());
        assertCardConservation();
    }

    /** 将牌组中的现有卡移入手牌，不创建额外卡、不破坏牌堆守恒。 */
    private int putInitialCardInHand(Class<? extends GameModel.Card> type) {
        GameModel.Player player = controller.getPlayer();
        GameModel.Card card = player.getDeck().stream().filter(type::isInstance)
                .findFirst().orElseThrow();
        if (!player.getHand().contains(card)) {
            assertTrue(player.getDrawPile().remove(card) || player.getDiscardPile().remove(card));
            player.getHand().add(card);
        }
        return player.getHand().indexOf(card);
    }

    /** 固定为一击可击败的前置状态，再通过真实出牌触发控制器的击败流程。
     * 此处测试状态流转，不模拟随机策略，也不修改生产代码的游戏规则。
     */
    private void defeatCurrentEnemy() {
        assertFalse(controller.isGameOver());
        assertFalse(controller.isRewardPending());
        int index = putInitialCardInHand(GameModel.StrikeCard.class);
        GameModel.Enemy enemy = controller.getEnemy();
        enemy.takeDamage(enemy.getBlock() + enemy.getHp() - 1);
        assertEquals(1, enemy.getHp());
        assertTrue(controller.playCard(index));
        assertFalse(enemy.isAlive());
        assertCardConservation();
    }

    private void winAllLevels() {
        for (int level = 1; level <= controller.getTotalLevels(); level++) {
            assertEquals(level, controller.getCurrentLevel());
            defeatCurrentEnemy();
            if (level < controller.getTotalLevels()) {
                assertFalse(controller.isGameOver());
                assertTrue(controller.isRewardPending());
                assertEquals(3, controller.getRewardChoices().size());
                assertTrue(controller.selectReward(0));
                assertFalse(controller.isRewardPending());
                assertEquals(level + 1, controller.getCurrentLevel());
            }
        }
    }

    private void assertTerminalActionsAreIgnored() {
        assertTrue(controller.isGameOver(), "必须确实进入终局，避免条件断言空通过");
        List<String> log = new ArrayList<>(controller.getBattleLog());
        List<GameModel.Card> hand = new ArrayList<>(controller.getPlayer().getHand());
        int hp = controller.getPlayer().getHp();
        int enemyHp = controller.getEnemy().getHp();
        int energy = controller.getPlayer().getEnergy();
        int level = controller.getCurrentLevel();
        assertFalse(controller.playCard(0));
        assertFalse(controller.canPlayCard(controller.getModel().new StrikeCard()));
        assertFalse(controller.selectReward(0));
        controller.endTurn();
        assertEquals(log, controller.getBattleLog());
        assertEquals(hand, controller.getPlayer().getHand());
        assertEquals(hp, controller.getPlayer().getHp());
        assertEquals(enemyHp, controller.getEnemy().getHp());
        assertEquals(energy, controller.getPlayer().getEnergy());
        assertEquals(level, controller.getCurrentLevel());
    }

    private void assertCardConservation() {
        GameModel.Player player = controller.getPlayer();
        List<GameModel.Card> cards = new ArrayList<>(player.getHand());
        cards.addAll(player.getDrawPile());
        cards.addAll(player.getDiscardPile());
        assertEquals(player.getDeck().size(), cards.size());
        assertEquals(cards.size(), cards.stream().distinct().count());
        assertTrue(cards.containsAll(player.getDeck()));
    }
}
