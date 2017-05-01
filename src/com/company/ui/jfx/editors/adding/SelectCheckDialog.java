package com.company.ui.jfx.editors.adding;

import com.company.check.Check;
import com.company.ui.jfx.editors.EditCallback;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Александр on 01.05.2017.
 */
public class SelectCheckDialog extends Dialog<Check>{

    public void open(Window parentWindow, List<Check> checks, EditCallback<Check> selectCallback) {
        setTitle("Выбор проверки.");
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("selectCheck.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 400);
            Stage stage = new Stage();
            stage.initOwner(parentWindow);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            SelectCheckDialogController controller = loader.<SelectCheckDialogController>getController();
            getDialogPane().setContent(root);

            ButtonType saveButtonType = new ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

            Node saveButton = getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);
            controller.setChecks(checks);

            controller.addSelectionChangeHandler(new SelectCheckDialogController.SelectionChangeHandler() {
                @Override
                public void onSelectionChange(Check check) {
                    saveButton.setDisable(check == null);
                }
            });

            setResultConverter(new Callback<ButtonType, Check>() {
                @Override
                public Check call(ButtonType param) {
                    if (param == saveButtonType) {
                        return controller.getCurrentValue();
                    } else {
                        return null;
                    }
                }
            });

            Optional<Check> edited = showAndWait();
            if (edited.isPresent()) {
                selectCallback.onFinish(edited.get());
            } else {
                selectCallback.onCancel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
