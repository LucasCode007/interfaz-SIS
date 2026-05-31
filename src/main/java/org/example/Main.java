package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.ui.MainMenuUI;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        new MainMenuUI().show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}