package com.company.ui.jfx.tabs.admin;

import com.company.ChecksService;
import com.company.Column;
import com.company.UpdateService;
import com.company.check.Check;
import com.company.check.CheckType;
import com.company.ui.jfx.editors.EditCallback;
import com.company.ui.jfx.editors.adding.SelectCheckDialog;
import com.company.ui.jfx.tabs.TabController;
import com.company.ui.jfx.tabs.admin.models.ColumnModel;
import com.company.ui.jfx.tabs.admin.models.TableModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Александр on 01.04.2017.
 */
public class TableSettingsController implements TabController, Initializable {


    @FXML public Button saveButton;
    @FXML public Button refreshButton;
    @FXML public ComboBox<TableModel> tablesBox;
    public TableView<ColumnModel> columnsView;
    public TableView<Check> checksView;
    public TableColumn<ColumnModel, String> columnNameColumn;
    public TableColumn<ColumnModel, Boolean> checkboxColumn;
    public TableColumn<Check, String> checkNameColumn;
    public TableColumn<Check, CheckType> checkTypeColumn;
    public TableColumn<Check, Check> deleteCheckColumn;
    public Button addCheckButton;
    public BorderPane root;

    private Map<ColumnModel, List<Check>> deleteCheckPatch = new HashMap<>();
    private Map<ColumnModel, List<Check>> addCheckPatch = new HashMap<>();
    private List<ColumnModel> enableUpdatePatch = new ArrayList<>();
    private List<ColumnModel> disableUpdatePatch = new ArrayList<>();
    private List<Check> availableChecks = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        String buttonStyle = "-fx-font: 22 arial; -fx-base: #9effa6;";

        tablesBox.valueProperty().addListener(new ChangeListener<TableModel>() {
            @Override
            public void changed(ObservableValue<? extends TableModel> observable, TableModel oldValue, TableModel newValue) {
                System.out.println("Selection changed '" + oldValue + "' --> '" + newValue);
                if (newValue == null) {
                    columnsView.getItems().clear();
                } else {
                    columnsView.getItems().setAll(newValue.getColumns().values());
                }
            }
        });

