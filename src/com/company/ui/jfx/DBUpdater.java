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

public class DBUpdater extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("dbupdater.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Утилита импорта данных");
        primaryStage.setScene(new Scene(root, 800, 500));
        Image e = new Image("icons/icon.png");
        primaryStage.getIcons().add(e);
        AppController controller = fxmlLoader.<AppController>getController();

        //start login
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.initOwner(root.getScene().getWindow());
        loginDialog.initModality(Modality.APPLICATION_MODAL);

        primaryStage.show();

        loginDialog.open(new AuthenticationCallback() {
            @Override
            public void onAuth(boolean isAuthenticated, User user) {
                if (isAuthenticated) {
                    System.out.println("Logged in as `" + user.getName() + "'(" + user.getRole() + ")");
                    controller.onUserLogin(user);
                } else {
                    System.out.println("Cancelled authentication. Closing...");
                    Platform.exit();
                }
            }
        });
    }
}
