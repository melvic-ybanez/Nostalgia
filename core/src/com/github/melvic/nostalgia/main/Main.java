package com.github.melvic.nostalgia.main;

import com.github.melvic.nostalgia.views.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Created by melvic on 9/12/18.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainView root = new MainView();
        primaryStage.setTitle("Nostalgia");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().addAll(new Image(Resources.pathOf("pieces/white_knight.png")));
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.setOnCloseRequest(value -> exit());
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void exit() {
        Platform.exit();
        System.exit(0);
    }
}