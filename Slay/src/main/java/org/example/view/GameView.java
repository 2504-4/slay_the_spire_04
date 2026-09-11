package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.controller.GameController;
import org.example.model.GameModel;

import java.util.function.Supplier;

/**
 * 游戏主界面。负责渲染 Model 状态、把用户操作转发给 Controller。
 */
public class GameView {

    // ===== 尺寸常量：避免手牌在窗口内溢出 =====
    private static final double CARD_WIDTH  = 130;
    private static final double CARD_HEIGHT = 180;
    private static final double CARD_GAP    = 10;

    // ===== 按钮样式常量 =====
    private static final String BTN_END_NORMAL =
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;";
    private static final String BTN_END_HOVER =
            "-fx-background-color: #c0392b;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;";
    private static final String BTN_END_DISABLED =
            "-fx-background-color: #7f8c8d;" +
            "-fx-text-fill: #bdc3c7;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;";
    private static final String BTN_RESTART =
            "-fx-background-color: #27ae60;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;";
    private static final String BTN_RESTART_HOVER =
            "-fx-background-color: #229954;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;";

    private static final String CARD_ATTACK_STYLE =
            "-fx-background-color: linear-gradient(to bottom, #ff6b6b, #c0392b);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #ff9f9f;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;";
    private static final String CARD_SKILL_STYLE =
            "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #85c1e9;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;";

    // ===== 依赖 =====
    private GameController controller;
    private final Supplier<GameController> controllerFactory;   // 用于"重新开始"

    // ===== 顶部状态 =====
    private Label playerNameLabel;
    private Label enemyNameLabel;
    private Label enemyIntentLabel;
    private ProgressBar playerHpBar;
    private ProgressBar enemyHpBar;
    private Label playerHpText;
    private Label enemyHpText;
    private Label playerBlockLabel;
    private Label enemyBlockLabel;

    // ===== 能量 / 牌堆 =====
    private Label energyLabel;
    private Label drawPileLabel;
    private Label discardPileLabel;

    // ===== 手牌区 =====
    private HBox handBox;

    // ===== 日志区 =====
    private TextArea logArea;

    // ===== 按钮 =====
    private Button endTurnButton;
    private Button restartButton;

    // ===== 根容器 =====
    private final BorderPane root;

    public GameView(GameController controller) {
        this(controller, null);
    }

    /**
     * @param controllerFactory 用于"重新开始"按钮重建 controller；传 null 则隐藏该按钮。
     */
    public GameView(GameController controller, Supplier<GameController> controllerFactory) {
        this.controller = controller;
        this.controllerFactory = controllerFactory;
        this.root = new BorderPane();
        initializeUI();
    }

    // ==================== UI 构建 ====================

    private void initializeUI() {
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setPadding(new Insets(15));

        root.setTop(buildTopArea());
        root.setCenter(buildCenterArea());
        root.setBottom(buildBottomArea());
    }

    private VBox buildTopArea() {
        VBox topBox = new VBox(12);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        // 敌人（含意图）
        VBox enemyBox = createCharacterBox("敌人", Color.RED, true);
        enemyNameLabel   = (Label)       enemyBox.getChildren().get(0);
        enemyHpBar       = (ProgressBar) enemyBox.getChildren().get(1);
        enemyHpText      = (Label)       enemyBox.getChildren().get(2);
        enemyBlockLabel  = (Label)       enemyBox.getChildren().get(3);
        enemyIntentLabel = (Label)       enemyBox.getChildren().get(4);

        // 玩家（不含意图）
        VBox playerBox = createCharacterBox("玩家", Color.GREEN, false);
        playerNameLabel  = (Label)       playerBox.getChildren().get(0);
        playerHpBar      = (ProgressBar) playerBox.getChildren().get(1);
        playerHpText     = (Label)       playerBox.getChildren().get(2);
        playerBlockLabel = (Label)       playerBox.getChildren().get(3);

        // 能量
        HBox energyBox = new HBox(10);
        energyBox.setAlignment(Pos.CENTER);
        energyLabel = new Label("⚡ 能量: 0/0");
        energyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        energyLabel.setTextFill(Color.GOLD);
        energyBox.getChildren().add(energyLabel);

        // 牌堆
        HBox pileBox = new HBox(20);
        pileBox.setAlignment(Pos.CENTER);
        drawPileLabel = new Label("🃏 抽牌堆: 0");
        discardPileLabel = new Label("🗑️ 弃牌堆: 0");
        drawPileLabel.setFont(Font.font("Arial", 14));
        discardPileLabel.setFont(Font.font("Arial", 14));
        drawPileLabel.setTextFill(Color.WHITE);
        discardPileLabel.setTextFill(Color.WHITE);
        pileBox.getChildren().addAll(drawPileLabel, discardPileLabel);

        topBox.getChildren().addAll(enemyBox, playerBox, energyBox, pileBox);
        return topBox;
    }

