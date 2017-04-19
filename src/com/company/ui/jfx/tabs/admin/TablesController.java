package com.company.ui.jfx.tabs.admin;

import com.company.ChecksService;
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
public class TablesController implements TabController, Initializable {


    public Button moveRight;
    public Button moveLeft;
    public ListView<String> allTablesListView;
    public ListView<String> updatableTablesListView;
    public Button saveButton;
    public Button refreshButton;

    private Set<String> disableUpdatePatch = new HashSet<>();
    private Set<String> enableUpdatePatch = new HashSet<>();
    private List<String> initialAllTablesNamesList;
    private Set<String> initialUpdatableSet;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        String buttonStyle = "-fx-font: 22 arial; -fx-base: #9effa6;";
        String baseStyle = "-fx-font: 22 arial; -fx-font-weight: bold;";
        moveRight.setStyle(baseStyle + " -fx-base: #c7ffb7;");
        moveLeft.setStyle(baseStyle + " -fx-base: #ffb7b7");

        allTablesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Selected in all " + newValue);
                moveRight.setDisable(newValue == null || newValue.isEmpty());
            }
        });

        updatableTablesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Selected in initialUpdatableSet " + newValue);
                moveLeft.setDisable(newValue == null || newValue.isEmpty());
            }
        });
    }

    @Override
    public void load() {
        initialAllTablesNamesList = UpdateService.getAllTablesNames();
        initialUpdatableSet = UpdateService.getTableNamesForUpdate();

        initialAllTablesNamesList.removeAll(initialUpdatableSet);//filter already initialUpdatableSet

        allTablesListView.getItems().clear();
        allTablesListView.getItems().addAll(initialAllTablesNamesList);

        updatableTablesListView.getItems().clear();
        updatableTablesListView.getItems().addAll(initialUpdatableSet);

        java.util.Collections.sort(allTablesListView.getItems());
        java.util.Collections.sort(updatableTablesListView.getItems());

        System.out.println("Loaded data for tables tab. Updatable tables:" + initialUpdatableSet);
        clearDataPatch();
        saveButton.setDisable(isDataPatchEmpty());
    }

    private boolean isDataPatchEmpty() {
        return disableUpdatePatch.isEmpty() && enableUpdatePatch.isEmpty();
    }

    private void clearDataPatch() {
        disableUpdatePatch.clear();
        enableUpdatePatch.clear();
        System.out.println("Cleared data patch.");
    }

    public void saveChanges(ActionEvent event) {
        if (isDataPatchEmpty()) {
            System.out.println("Clicked save, although data patch is empty. ");
            saveButton.setDisable(true);
        } else {
            System.out.println("Start applying patch: disabled=" + disableUpdatePatch + "; enabled=" + enableUpdatePatch);

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
            }
        }
    }

    public void refresh(ActionEvent event) {
        load();
        System.out.println("Refreshed tables tab.");
    }

    public void moveRight(ActionEvent actionEvent) {
        String selectedTableName = allTablesListView.getSelectionModel().selectedItemProperty().getValue();
        disableUpdatePatch.remove(selectedTableName);
        boolean wasUpdatableOnStart = !initialUpdatableSet.contains(selectedTableName);
        if (wasUpdatableOnStart) {
            /* Необходимо исключить лишнюю работу.
            Пример: таблица была доступна для обновления, сначала мы запретили ее обновление, потом разрешили.
            В результате, для этой таблицы ничего не поменялось.
            Схожая ситуация и с запретом на редактирование.
             */
            enableUpdatePatch.add(selectedTableName);
        }
        allTablesListView.getItems().remove(selectedTableName);
        updatableTablesListView.getItems().add(selectedTableName);
        System.out.println("Marked " + selectedTableName + " as ENABLED for update.");

        java.util.Collections.sort(updatableTablesListView.getItems());
        saveButton.setDisable(isDataPatchEmpty());
    }

    public void moveLeft(ActionEvent actionEvent) {
        String selectedTableName = updatableTablesListView.getSelectionModel().selectedItemProperty().getValue();
        enableUpdatePatch.remove(selectedTableName);
        if (!initialAllTablesNamesList.contains(selectedTableName)) {
            disableUpdatePatch.add(selectedTableName);
        }

        updatableTablesListView.getItems().remove(selectedTableName);
        allTablesListView.getItems().add(selectedTableName);
        System.out.println("Marked " + selectedTableName + " as DISABLED for update.");

        java.util.Collections.sort(allTablesListView.getItems());
        saveButton.setDisable(isDataPatchEmpty());
    }
}
