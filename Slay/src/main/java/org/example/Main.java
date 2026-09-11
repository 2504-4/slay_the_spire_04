package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.GameController;
import org.example.view.GameView;
import org.example.view.MenuView;

public class Main extends Application {

    private static final double SCENE_WIDTH  = 900;
    private static final double SCENE_HEIGHT = 820;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 首屏显示主菜单；点"开始游戏"通过回调切到战斗场景
        MenuView menuView = new MenuView(this::showGameScene);
        Scene menuScene = new Scene(menuView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.setTitle("Slay the Spire - JavaFX");
        primaryStage.setScene(menuScene);
        primaryStage.setMinWidth(SCENE_WIDTH);
        primaryStage.setMinHeight(SCENE_HEIGHT);
        primaryStage.show();
    }

    /** 切换到战斗场景：每次都新建 Controller，保证是全新的一局 */
    private void showGameScene() {
        GameView gameView = new GameView(new GameController(), GameController::new);
        gameView.update();
        Scene gameScene = new Scene(gameView.getRoot(), SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(gameScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
