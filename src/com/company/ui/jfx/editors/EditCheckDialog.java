package com.company.ui.jfx.editors;

import com.company.check.Check;
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
import java.util.Optional;

/**
 * Created by Александр on 09.04.2017.
 */
public class EditCheckDialog extends Dialog<Check> {

    public void open(Check item, Window parentWindow, EditCallback<Check> editCallback) {
        setTitle("Редактирование проверки.");
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("editCheck.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 400);
            Stage stage = new Stage();
            stage.initOwner(parentWindow);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            EditCheckDialogController controller = loader.<EditCheckDialogController>getController();
            getDialogPane().setContent(root);

            controller.edit(item);

            ButtonType saveButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

            Node saveButton = getDialogPane().lookupButton(saveButtonType);
            saveButton.setDisable(true);

            controller.addChangeHandler(new EditCheckDialogController.ChangeHandler() {
                @Override
                public void onChange(boolean isNewValueEmpty) {
                    saveButton.setDisable(isNewValueEmpty);
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
                editCallback.onFinish(edited.get());
            } else {
                editCallback.onCancel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
