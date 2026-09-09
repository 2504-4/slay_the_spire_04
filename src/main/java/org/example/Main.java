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
    @Override
    public void start(Stage stage) {
        BattleView view = new BattleView();
        BattleGame game = new DefaultBattleGame();
        new GameController(game, view);
        stage.setTitle("单关卡牌战斗 Demo");
        stage.setScene(new Scene(view, 960, 640));
        stage.setMinWidth(780);
        stage.setMinHeight(540);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
