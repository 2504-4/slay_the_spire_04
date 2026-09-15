package org;

import javafx.application.Application;
import org.view.GameApplication;

/** 普通 Java 入口，支持 Maven 和 IDEA 的类路径启动方式。 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(GameApplication.class, args);
    }
}
