package com.github.melvic.nostalgia.main;

import com.github.melvic.nostalgia.views.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Created by melvic on 9/12/18.
 *
 * TODO: Convert this into Scala code
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        setupFontProperties();

        MainView root = new MainView();

        Scene scene = new Scene(root.delegate());
        scene.getStylesheets().add(Resources.styleSheets("bootstrap3"));

        primaryStage.setTitle("Nostalgia");
        primaryStage.setScene(scene);
        primaryStage.getIcons().addAll(new Image(Resources.image("pieces/white_knight.png")));
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

    /**
     * Fixes the font rendering on linux-based systems
     */
    public static void setupFontProperties() {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
    }
}