// org/example/view/GameView.java
package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.controller.GameController;
import org.example.model.Gamemodel;

public class GameView {
    private GameController controller;

    // 顶部状态
    private Label playerNameLabel;
    private Label enemyNameLabel;
    private ProgressBar playerHpBar;
    private ProgressBar enemyHpBar;
    private Label playerHpText;
    private Label enemyHpText;
    private Label playerBlockLabel;
    private Label enemyBlockLabel;

    // 能量显示
    private HBox energyBox;
    private Label energyLabel;

    // 牌堆信息
    private Label drawPileLabel;
    private Label discardPileLabel;

    // 手牌区
    private HBox handBox;
    private Label handTitle;

    // 日志区
    private Text logText;

    // 按钮
    private Button endTurnButton;

    // 根容器
    private BorderPane root;

    public GameView(GameController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setPadding(new Insets(20));

        // ===== 顶部区域 =====
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));

        // 敌人区域
        VBox enemyBox = createCharacterBox("敌人", Color.RED);
        enemyNameLabel = (Label) enemyBox.getChildren().get(0);
        enemyHpBar = (ProgressBar) enemyBox.getChildren().get(1);
        enemyHpText = (Label) enemyBox.getChildren().get(2);
        enemyBlockLabel = (Label) enemyBox.getChildren().get(3);

        // 玩家区域
        VBox playerBox = createCharacterBox("玩家", Color.GREEN);
        playerNameLabel = (Label) playerBox.getChildren().get(0);
        playerHpBar = (ProgressBar) playerBox.getChildren().get(1);
        playerHpText = (Label) playerBox.getChildren().get(2);
        playerBlockLabel = (Label) playerBox.getChildren().get(3);

        // 能量显示
        energyBox = new HBox(10);
        energyBox.setAlignment(Pos.CENTER);
        energyLabel = new Label("⚡ 能量: 3/3");
        energyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        energyLabel.setTextFill(Color.GOLD);
        energyBox.getChildren().add(energyLabel);

        // 牌堆信息
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
        root.setTop(topBox);

        // ===== 中央手牌区 =====
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);

        handTitle = new Label("你的手牌");
        handTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        handTitle.setTextFill(Color.WHITE);

        handBox = new HBox(15);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(20));

        centerBox.getChildren().addAll(handTitle, handBox);
        root.setCenter(centerBox);

        // ===== 底部区域 =====
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        // 日志区域
        logText = new Text();
        logText.setFont(Font.font("Arial", 14));
        logText.setFill(Color.LIGHTGRAY);
        logText.setWrappingWidth(600);

        // 结束回合按钮
        endTurnButton = new Button("结束回合");
        endTurnButton.setPrefWidth(200);
        endTurnButton.setPrefHeight(40);
        endTurnButton.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        endTurnButton.setOnAction(e -> endTurn());

        // 按钮悬停效果
        endTurnButton.setOnMouseEntered(e ->
                endTurnButton.setStyle(
                        "-fx-background-color: #c0392b;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;"
                )
        );
        endTurnButton.setOnMouseExited(e ->
                endTurnButton.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;"
                )
        );

        bottomBox.getChildren().addAll(logText, endTurnButton);
        root.setBottom(bottomBox);
    }

    private VBox createCharacterBox(String title, Color hpColor) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(255,255,255,0.3);" +
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
        return box;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public void update() {
        Gamemodel.Player player = controller.getPlayer();
        Gamemodel.Enemy enemy = controller.getEnemy();

        // 更新玩家信息
        playerNameLabel.setText("玩家");
        playerHpBar.setProgress((double) player.getHp() / player.getMaxHp());
        playerHpText.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        playerBlockLabel.setText("🛡️ 格挡: " + player.getBlock());

        // 更新敌人信息
        enemyNameLabel.setText(enemy.getName());
        enemyHpBar.setProgress((double) enemy.getHp() / enemy.getMaxHp());
        enemyHpText.setText("HP: " + enemy.getHp() + "/" + enemy.getMaxHp());
        enemyBlockLabel.setText("🛡️ 格挡: " + enemy.getBlock());

        // 更新能量
        energyLabel.setText("⚡ 能量: " + player.getEnergy() + "/" + player.getMaxEnergy());

        // 更新牌堆
        drawPileLabel.setText("🃏 抽牌堆: " + player.getDrawPile().size());
        discardPileLabel.setText("🗑️ 弃牌堆: " + player.getDiscardPile().size());

        // 更新手牌
        handBox.getChildren().clear();
        for (int i = 0; i < player.getHand().size(); i++) {
            Gamemodel.Card card = player.getHand().get(i);
            Button cardButton = createCardButton(card, i);
            handBox.getChildren().add(cardButton);
        }

        // 更新日志
        logText.setText(controller.getMessage());

        // 游戏结束处理
        if (controller.isGameOver()) {
            endTurnButton.setDisable(true);
            if (enemy.getHp() <= 0) {
                logText.setText("🎉 你赢了！");
                logText.setFill(Color.GOLD);
            } else {
                logText.setText("💀 你输了...");
                logText.setFill(Color.RED);
            }
        }
    }

    private Button createCardButton(Gamemodel.Card card, int index) {
        Button button = new Button();
        button.setPrefWidth(150);
        button.setPrefHeight(200);

        // 根据卡牌类型设置样式
        String cardStyle;
        if (card.getType() == Gamemodel.Card.CardType.ATTACK) {
            cardStyle =
                    "-fx-background-color: linear-gradient(to bottom, #ff6b6b, #c0392b);" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #ff9f9f;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 10;";
        } else {
            cardStyle =
                    "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #85c1e9;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 10;";
        }

        button.setStyle(cardStyle);

        // 卡牌内容
        VBox cardContent = new VBox(5);
        cardContent.setAlignment(Pos.CENTER);

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
        descLabel.setMaxWidth(130);

        cardContent.getChildren().addAll(nameLabel, costLabel, descLabel);
        button.setGraphic(cardContent);

        // 禁用状态
        if (!controller.canPlayCard(card)) {
            button.setOpacity(0.5);
        }

        // 悬停效果
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

        final int cardIndex = index;
        button.setOnAction(e -> playCard(cardIndex));

        return button;
    }

    private void playCard(int index) {
        controller.playCard(index);
        update();
    }

    private void endTurn() {
        controller.endTurn();
        update();
    }

    public BorderPane getRoot() {
        return root;
    }
}