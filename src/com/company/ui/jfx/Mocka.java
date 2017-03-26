package com.company.ui.jfx;/**
 * Created by Александр on 20.03.2017.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Mocka extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Утилита импорта данных");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }
}