        tablesBox.setCellFactory(new Callback<ListView<TableModel>, ListCell<TableModel>>() {
            @Override
            public ListCell<TableModel> call(ListView<TableModel> param) {
                return new ListCell<TableModel>(){
                    @Override
                    protected void updateItem(TableModel item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        tablesBox.setButtonCell(new ListCell<TableModel>(){
            @Override
            protected void updateItem(TableModel item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getName());
                }
            }
        });

        /* COLUMNS TABLE */
        checkboxColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnModel, Boolean>, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<ColumnModel, Boolean> param) {
                SimpleBooleanProperty property = new SimpleBooleanProperty(isEditable(param.getValue()));
                property.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        System.out.println("property: " + oldValue + " --> " + newValue + ", observable=" + observable + ", param:" + param.getValue());
                        onCheckboxClick(param.getValue(), newValue);
                    }
                });
                return property;
            }
        });

        checkboxColumn.setCellFactory(new Callback<TableColumn<ColumnModel, Boolean>, TableCell<ColumnModel, Boolean>>() {
            @Override
            public TableCell<ColumnModel, Boolean> call(TableColumn<ColumnModel, Boolean> param) {
                return new CheckBoxTableCell<ColumnModel, Boolean>();
            }
        });

        columnNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ColumnModel, String> param) {
                return new SimpleStringProperty(param.getValue().getName());
            }
        });

        columnsView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ColumnModel>() {
            @Override
            public void changed(ObservableValue<? extends ColumnModel> observable, ColumnModel oldValue, ColumnModel selectedColumn) {
                System.out.println("Changed selection in columns view: '" + oldValue + "' --> '" + selectedColumn + "'");
                if (selectedColumn == null) {
                    checksView.getItems().clear();
                    addCheckButton.setDisable(true);
                    System.out.println("Cleared column selection");
                } else {
                    onColumnSelect(selectedColumn);
                }
            }
        });

        /*
        need select first column?
        columnsView.getItems().addListener(new ListChangeListener<ColumnModel>() {
            @Override
            public void onChanged(Change<? extends ColumnModel> c) {
                System.out.println("Changed columns view items:" + c.getList());
                if (columnsView.getItems().size() > 0) {
                    ColumnModel first = columnsView.getItems().get(0);
                    columnsView.selectionModelProperty().getValue().select(first);
                    System.out.println("Selected first column " + first);
                } else {
                    System.out.println("Nothing to select");
                }
            }
        });*/

        /* CHECKS TABLE */
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

        deleteCheckColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, Check>, ObservableValue<Check>>() {
            @Override
            public ObservableValue<Check> call(TableColumn.CellDataFeatures<Check, Check> param) {
                return new SimpleObjectProperty<Check>(param.getValue());
            }
        });
        deleteCheckColumn.setCellFactory(new Callback<TableColumn<Check, Check>, TableCell<Check, Check>>() {
            @Override
            public TableCell<Check, Check> call(TableColumn<Check, Check> param) {
                return new TableCell<Check, Check>() {
                    final Button btn = new Button("", new ImageView(new Image("icons/delete.png")));

                    @Override
                    protected void updateItem(Check item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            btn.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    System.out.println("Clicked delete on " + item);
                                    onDeleteClick(item);
                                }
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
            }
        });
    }

    private void onColumnSelect(ColumnModel selectedColumn) {
        checksView.getItems().clear();
        List<Check> checks = selectedColumn.getChecks();

        List<Check> newChecks = addCheckPatch.get(selectedColumn);
        if (newChecks != null) {
            checks.addAll(newChecks);
        }

        List<Check> deleted = deleteCheckPatch.get(selectedColumn);
        if (deleted != null) {
            checks.removeAll(deleted);
        }
        checksView.getItems().addAll(checks);
        addCheckButton.setDisable(isNotEditable(selectedColumn));
        checksView.setDisable(isNotEditable(selectedColumn));
    }

    private boolean isEditable(ColumnModel model) {
        return !disableUpdatePatch.contains(model) && (model.isEditable() || enableUpdatePatch.contains(model));
    }

    private boolean isNotEditable(ColumnModel model) {
        return !isEditable(model);
    }

    private void onCheckboxClick(ColumnModel model, Boolean enableUpdate) {
        if (enableUpdate) {
            disableUpdatePatch.remove(model);
            boolean wasDisabled = !model.isEditable();
            if (wasDisabled) {
                enableUpdatePatch.add(model);
            }
        } else {
            enableUpdatePatch.remove(model);
            boolean wasEditable = model.isEditable();
            if (wasEditable) {
                disableUpdatePatch.add(model);
            }

        }

        if (columnsView.getSelectionModel().getSelectedItem() == model) {
            onColumnSelect(model);
        } else {
            columnsView.getSelectionModel().select(model);
        }

        saveButton.setDisable(isDataPatchEmpty());
    }

    private void onDeleteClick(Check check) {
        ColumnModel selectedItem = columnsView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            System.out.println("Warning! User clicked on delete check when there is no one column selected");
        } else {
            List<Check> addChecksList = addCheckPatch.get(selectedItem);
            if (addChecksList != null) {
                addChecksList.remove(check);
            }

            List<Check> deleted = deleteCheckPatch.get(selectedItem);
            if (deleted == null) {
                deleted = new ArrayList<>();
            }
            deleted.add(check);
            deleteCheckPatch.put(selectedItem, deleted);

            checksView.getItems().remove(check);
            System.out.println("Deleted check " + check.getId() + "(" + check.getName() + ")" + " from " + selectedItem);
        }
        saveButton.setDisable(isDataPatchEmpty());
    }

    @Override
    public void load() {
        List<TableModel> tableModels = new ArrayList<>();
        try {
            tableModels = UpdateService.getTablesForUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to load tables: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Loaded "  + tableModels.size() + " tables available for update: " + tableModels);
        tablesBox.getItems().clear();
        tablesBox.getItems().addAll(tableModels);
        columnsView.getItems().clear();
        checksView.getItems().clear();

        if (tableModels.size() > 0) {
            tablesBox.valueProperty().setValue(tablesBox.getItems().get(0));
        }
        clearDataPatch();
        saveButton.setDisable(isDataPatchEmpty());
    }

    private boolean isDataPatchEmpty() {
        boolean empty = disableUpdatePatch.isEmpty() && enableUpdatePatch.isEmpty() && addCheckPatch.isEmpty() && deleteCheckPatch.isEmpty();
        System.out.println("Datapatch is " + (empty? "EMPTY" : "NOT EMPTY"));
        return empty;
    }

    private void clearDataPatch() {
        disableUpdatePatch.clear();
        enableUpdatePatch.clear();
        addCheckPatch.clear();
        deleteCheckPatch.clear();
        System.out.println("Cleared data patch.");
    }

    public void saveChanges(ActionEvent event) {
        if (isDataPatchEmpty()) {
            System.out.println("Clicked save, although data patch is empty. ");
            saveButton.setDisable(true);
        } else {
            System.out.println("Data patch:");
            System.out.println("Enabled columns:" + enableUpdatePatch);
            System.out.println("Disabled columns:" + disableUpdatePatch);
            System.out.println("Added checks:" + addCheckPatch);
            System.out.println("Removed checks:" + deleteCheckPatch);
            System.out.println("========================");
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

    public void addCheck(ActionEvent event) {
        System.out.println("Cleicked addd");

        try {
            boolean isNotLoaded = availableChecks == null;
            if (isNotLoaded) {
                availableChecks = ChecksService.loadChecks();
            }
        } catch (SQLException e) {
            System.out.println("Failed to load checks: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Нe удалось загрузить список проверок. Ошибка: " + e.getMessage(), ButtonType.OK).show();
        }

        if (availableChecks != null) {
            SelectCheckDialog dialog = new SelectCheckDialog();


            //maybe filter already added checks in this column
            dialog.open(root.getScene().getWindow(), availableChecks, new EditCallback<Check>() {
                @Override
                public void onFinish(Check selected) {
                    System.out.println("User selected:" + selected);
                    ColumnModel selectedColumn = columnsView.getSelectionModel().getSelectedItem();
                    if (selectedColumn == null) {
                        System.out.println("Warning! User clicked on select check when there is no one column selected");
                    } else {
                        List<Check> added = addCheckPatch.get(selectedColumn);
                        if (added == null) {
                            added = new ArrayList<>();
                        }
                        added.add(selected);
                        deleteCheckPatch.put(selectedColumn, added);
                    }
                    checksView.getItems().add(selected);
                    saveButton.setDisable(isDataPatchEmpty());
                }

                @Override
                public void onCancel() {
                    System.out.println("User cancelled adding new check.");
                }
            });
        } else {
            System.out.println("Skip open dialog cause checks list is not initialized...");
        }




    }
}
