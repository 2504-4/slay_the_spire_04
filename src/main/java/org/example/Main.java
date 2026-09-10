package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.GameController;
import org.example.model.BattleGame;
import org.example.model.DefaultBattleGame;
import org.example.view.BattleView;

/** JavaFX application entry point. */
public class Main extends Application {
    private static final String WINDOW_TITLE = "单关卡牌战斗 Demo";
    private static final double INITIAL_WINDOW_WIDTH = 960;
    private static final double INITIAL_WINDOW_HEIGHT = 640;
    private static final double MINIMUM_WINDOW_WIDTH = 780;
    private static final double MINIMUM_WINDOW_HEIGHT = 540;

    @Override
    public void start(Stage stage) {
        BattleView battleView = new BattleView();
        BattleGame battleGame = new DefaultBattleGame();
        new GameController(battleGame, battleView);
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(new Scene(battleView, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT));
        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
        stage.show();
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }
}