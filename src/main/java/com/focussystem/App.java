package com.focussystem;

import com.focussystem.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Focus System - Study Manager");

        MainController mainController = new MainController();
        Scene scene = mainController.getPrimaryScene();

        stage.setScene(scene);
        stage.show();
    }
}