package com.company.ui.jfx.login;

import com.company.UserAdminService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.net.URL;
import java.util.Optional;

/**
 * Created by Александр on 29.03.2017.
 */
public class LoginDialog extends Dialog<User> {

    private final TextField usernameField;
    private final Label wrongAuthDataLabel;
    private boolean isErrorLabelVisible;

    public LoginDialog() {
        setTitle("Вход в систему");
        setContentText("Представьтесь:");

        wrongAuthDataLabel = new Label("Неправильный логин/пароль.");
        wrongAuthDataLabel.setTextFill(Color.RED);

        // Set the icon (must be included in the project).
        URL resource = this.getClass().getClassLoader().getResource("images/login.png");
        ImageView graphic = new ImageView(resource.toString());
        setGraphic(graphic);

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Войти", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);


        // Create the usernameField and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(50, 50, 10, 40));

        usernameField = new TextField();
        usernameField.setPromptText("Логин");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        grid.add(new Label("Пользователь:"),0,0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Пароль:"),0,1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label(), 0, 2, 2, 1);

        // Enable/Disable login button depending on whether a usernameField was entered.
        Node loginButton = getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        loginButton.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = usernameField.getText();
                wrongAuthDataLabel.setVisible(false);
                loginButton.setDisable(true);
                usernameField.setDisable(true);
                passwordField.setDisable(true);

                boolean authenticated = UserAdminService.isAuthenticated(username, passwordField.getText());
                System.out.println("Access " + (authenticated? "GRANTED" : "DENIED") + " for '"  + username + "' " );
                if (!authenticated) {
                    event.consume();
                    System.out.println("isErrorLabelVisible=" + isErrorLabelVisible);
                    if (!isErrorLabelVisible) {
                        isErrorLabelVisible = true;
                        grid.add(wrongAuthDataLabel, 0, 2, 2, 1);
                        System.out.println("Showed error label");
                    } else {
                        System.out.println("Already shows error label.");
                    }
                    wrongAuthDataLabel.setVisible(true);
                }
                loginButton.setDisable(false);
                usernameField.setDisable(false);
                passwordField.setDisable(false);
            }
        });

        usernameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                loginButton.setDisable(newValue.trim().isEmpty());
            }
        });

        getDialogPane().setContent(grid);

        setResultConverter(new Callback<ButtonType, User>() {
            @Override
            public User call(ButtonType buttonPressed) {
                System.out.println("Pressed:" + buttonPressed);
                if (buttonPressed == loginButtonType) {
                    String username = usernameField.getText();
                    return new User(username, UserAdminService.getRole(username));
                }
                return null;
            }
        });
    }


    public void open(AuthenticationCallback callback) {
        // Request focus on the usernameField field by default.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                usernameField.requestFocus();
            }
        });
        Optional<User> resultParam = showAndWait();
        callback.onAuth(resultParam.isPresent(), resultParam.orElse(null));
    }
}
