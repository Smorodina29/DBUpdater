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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    public TableColumn<Check, Check> editColumn;
    public TableColumn<Check, Check> deleteColumn;

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


        // --BUTTONS--
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
