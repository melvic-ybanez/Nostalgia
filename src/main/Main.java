package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sun.awt.PlatformFont;
import views.MainView;

/**
 * Created by melvic on 9/12/18.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainView root = new MainView();
        primaryStage.setTitle("Nostalgia");
        primaryStage.setScene(new Scene(root));
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