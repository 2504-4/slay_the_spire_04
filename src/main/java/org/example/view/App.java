package org.example.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Day01 - 10×20网格演示
 * 核心：模型(board) ↔ 视图(cells) 分离映射
 */
public class App extends Application {

    // 网格常量：20行，10列，每个格子30像素
    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final int CELL_SIZE = 30;

    // 模型层：只存数据，0=空格子，1=填充格子
    private final int[][] board = new int[ROWS][COLS];

    // 视图层：每个格子对应一个界面方块
    private final Rectangle[][] cells = new Rectangle[ROWS][COLS];

    @Override
    public void start(Stage stage) {
        // 1. 创建画板容器
        Pane root = new Pane();

        // 2. 初始化所有界面格子
        initCells(root);

        // 演示：第5行第5列填充成蓝色
        board[5][6] = 1;

        // 3. 渲染：把数据同步到界面颜色
        render();

        // 4. 创建场景并显示窗口
        Scene scene = new Scene(root, COLS * CELL_SIZE, ROWS * CELL_SIZE);
        stage.setTitle("Day01 - 10×20网格");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 初始化所有格子的界面和颜色
     */
    private void initCells(Pane root) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // 创建矩形：x坐标、y坐标、宽度、高度
                Rectangle rect = new Rectangle(
                        c * CELL_SIZE,
                        r * CELL_SIZE,
                        CELL_SIZE - 1,
                        CELL_SIZE - 1
                );
                // ===== 这里设置颜色：边框浅灰色，默认填充白色 =====
                rect.setStroke(Color.LIGHTGRAY);
                rect.setFill(Color.WHITE);

                root.getChildren().add(rect); // 加到画板
                cells[r][c] = rect;           // 存入视图数组
            }
        }
    }

    /**
     * 渲染方法：模型数据 → 界面颜色
     * 数据改了，调用一次这个方法，界面颜色就自动更新
     */
    private void render() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // 0=白色空格子，1=蓝色填充格子
                cells[r][c].setFill(
                        board[r][c] == 0 ? Color.WHITE : Color.STEELBLUE
                );
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}