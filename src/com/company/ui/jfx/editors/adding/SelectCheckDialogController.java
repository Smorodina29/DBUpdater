package com.company.ui.jfx.editors.adding;

import com.company.check.Check;
import com.company.check.CheckType;
import com.company.check.ValidationMethod;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Александр on 01.05.2017.
 */
public class SelectCheckDialogController implements Initializable {

    public TableColumn<Check, String> checkNameColumn;
    public TableColumn<Check, CheckType> checkTypeColumn;
    public TableColumn<Check, ValidationMethod> validationTypeColumn;
    public TableView<Check> checksView;
    private ArrayList<SelectionChangeHandler> handlers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Check, String> param) {
                return new SimpleStringProperty(param.getValue().getName());
            }
        });

        checkTypeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, CheckType>, ObservableValue<CheckType>>() {
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

        checksView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Check>() {
            @Override
            public void changed(ObservableValue<? extends Check> observable, Check oldValue, Check newValue) {
                System.out.println("Changes selection  \'" + oldValue + "\' -- > \'" + newValue + "\'" );
                for (SelectionChangeHandler handler : handlers) {
                    if (handler != null) {
                        handler.onSelectionChange(newValue);
                    }
                }
            }
        });
    }

    public void addSelectionChangeHandler(SelectionChangeHandler handler) {
        handlers.add(handler);
    }

    public Check getCurrentValue() {
        return checksView.getSelectionModel().getSelectedItem();
    }

    public void setChecks(List<Check> checks) {
        checksView.getItems().addAll(checks);
    }

    interface SelectionChangeHandler {
        void onSelectionChange(Check check);
    }
}
