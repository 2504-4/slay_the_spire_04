package org.example.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 主菜单界面。
 * <p>
 * 与 Model / Controller 完全解耦：只负责渲染 UI，通过 {@link Runnable} 回调通知外部（Main）
 * "玩家点了开始游戏"，由 Main 决定切换到哪个 Scene。
 */
public class MenuView {

    // ===== 字体常量（中文字体，避免 Arial fallback 导致 textFill 失效）=====
    private static final String FONT_FAMILY = "Microsoft YaHei";

    // ===== 按钮样式常量 =====
    private static final String BTN_START_NORMAL =
            "-fx-font-family: 'Microsoft YaHei';" +
            "-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #ff9f9f;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;";

    private static final String BTN_START_HOVER =
            "-fx-font-family: 'Microsoft YaHei';" +
            "-fx-background-color: linear-gradient(to bottom, #ff6b5b, #e74c3c);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #ffbfbf;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;";

    // ===== 根容器 =====
    private final StackPane root;

    /**
     * @param onStart 玩家点击"开始游戏"时触发的回调（一般是 Main::showGameScene）；传 null 则按钮无反应
     */
    public MenuView(Runnable onStart) {
        root = new StackPane();
        root.setStyle("-fx-background-color: radial-gradient(circle at center, #2c3e50 0%, #1a1a2e 100%);");

        VBox centerBox = new VBox(28);
        centerBox.setAlignment(Pos.CENTER);

        // ===== 主标题 =====
        // 注意：不用 ⚔ 等彩色 emoji，Windows 上 Segoe UI Emoji 会忽略 textFill 强制显示自带颜色
        Label titleLabel = new Label("杀戮尖塔");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 60));
        titleLabel.setTextFill(Color.web("#f1c40f"));
        titleLabel.setEffect(new DropShadow(25, Color.web("#e67e22", 0.8)));

        // ===== 副标题 =====
        Label subtitleLabel = new Label("Slay the Spire — JavaFX Edition");
        subtitleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.web("#bdc3c7"));

        // ===== 开始游戏按钮 =====
        Button startButton = new Button("开始游戏");
        startButton.setPrefWidth(280);
        startButton.setPrefHeight(70);
        startButton.setStyle(BTN_START_NORMAL);
        startButton.setOnAction(e -> {
            if (onStart != null) onStart.run();
        });
        startButton.setOnMouseEntered(e -> {
            startButton.setStyle(BTN_START_HOVER);
            startButton.setScaleX(1.05);
            startButton.setScaleY(1.05);
        });
        startButton.setOnMouseExited(e -> {
            startButton.setStyle(BTN_START_NORMAL);
            startButton.setScaleX(1.0);
            startButton.setScaleY(1.0);
        });

        // ===== 底部提示 =====
        Label hintLabel = new Label("v1.0  ·  击败敌人即可获胜");
        hintLabel.setFont(Font.font(FONT_FAMILY, 12));
        hintLabel.setTextFill(Color.web("#7f8c8d"));

        centerBox.getChildren().addAll(titleLabel, subtitleLabel, startButton, hintLabel);
        root.getChildren().add(centerBox);
    }

    /** 供 Main 塞进 Scene */
    public StackPane getRoot() {
        return root;
    }
}
