package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.model.BattleSnapshot;
import org.example.model.Card;
import org.example.model.CardType;
import org.example.model.GameStatus;

import java.util.function.Consumer;
import java.util.List;

/** JavaFX-only rendering and input widgets. Game rules live in model. */
public final class BattleView extends BorderPane implements BattleViewPort {
    private final Label message = new Label();
    private final Label energy = new Label();
    private final Label playerHp = new Label();
    private final Label monsterHp = new Label();
    private final Label playerBlock = new Label();
    private final Label intent = new Label();
    private final ProgressBar playerBar = new ProgressBar();
    private final ProgressBar monsterBar = new ProgressBar();
    private final HBox handBox = new HBox(12);
    private final Button startButton = new Button("开始战斗");
    private final Button endTurnButton = new Button("结束回合");
    private final Button drawPileButton = new Button();
    private final Button discardPileButton = new Button();
    private Runnable onStart = () -> {};
    private Runnable onEndTurn = () -> {};
    private Runnable onDrawPileViewed = () -> {};
    private Runnable onDiscardPileViewed = () -> {};
    private Consumer<Integer> onCardPlayed = ignored -> {};

    public BattleView() {
        setPadding(new Insets(22));
        setStyle("-fx-background-color: linear-gradient(to bottom, #202a38, #111820);");
        message.setFont(Font.font(18));
        message.setTextFill(Color.WHITESMOKE);
        energy.setTextFill(Color.web("#ffd866"));
        energy.setFont(Font.font(20));
        startButton.setOnAction(e -> onStart.run());
        endTurnButton.setOnAction(e -> onEndTurn.run());
        drawPileButton.setOnAction(e -> onDrawPileViewed.run());
        discardPileButton.setOnAction(e -> onDiscardPileViewed.run());

        HBox top = new HBox(18, startButton, endTurnButton, energy, drawPileButton, discardPileButton);
        top.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(9, top, message);
        setTop(header);

        VBox player = fighterBox("勇者", playerHp, playerBlock, playerBar, "#58c4ff");
        VBox monster = fighterBox("史莱姆", monsterHp, intent, monsterBar, "#ff7272");
        HBox arena = new HBox(100, player, monster);
        arena.setAlignment(Pos.CENTER);
        setCenter(arena);

        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(18, 0, 0, 0));
        setBottom(handBox);
    }

    public void setOnStart(Runnable action) { onStart = action; }
    public void setOnEndTurn(Runnable action) { onEndTurn = action; }
    public void setOnCardPlayed(Consumer<Integer> action) { onCardPlayed = action; }
    public void setOnDrawPileViewed(Runnable action) { onDrawPileViewed = action; }
    public void setOnDiscardPileViewed(Runnable action) { onDiscardPileViewed = action; }

    public void render(BattleSnapshot state) {
        message.setText(state.message());
        energy.setText("能量：" + state.energy() + " / 3");
        drawPileButton.setText("抽牌堆（" + state.drawPileSize() + "）");
        discardPileButton.setText("弃牌堆（" + state.discardPileSize() + "）");
        playerHp.setText("生命 " + state.playerHealth() + " / " + state.playerMaxHealth());
        playerBlock.setText("格挡：" + state.playerBlock());
        monsterHp.setText("生命 " + state.monsterHealth() + " / " + state.monsterMaxHealth());
        intent.setText("格挡：" + state.monsterBlock() + "\n意图：" + state.monsterIntent());
        playerBar.setProgress((double) state.playerHealth() / state.playerMaxHealth());
        monsterBar.setProgress((double) state.monsterHealth() / state.monsterMaxHealth());
        startButton.setText(state.status() == GameStatus.READY ? "开始战斗" : "重新开始");
        boolean playing = state.status() == GameStatus.PLAYING;
        endTurnButton.setDisable(!playing);
        handBox.getChildren().clear();
        for (int i = 0; i < state.hand().size(); i++) {
            int index = i;
            Card card = state.hand().get(i);
            Button cardButton = new Button(card.name() + "\n" + card.description() + "\n消耗 " + card.cost());
            cardButton.setWrapText(true);
            cardButton.setPrefSize(142, 116);
            cardButton.setDisable(!playing || state.energy() < card.cost());
            String color = card.type() == CardType.ATTACK ? "#b94c52" : "#377db3";
            cardButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            cardButton.setOnAction(e -> onCardPlayed.accept(index));
            handBox.getChildren().add(cardButton);
        }
    }

    /** Displays card data supplied by the controller; this method changes no game state. */
    public void showPile(String title, List<Card> cards) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(title);
        dialog.setHeaderText(title + "：" + cards.size() + " 张");
        if (cards.isEmpty()) {
            dialog.setContentText("当前没有卡牌");
        } else {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                content.append(i + 1).append(". ").append(card.name())
                        .append(" - ").append(card.description()).append('\n');
            }
            dialog.setContentText(content.toString());
        }
        dialog.showAndWait();
    }

    private VBox fighterBox(String title, Label hp, Label detail, ProgressBar bar, String accent) {
        Label name = new Label(title);
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font(24));
        hp.setTextFill(Color.WHITE);
        detail.setTextFill(Color.LIGHTGRAY);
        bar.setPrefWidth(240);
        bar.setStyle("-fx-accent: " + accent + ";");
        VBox box = new VBox(9, name, hp, bar, detail);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(28));
        box.setMinWidth(300);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 14;");
        return box;
    }
}
