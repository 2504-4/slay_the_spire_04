package org;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controller.GameController;
import org.view.GameView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameController controller = new GameController();
        controller.startGame();

        GameView view = new GameView(controller);
        view.update();

        Scene scene = new Scene(view.getRoot(), 1000, 760);
        primaryStage.setTitle("Slay the Spire - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}