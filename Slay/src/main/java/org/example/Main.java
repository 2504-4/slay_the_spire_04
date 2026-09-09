package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.GameController;
import org.example.view.GameView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameController controller = new GameController();
        controller.startGame(); // 确保GameController有此方法

        GameView view = new GameView(controller);
        view.update();

        Scene scene = new Scene(view.getRoot(), 800, 600);
        primaryStage.setTitle("Slay the Spire - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}