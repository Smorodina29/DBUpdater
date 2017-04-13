package com.company.ui.jfx.tabs.admin;

import com.company.ChecksService;
import com.company.check.Check;
import com.company.check.CheckType;
import com.company.check.ValidationMethod;
import com.company.ui.jfx.editors.DataPatch;
import com.company.ui.jfx.editors.EditCallback;
import com.company.ui.jfx.editors.EditCheckDialog;
import com.company.ui.jfx.editors.adding.CreateCheckDialog;
import com.company.ui.jfx.tabs.TabController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Александр on 01.04.2017.
 */
public class ChecksController implements TabController {
    public TableView<Check> tableView;
    public TableColumn<Check, String> nameColumn;
    public TableColumn<Check, CheckType> typeColumn;
    public TableColumn<Check, ValidationMethod> validationTypeColumn;
    public TableColumn<Check, String> queryTextColumn;
    public TableColumn<Check, String> messageTextColumn;
    public TableColumn<Check, Check> editColumn;
    public TableColumn<Check, Check> deleteColumn;

    public Button refreshButton;
    public BorderPane root;
    public Button saveButton;
    public Button addButton;

    private DataPatch dataPatch = new DataPatch();

    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {

        // Инициализация таблицы проверок с двумя столбцами.
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

        editColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, Check>, ObservableValue<Check>>() {
            @Override
            public ObservableValue<Check> call(TableColumn.CellDataFeatures<Check, Check> param) {
                return new SimpleObjectProperty<Check>(param.getValue());
            }
        });


        editColumn.setCellFactory(new Callback<TableColumn<Check, Check>, TableCell<Check, Check>>() {
            @Override
            public TableCell<Check, Check> call(TableColumn<Check, Check> param) {
                TableCell<Check, Check> cell = new TableCell<Check, Check>() {

                    final Button btn = new Button("", new ImageView(new Image("icons/edit.png")));

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
                                    System.out.println("Clicked edit on " + item);
                                    onEditCheckClick(item);
                                }
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        });

        deleteColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Check, Check>, ObservableValue<Check>>() {
            @Override
            public ObservableValue<Check> call(TableColumn.CellDataFeatures<Check, Check> param) {
                return new SimpleObjectProperty<Check>(param.getValue());
            }
        });
        deleteColumn.setCellFactory(new Callback<TableColumn<Check, Check>, TableCell<Check, Check>>() {
            @Override
            public TableCell<Check, Check> call(TableColumn<Check, Check> param) {
                TableCell<Check, Check> cell = new TableCell<Check, Check>() {
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
                                    System.out.println("Clicked edit on " + item);
                                    onDeleteClick(item);
                                }
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        });
    }

    private void onEditCheckClick(Check item) {
        EditCheckDialog dialog = new EditCheckDialog();
        dialog.open(item, root.getScene().getWindow(), new EditCallback<Check>() {
            @Override
            public void onFinish(Check edited) {
                if (edited.isNew()) {
                    dataPatch.updateCreated(edited, item);
                } else {
                    dataPatch.addUpdated(edited);
                }
                ObservableList<Check> list = tableView.itemsProperty().getValue();
                list.add(list.indexOf(item), edited);
                list.remove(item);
                System.out.println("Finished editing: " + edited);
                saveButton.setDisable(dataPatch.isEmpty());
                tableView.refresh();
            }

            @Override
            public void onCancel() {
                System.out.println("User cancelled editing.");
            }
        });
    }

    private void onDeleteClick(Check item) {
        if (item.isNew()) {
            dataPatch.getCreated().remove(item);
        } else {
            dataPatch.addDeleted(item);
        }
        tableView.itemsProperty().getValue().remove(item);
        System.out.println("Deleted: " + item);
        saveButton.setDisable(dataPatch.isEmpty());
        tableView.refresh();
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
        tableView.getItems().clear();
        tableView.getItems().addAll(checks);
        dataPatch.clear();
        System.out.println("Loaded " + checks.size() + " checks. Dropped data patch.");
        saveButton.setDisable(dataPatch.isEmpty());
    }

    public void refresh(ActionEvent actionEvent) {
        load();
        System.out.println("Refreshed checks table.");
    }

    public void saveChanges(ActionEvent event) {
        if (dataPatch.isNotEmpty()) {
            System.out.println("Start applying patch: " + dataPatch);
            try {
                ChecksService.update(dataPatch.getUpdated());
                dataPatch.getUpdated().clear();
                ChecksService.delete(dataPatch.getDeleted());
                dataPatch.getDeleted().clear();
                ChecksService.create(dataPatch.getCreated());
                System.out.println("Applied patch: " + dataPatch);
                /*
                * перезагрузка нужна для того, чтобы вновь добавленные проверки в дальнейшем при редактировании
                * помечались как отредактированые, а не добавленные (не dataPatch.created, dataPatch.updated)
                * */
                load();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Произошла ошибка во время сохранения:" + e.getMessage(), ButtonType.OK).show();
                System.out.println("Failed to save patch: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Clicked save, although data patch is empty. ");
            saveButton.setDisable(true);
        }
    }

    public void add(ActionEvent actionEvent) {
        CreateCheckDialog dialog = new CreateCheckDialog();
        dialog.open(root.getScene().getWindow(), new EditCallback<Check>() {
            @Override
            public void onFinish(Check create) {
                dataPatch.getCreated().add(create);
                ObservableList<Check> list = tableView.itemsProperty().getValue();
                list.add(create);
                System.out.println("Finished creating new: " + create);
                saveButton.setDisable(dataPatch.isEmpty());
                tableView.refresh();
            }

            @Override
            public void onCancel() {
                System.out.println("User cancelled creating.");
            }
        });
    }
}
