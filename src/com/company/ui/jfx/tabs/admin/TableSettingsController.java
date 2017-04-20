package com.company.ui.jfx.tabs.admin;

import com.company.UpdateService;
import com.company.ui.jfx.tabs.TabController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by Александр on 01.04.2017.
 */
public class TableSettingsController implements TabController, Initializable {


    public Button saveButton;
    public Button refreshButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        String buttonStyle = "-fx-font: 22 arial; -fx-base: #9effa6;";
    }

    @Override
    public void load() {

    }

    private boolean isDataPatchEmpty() {
        return true/*disableUpdatePatch.isEmpty() && enableUpdatePatch.isEmpty()*/;
    }

    private void clearDataPatch() {
//        disableUpdatePatch.clear();
//        enableUpdatePatch.clear();
        System.out.println("Cleared data patch.");
    }

    public void saveChanges(ActionEvent event) {
        if (isDataPatchEmpty()) {
            System.out.println("Clicked save, although data patch is empty. ");
            saveButton.setDisable(true);
        } else {
            /*System.out.println("Start applying patch: disabled=" + disableUpdatePatch + "; enabled=" + enableUpdatePatch);

            try {
                UpdateService.disableUpdateFor(disableUpdatePatch);
                disableUpdatePatch.clear();
                UpdateService.enableUpdateFor(enableUpdatePatch);
                enableUpdatePatch.clear();
                System.out.println("Applied patch.");
                load();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Произошла ошибка во время сохранения:" + e.getMessage(), ButtonType.OK).show();
                System.out.println("Failed to save patch: " + e.getMessage());
                e.printStackTrace();
            }*/
        }
    }

    public void refresh(ActionEvent event) {
        load();
        System.out.println("Refreshed tables tab.");
    }
}
