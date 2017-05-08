package com.company.ui.jfx.tabs.admin;

import com.company.AppConfig;
import com.company.ConnectionProvider;
import com.company.Utils;
import com.company.ui.jfx.tabs.TabController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Александр on 01.04.2017.
 */
public class SettingsController implements TabController, Initializable {


    public Button saveButton;
    public Button cancelButton;


    public TextField dbUrlField;
    public TextField dbUserField;
    public PasswordField dbUserPwdField;

    private AppConfig initial;
    private AppConfig current;

    public Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbUrlField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Changed db url \'" + oldValue + "\' --> \'" + newValue + "\'");
                current.setDbUrl(newValue);
                onPropertyChange();
            }
        });

        dbUserField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Changed user \'" + oldValue + "\' --> \'" + newValue + "\'");
                current.setDbUser(newValue);
                onPropertyChange();
            }
        });

        dbUserPwdField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Changed pwd \'" + oldValue + "\' --> \'" + newValue + "\'");
                current.setDbUser(newValue);
                onPropertyChange();
            }
        });

    }

    private void onPropertyChange() {
        saveButton.setDisable(initial.equals(current));
    }

    @Override
    public void load() {
        initial = ConnectionProvider.get().getAppConfig();
        current = initial.copy();
        dbUrlField.setText(initial.getDbConnectString());
        dbUserField.setText(initial.getDbUserid());
        dbUserPwdField.setText(initial.getDbPassword());
        statusLabel.setVisible(false);

//        check(current);
    }

    public void saveChanges(ActionEvent actionEvent) {
        if (Utils.isEmpty(current.getDbUserid())) {
            new Alert(Alert.AlertType.WARNING, "Необходимо указать имя пользователя.", ButtonType.OK).show();
        } else if (Utils.isEmpty(current.getDbConnectString())) {
            new Alert(Alert.AlertType.WARNING, "Необходимо указать адрес БД.", ButtonType.OK).show();
        } else {
            //everything is fine
            try {
                ConnectionProvider.get().updateConfig(current);
                initial = current;
                saveButton.setDisable(true);
            } catch (IOException e) {
                System.out.println("Failed to save updated config file: " + e.getMessage());
                new Alert(Alert.AlertType.ERROR, "Не удалось сохранить файл конфигурации: " + e.getMessage(), ButtonType.OK).show();
                e.printStackTrace();
            }
        }
    }

    public void cancel(ActionEvent actionEvent) {
        load();
    }

    public void checkConnection(ActionEvent actionEvent) {
        check(current);
    }

    private void check(AppConfig config) {
        statusLabel.setText("Проверка");
        statusLabel.setTextFill(Color.BLACK);
        statusLabel.setVisible(true);
        try {
            if (ConnectionProvider.get().check(config)) {
                statusLabel.setText("Успешно!");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("Неудача.");
                statusLabel.setTextFill(Color.RED);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            statusLabel.setText("Неудача: " + throwable.getMessage());
            statusLabel.setTextFill(Color.RED);
        }
    }

}
