package com.company.ui.jfx.tabs.admin;

import com.company.ChecksService;
import com.company.UpdateService;
import com.company.check.Check;
import com.company.check.CheckType;
import com.company.check.ValidationMethod;
import com.company.ui.jfx.tabs.TabController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Александр on 01.04.2017.
 */
public class ChecksController implements TabController {
    public TableView<Check> gridView;
    public TableColumn<Check, String> nameColumn;
    public TableColumn<Check, CheckType> typeColumn;
    public TableColumn<Check, ValidationMethod> validationTypeColumn;
    public TableColumn<Check, String> queryTextColumn;
    public TableColumn<Check, String> messageTextColumn;
    public Button refreshButton;

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {

        //nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));

        /* Инициализация таблицы проверок с двумя столбцами.

         */
        nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Check, String> param) {
                return new SimpleStringProperty(param.getValue().getName());
            }
        });
        typeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, CheckType>, ObservableValue<CheckType>>() {
            @Override
            public ObservableValue<CheckType> call(TableColumn.CellDataFeatures<Check, CheckType> param) {
                return new SimpleObjectProperty<CheckType>(param.getValue().getType());
            }
        });

        validationTypeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, ValidationMethod>, ObservableValue<ValidationMethod>>() {
            @Override
            public ObservableValue<ValidationMethod> call(TableColumn.CellDataFeatures<Check, ValidationMethod> param) {
                return new SimpleObjectProperty<ValidationMethod>(param.getValue().getValidationMethod());
            }
        });

        queryTextColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Check, String> param) {
                return new SimpleStringProperty(param.getValue().getQueryText());
            }
        });

        messageTextColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Check, String> param) {
                return new SimpleStringProperty(param.getValue().getMessageText());
            }
        });

        refreshButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                load();
                System.out.println("Refreshed checks table.");
            }
        });
    }

    @Override
    public void load() {
        List<Check> checks = new ArrayList<>();
        try {
            checks = ChecksService.loadChecks();
        } catch (SQLException e) {
            System.out.println("Failed to load checks: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Нe удалось загрузить список проверок. Ошибка: " + e.getMessage(), ButtonType.OK).show();
        }
        gridView.getItems().clear();
        gridView.getItems().addAll(checks);
        System.out.println("Loaded " + checks.size() + " checks.");
    }
}