    private VBox buildCenterArea() {
        VBox centerBox = new VBox(8);
        centerBox.setAlignment(Pos.CENTER);

        Label handTitle = new Label("你的手牌");
        handTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        handTitle.setTextFill(Color.WHITE);

        handBox = new HBox(CARD_GAP);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(10));
        handBox.setMinHeight(CARD_HEIGHT + 10);
        handBox.setPrefHeight(CARD_HEIGHT + 10);

        centerBox.getChildren().addAll(handTitle, handBox);
        return centerBox;
    }

    private VBox buildBottomArea() {
        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        // 战斗日志：显式约束高度，避免撑爆 BorderPane bottom 区域把按钮挤出窗口
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(5);
        logArea.setPrefWidth(760);
        logArea.setPrefHeight(110);
        logArea.setMinHeight(90);
        logArea.setMaxHeight(140);
        logArea.setStyle(
                "-fx-control-inner-background: #16213e;" +
                "-fx-text-fill: #ecf0f1;" +
                "-fx-font-family: 'Consolas', 'Microsoft YaHei Mono', monospace;" +
                "-fx-font-size: 13px;" +
                "-fx-border-color: rgba(255,255,255,0.2);" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );

        // 按钮组
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        endTurnButton = new Button("结束回合");
        endTurnButton.setPrefWidth(180);
        endTurnButton.setPrefHeight(40);
        endTurnButton.setStyle(BTN_END_NORMAL);
        endTurnButton.setOnAction(e -> onEndTurn());
        endTurnButton.setOnMouseEntered(e -> {
            if (!endTurnButton.isDisable()) endTurnButton.setStyle(BTN_END_HOVER);
        });
        endTurnButton.setOnMouseExited(e -> {
            if (!endTurnButton.isDisable()) endTurnButton.setStyle(BTN_END_NORMAL);
        });

        restartButton = new Button("重新开始");
        restartButton.setPrefWidth(140);
        restartButton.setPrefHeight(40);
        restartButton.setStyle(BTN_RESTART);
        restartButton.setOnAction(e -> onRestart());
        restartButton.setOnMouseEntered(e -> restartButton.setStyle(BTN_RESTART_HOVER));
        restartButton.setOnMouseExited(e -> restartButton.setStyle(BTN_RESTART));
        if (controllerFactory == null) {
            restartButton.setVisible(false);
            restartButton.setManaged(false);
        }

        buttonBox.getChildren().addAll(endTurnButton, restartButton);
        bottomBox.getChildren().addAll(logArea, buttonBox);
        return bottomBox;
    }

    private VBox createCharacterBox(String title, Color hpColor, boolean showIntent) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: rgba(255,255,255,0.25);" +
                "-fx-border-radius: 10;"
        );

        Label nameLabel = new Label(title);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        ProgressBar hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(300);
        hpBar.setPrefHeight(20);
        hpBar.setStyle("-fx-accent: " + toHexString(hpColor) + ";");

        Label hpText = new Label("HP: 0/0");
        hpText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hpText.setTextFill(Color.WHITE);

        Label blockLabel = new Label("🛡️ 格挡: 0");
        blockLabel.setFont(Font.font("Arial", 12));
        blockLabel.setTextFill(Color.CYAN);

        box.getChildren().addAll(nameLabel, hpBar, hpText, blockLabel);

        if (showIntent) {
            Label intentLabel = new Label("意图: —");
            intentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            intentLabel.setTextFill(Color.ORANGE);
            box.getChildren().add(intentLabel);
        }
        return box;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed()   * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue()  * 255));
    }

    // ==================== 状态刷新 ====================

    public void update() {
        GameModel.Player player = controller.getPlayer();
        GameModel.Enemy  enemy  = controller.getEnemy();

        // 玩家
        playerNameLabel.setText("玩家");
        playerHpBar.setProgress(safeRatio(player.getHp(), player.getMaxHp()));
        playerHpText.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        playerBlockLabel.setText("🛡️ 格挡: " + player.getBlock());

        // 敌人
        enemyNameLabel.setText(enemy.getName());
        enemyHpBar.setProgress(safeRatio(enemy.getHp(), enemy.getMaxHp()));
        enemyHpText.setText("HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
        enemyBlockLabel.setText("🛡️ 格挡: " + enemy.getBlock());
        enemyIntentLabel.setText(enemy.isAlive()
                ? "下回合意图: " + enemy.getIntentDescription()
                : "已击败");

        // 能量 & 牌堆
        energyLabel.setText("⚡ 能量: " + player.getEnergy() + "/" + player.getMaxEnergy());
        drawPileLabel.setText("🃏 抽牌堆: " + player.getDrawPile().size());
        discardPileLabel.setText("🗑️ 弃牌堆: " + player.getDiscardPile().size());

        // 手牌（重建）
        handBox.getChildren().clear();
        for (int i = 0; i < player.getHand().size(); i++) {
            handBox.getChildren().add(createCardButton(player.getHand().get(i), i));
        }

        // 日志
        logArea.setText(String.join("\n", controller.getBattleLog()));
        logArea.setScrollTop(Double.MAX_VALUE);

        // 结束状态
        boolean over = controller.isGameOver();
        endTurnButton.setDisable(over);
        endTurnButton.setStyle(over ? BTN_END_DISABLED : BTN_END_NORMAL);
        if (over) {
            String banner = controller.isPlayerWin()
                    ? "\n========== 🎉 胜利！ =========="
                    : "\n========== 💀 战败… ==========";
            logArea.appendText(banner);
            logArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    private double safeRatio(int current, int max) {
        if (max <= 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) current / max));
    }

    private Button createCardButton(GameModel.Card card, int index) {
        Button button = new Button();
        button.setPrefWidth(CARD_WIDTH);
        button.setPrefHeight(CARD_HEIGHT);
        button.setStyle(card.getType() == GameModel.Card.CardType.ATTACK
                ? CARD_ATTACK_STYLE
                : CARD_SKILL_STYLE);

        // 卡面内容
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(card.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);

        Label costLabel = new Label("⚡ " + card.getCost());
        costLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        costLabel.setTextFill(Color.GOLD);

        Label descLabel = new Label(card.getDescription());
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(CARD_WIDTH - 20);

        content.getChildren().addAll(nameLabel, costLabel, descLabel);
        button.setGraphic(content);

        // 禁用状态：真正 disable + 半透明，且不注册悬停动画
        boolean playable = controller.canPlayCard(card);
        if (!playable) {
            button.setOpacity(0.5);
            button.setDisable(true);
        } else {
            button.setOnMouseEntered(e -> {
                button.setScaleX(1.05);
                button.setScaleY(1.05);
                button.setEffect(new DropShadow(20, Color.WHITE));
            });
            button.setOnMouseExited(e -> {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
                button.setEffect(null);
            });
        }

        button.setOnAction(e -> onPlayCard(index));
        return button;
    }

    // ==================== 用户交互 ====================

    private void onPlayCard(int index) {
        controller.playCard(index);
        update();
    }

    private void onEndTurn() {
        if (endTurnButton.isDisable()) return;
        controller.endTurn();
        update();
    }

    private void onRestart() {
        if (controllerFactory == null) return;
        this.controller = controllerFactory.get();
        update();
    }

    public BorderPane getRoot() {
        return root;
    }
}
