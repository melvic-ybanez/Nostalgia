package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}