package org.example.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.BattleGame;
import org.example.model.BattleSnapshot;
import org.example.model.Card;
import org.example.model.DefaultBattleGame;
import org.example.model.GameStatus;

/** Standalone, simple JavaFX presentation that leaves the existing application unchanged. */
public final class SimpleBattleUi extends Application {
    private static final double WINDOW_WIDTH = 900;
    private static final double WINDOW_HEIGHT = 560;
    private static final double CONTENT_PADDING = 24;
    private static final double SECTION_SPACING = 16;
    private static final double CARD_WIDTH = 130;
    private static final double CARD_HEIGHT = 96;
    private static final String WINDOW_TITLE = "Simple Battle UI";
    private static final String ROOT_STYLE = "-fx-background-color: #1b2638;";
    private static final String CARD_STYLE = "-fx-background-color: #426f9c; -fx-text-fill: white;";

    private final BattleGame battleGame = new DefaultBattleGame();
    private final Label messageLabel = new Label();
    private final Label energyLabel = new Label();
    private final Label playerStatsLabel = new Label();
    private final Label monsterStatsLabel = new Label();
    private final ProgressBar playerHealthBar = new ProgressBar();
    private final ProgressBar monsterHealthBar = new ProgressBar();
    private final HBox handContainer = new HBox(SECTION_SPACING);
    private final Button startButton = new Button("Start battle");
    private final Button endTurnButton = new Button("End turn");

    @Override
    public void start(Stage stage) {
        BorderPane root = createRoot();
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
        renderSnapshot();
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }

    private BorderPane createRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(CONTENT_PADDING));
        root.setStyle(ROOT_STYLE);
        root.setTop(createHeader());
        root.setCenter(createBattleStatus());
        root.setBottom(handContainer);
        handContainer.setAlignment(Pos.CENTER);
        return root;
    }

    private VBox createHeader() {
        startButton.setOnAction(ignoredEvent -> startBattle());
        endTurnButton.setOnAction(ignoredEvent -> endTurn());
        HBox actionBar = new HBox(SECTION_SPACING, startButton, endTurnButton, energyLabel);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        return new VBox(SECTION_SPACING, actionBar, messageLabel);
    }

    private HBox createBattleStatus() {
        VBox playerPanel = createFighterPanel("Player", playerStatsLabel, playerHealthBar);
        VBox monsterPanel = createFighterPanel("Monster", monsterStatsLabel, monsterHealthBar);
        HBox statusPanel = new HBox(SECTION_SPACING, playerPanel, monsterPanel);
        statusPanel.setAlignment(Pos.CENTER);
        return statusPanel;
    }

    private VBox createFighterPanel(String fighterName, Label statsLabel, ProgressBar healthBar) {
        Label fighterNameLabel = new Label(fighterName);
        VBox fighterPanel = new VBox(SECTION_SPACING, fighterNameLabel, healthBar, statsLabel);
        fighterPanel.setAlignment(Pos.CENTER);
        return fighterPanel;
    }

    private void startBattle() {
        battleGame.start();
        renderSnapshot();
    }

    private void endTurn() {
        battleGame.endTurn();
        renderSnapshot();
    }

    private void playCard(int handCardIndex) {
        battleGame.playCard(handCardIndex);
        renderSnapshot();
    }

    private void renderSnapshot() {
        BattleSnapshot battleSnapshot = battleGame.snapshot();
        renderStatus(battleSnapshot);
        renderHand(battleSnapshot);
    }

    private void renderStatus(BattleSnapshot battleSnapshot) {
        messageLabel.setText(battleSnapshot.message());
        energyLabel.setText("Energy: " + battleSnapshot.energy());
        playerStatsLabel.setText(formatStats(battleSnapshot.playerHealth(), battleSnapshot.playerMaxHealth(), battleSnapshot.playerBlock()));
        monsterStatsLabel.setText(formatStats(battleSnapshot.monsterHealth(), battleSnapshot.monsterMaxHealth(), battleSnapshot.monsterBlock()));
        playerHealthBar.setProgress(healthRatio(battleSnapshot.playerHealth(), battleSnapshot.playerMaxHealth()));
        monsterHealthBar.setProgress(healthRatio(battleSnapshot.monsterHealth(), battleSnapshot.monsterMaxHealth()));
        startButton.setText(battleSnapshot.status() == GameStatus.READY ? "Start battle" : "Restart battle");
        endTurnButton.setDisable(battleSnapshot.status() != GameStatus.PLAYING);
    }

    private void renderHand(BattleSnapshot battleSnapshot) {
        handContainer.getChildren().clear();
        for (int cardIndex = 0; cardIndex < battleSnapshot.hand().size(); cardIndex++) {
            handContainer.getChildren().add(createCardButton(battleSnapshot, cardIndex));
        }
    }

    private Button createCardButton(BattleSnapshot battleSnapshot, int cardIndex) {
        Card handCard = battleSnapshot.hand().get(cardIndex);
        Button cardButton = new Button(handCard.name() + "\n" + handCard.description());
        cardButton.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardButton.setStyle(CARD_STYLE);
        cardButton.setDisable(battleSnapshot.energy() < handCard.cost());
        cardButton.setOnAction(ignoredEvent -> playCard(cardIndex));
        return cardButton;
    }

    private String formatStats(int health, int maximumHealth, int block) {
        return "Health: " + health + "/" + maximumHealth + "   Block: " + block;
    }

    private double healthRatio(int health, int maximumHealth) {
        return (double) health / maximumHealth;
    }
}