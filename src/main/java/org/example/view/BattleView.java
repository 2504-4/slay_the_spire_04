package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.model.BattleSnapshot;
import org.example.model.Card;
import org.example.model.CardType;
import org.example.model.GameStatus;

import java.util.List;
import java.util.function.Consumer;

/** JavaFX-only rendering and input widgets. */
public final class BattleView extends BorderPane implements BattleViewPort {
    private static final double VIEW_PADDING = 20;
    private static final double CARD_WIDTH = 140;
    private static final double CARD_HEIGHT = 110;
    private static final String BACKGROUND_STYLE = "-fx-background-color: linear-gradient(to bottom, #263b55, #101923);";
    private static final String ACTION_BUTTON_STYLE = "-fx-background-color: #d5a94a; -fx-font-weight: bold;";
    private static final String ATTACK_CARD_STYLE = "-fx-background-color: #b54c55; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String DEFEND_CARD_STYLE = "-fx-background-color: #3d7fb2; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String FIGHTER_PANEL_STYLE = "-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 12; -fx-padding: 18;";

    private final Label messageLabel = new Label();
    private final Label energyLabel = new Label();
    private final Label playerLabel = new Label();
    private final Label monsterLabel = new Label();
    private final Label monsterIntentLabel = new Label();
    private final ProgressBar playerBar = new ProgressBar();
    private final ProgressBar monsterBar = new ProgressBar();
    private final HBox handContainer = new HBox(VIEW_PADDING);
    private final Button startButton = new Button("Start");
    private final Button endTurnButton = new Button("End turn");
    private final Button drawPileButton = new Button();
    private final Button discardPileButton = new Button();
    private Runnable startHandler = () -> { };
    private Runnable endTurnHandler = () -> { };
    private Runnable drawPileHandler = () -> { };
    private Runnable discardPileHandler = () -> { };
    private Consumer<Integer> cardPlayedHandler = ignoredCardIndex -> { };

    public BattleView() {
        setPadding(new Insets(VIEW_PADDING));
        setStyle(BACKGROUND_STYLE);
        configureLabels();
        bindHandlers();
        buildLayout();
    }

    @Override
    public void setOnStart(Runnable startHandler) {
        this.startHandler = startHandler;
    }

    @Override
    public void setOnEndTurn(Runnable endTurnHandler) {
        this.endTurnHandler = endTurnHandler;
    }

    @Override
    public void setOnCardPlayed(Consumer<Integer> cardPlayedHandler) {
        this.cardPlayedHandler = cardPlayedHandler;
    }

    @Override
    public void setOnDrawPileViewed(Runnable drawPileViewedHandler) {
        drawPileHandler = drawPileViewedHandler;
    }

    @Override
    public void setOnDiscardPileViewed(Runnable discardPileViewedHandler) {
        discardPileHandler = discardPileViewedHandler;
    }

    @Override
    public void render(BattleSnapshot battleSnapshot) {
        updateLabels(battleSnapshot);
        renderHand(battleSnapshot);
    }

    @Override
    public void showPile(String pileTitle, List<Card> pileCards) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(pileTitle);
        dialog.setContentText(formatPile(pileCards));
        dialog.showAndWait();
    }

    private void configureLabels() {
        messageLabel.setTextFill(Color.WHITESMOKE);
        energyLabel.setTextFill(Color.GOLD);
        playerLabel.setTextFill(Color.LIGHTSKYBLUE);
        monsterLabel.setTextFill(Color.LIGHTPINK);
        monsterIntentLabel.setTextFill(Color.GOLD);
    }

    private void bindHandlers() {
        startButton.setOnAction(ignoredEvent -> startHandler.run());
        endTurnButton.setOnAction(ignoredEvent -> endTurnHandler.run());
        drawPileButton.setOnAction(ignoredEvent -> drawPileHandler.run());
        discardPileButton.setOnAction(ignoredEvent -> discardPileHandler.run());
    }

    private void buildLayout() {
        startButton.setStyle(ACTION_BUTTON_STYLE);
        endTurnButton.setStyle(ACTION_BUTTON_STYLE);
        HBox actions = new HBox(VIEW_PADDING, startButton, endTurnButton, energyLabel, drawPileButton, discardPileButton);
        VBox playerPanel = new VBox(VIEW_PADDING, playerLabel, playerBar);
        VBox monsterPanel = new VBox(VIEW_PADDING, monsterLabel, monsterBar, monsterIntentLabel);
        playerPanel.setStyle(FIGHTER_PANEL_STYLE);
        monsterPanel.setStyle(FIGHTER_PANEL_STYLE);
        HBox fighters = new HBox(VIEW_PADDING, playerPanel, monsterPanel);
        fighters.setAlignment(Pos.CENTER);
        handContainer.setAlignment(Pos.CENTER);
        setTop(new VBox(VIEW_PADDING, actions, messageLabel));
        setCenter(fighters);
        setBottom(handContainer);
    }

    private void updateLabels(BattleSnapshot battleSnapshot) {
        messageLabel.setText(battleSnapshot.message());
        energyLabel.setText("Energy: " + battleSnapshot.energy());
        playerLabel.setText(formatFighterStats("Player", battleSnapshot.playerHealth(), battleSnapshot.playerMaxHealth(), battleSnapshot.playerBlock()));
        monsterLabel.setText(formatFighterStats("Monster", battleSnapshot.monsterHealth(), battleSnapshot.monsterMaxHealth(), battleSnapshot.monsterBlock()));
        monsterIntentLabel.setText("Turn " + battleSnapshot.turn() + " intent: " + battleSnapshot.monsterIntent());
        playerBar.setProgress(healthRatio(battleSnapshot.playerHealth(), battleSnapshot.playerMaxHealth()));
        monsterBar.setProgress(healthRatio(battleSnapshot.monsterHealth(), battleSnapshot.monsterMaxHealth()));
        drawPileButton.setText("Draw: " + battleSnapshot.drawPileSize());
        discardPileButton.setText("Discard: " + battleSnapshot.discardPileSize());
        startButton.setText(battleSnapshot.status() == GameStatus.READY ? "Start" : "Restart");
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
        cardButton.setStyle(cardStyle(handCard.type()));
        cardButton.setDisable(battleSnapshot.status() != GameStatus.PLAYING || battleSnapshot.energy() < handCard.cost());
        cardButton.setOnAction(ignoredEvent -> cardPlayedHandler.accept(cardIndex));
        return cardButton;
    }

    private String formatFighterStats(String fighterName, int health, int maximumHealth, int block) {
        return fighterName + " HP: " + health + "/" + maximumHealth + "\nDefense: " + block;
    }

    private double healthRatio(int health, int maximumHealth) {
        return (double) health / maximumHealth;
    }

    private String cardStyle(CardType cardType) {
        return cardType == CardType.ATTACK ? ATTACK_CARD_STYLE : DEFEND_CARD_STYLE;
    }

    private String formatPile(List<Card> pileCards) {
        StringBuilder result = new StringBuilder();
        for (Card pileCard : pileCards) {
            result.append(pileCard.name()).append('\n');
        }
        return result.toString();
    }
}