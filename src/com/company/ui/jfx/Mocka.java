package com.company.ui.jfx;/**
 * Created by Александр on 20.03.2017.
 */

import com.company.ui.jfx.login.AuthenticationCallback;
import com.company.ui.jfx.login.LoginDialog;
import com.company.ui.jfx.login.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
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

        Image e = new Image("icons/icon.png");
        primaryStage.getIcons().add(e);

        LoginDialog dialog = new LoginDialog();

        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.open(new AuthenticationCallback() {
            @Override
            public void onAuth(boolean isAuthenticated, User user) {
                if (isAuthenticated) {
                    System.out.println("Logged in as `" + user.getName() + "'(" + user.getRole() + ")");
                    //set username to label
                } else {
                    System.out.println("Cancelled authentication. Closing...");
                    Platform.exit();
                }
            }
        });
    }
}
