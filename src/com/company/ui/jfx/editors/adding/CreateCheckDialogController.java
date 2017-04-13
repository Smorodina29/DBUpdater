package com.company.ui.jfx.editors.adding;

import com.company.Utils;
import com.company.check.Check;
import com.company.check.CheckType;
import com.company.check.ValidationMethod;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Александр on 01.04.2017.
 */
public class CreateCheckDialogController implements Initializable {

    public TextField nameField;
    public ComboBox<CheckType> typeComboBox;
    public ComboBox<ValidationMethod> validationMethodBox;
    public TextArea queryTextArea;
    public TextArea messageTextArea;
    private ArrayList<ChangeHandler> changeHandlerList = new ArrayList<ChangeHandler>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeComboBox.getItems().addAll(CheckType.values());
        validationMethodBox.getItems().addAll(ValidationMethod.values());

        ChangeListener<String> stringValueChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                onChange(oldValue, newValue);
            }
        };

        nameField.textProperty().addListener(stringValueChangeListener);
        queryTextArea.textProperty().addListener(stringValueChangeListener);
        messageTextArea.textProperty().addListener(stringValueChangeListener);

        typeComboBox.valueProperty().addListener(new ChangeListener<CheckType>() {
            @Override
            public void changed(ObservableValue<? extends CheckType> observable, CheckType oldValue, CheckType newValue) {
                onChange(oldValue, newValue);
            }
        });
        validationMethodBox.valueProperty().addListener(new ChangeListener<ValidationMethod>() {
            @Override
            public void changed(ObservableValue<? extends ValidationMethod> observable, ValidationMethod oldValue, ValidationMethod newValue) {
                onChange(oldValue,newValue);
            }
        });

        //focus field
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                nameField.requestFocus();
            }
        });
    }

    /*
    * Один метод для обработки изменения любого поля.
    * */
    private void onChange(Object oldValue, Object newValue) {


        boolean isAllFilled = Utils.isNotEmpty(queryTextArea.getText())
                && Utils.isNotEmpty(nameField.getText())
                && Utils.isNotEmpty(messageTextArea.getText())
                && typeComboBox.getValue() != null
                && validationMethodBox.getValue() != null;
        System.out.println("Changed \'" + oldValue + "\' --> \'" + newValue + "\'. All attributes field = " + isAllFilled);

        for (ChangeHandler handler : changeHandlerList) {
            handler.onChange(isAllFilled);
        }
    }

    public Check getCurrentValue() {
        return Check.create(null, queryTextArea.getText(), nameField.getText(), messageTextArea.getText(), typeComboBox.getValue(), validationMethodBox.getValue());
    }

    public void addChangeHandler(ChangeHandler handler) {
        changeHandlerList.add(handler);
    }

    interface ChangeHandler{
        void onChange(boolean isAllFilled);
    }
}
