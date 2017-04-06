package com.company.ui.jfx.tabs.user;

import com.company.UpdateService;
import com.company.check.CheckException;
import com.company.ui.jfx.tabs.TabController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Александр on 01.04.2017.
 */
public class UpdateDataController implements TabController {

    public SplitPane root;
    public TitledPane resultsPane;
    public ComboBox<String> tableNamesBox;
    public ComboBox<String> columnNamesBox;
    public Button exportTemplateButton;
    public Button uploadButton;
    public Label affectedRowsCountLabel;
    public TextArea resultsTextArea;


    public void initialize() {
        resultsPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean expanded) {
                if (expanded) {
                    root.setDividerPositions(0.5);
                } else {
                    root.setDividerPositions(0.94);
                }
            }
        });
        resultsPane.setExpanded(false);


        tableNamesBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String selectedTable) {
                System.out.println("User selected table \'" + selectedTable + "\' for update.");
                boolean hasSelectedTable = selectedTable != null && !selectedTable.isEmpty();
                exportTemplateButton.setDisable(!hasSelectedTable);
                uploadButton.setDisable(!hasSelectedTable);

                if (hasSelectedTable) {
                    List<String> columnNames = UpdateService.getTableColumns(selectedTable, true);
                    columnNames.remove("id");//
                    columnNames.remove("ID");//
                    System.out.println("Table " + selectedTable + " has columns for update: " + columnNames);
                    columnNamesBox.getItems().clear();

                    columnNamesBox.getItems().addAll(columnNames);

                    if (!columnNames.isEmpty()) {
                        columnNamesBox.setValue(columnNames.get(0));
                    } else {
                        columnNamesBox.setValue(null);
                    }
                }
            }
        });

        columnNamesBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("User selected column \'" + newValue + "\' for update.");
                boolean hasSelectedTable = newValue != null && !newValue.isEmpty();
                exportTemplateButton.setDisable(!hasSelectedTable);
                uploadButton.setDisable(!hasSelectedTable);
            }
        });
    }

    @Override
    public void load() {
        List<String> tableNamesForUpdate = UpdateService.getTableNamesForUpdate();
        System.out.println("TableNamesForAdd:" + tableNamesForUpdate);
        tableNamesBox.getItems().addAll(tableNamesForUpdate);

        if (!tableNamesForUpdate.isEmpty()) {
            tableNamesBox.setValue(tableNamesForUpdate.get(0));
        }
    }

    public void exportTemplate(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить шаблон как...");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xls", "*.xls"),
                new FileChooser.ExtensionFilter("xlsx", "*.xlsx")
        );
        File file = chooser.showSaveDialog(root.getScene().getWindow());

        boolean hasSelectedFile = file != null;
        if (hasSelectedFile) {
            System.out.println("Save template to \'" + file + "\'");
            UpdateService.exportTableToFile(tableNamesBox.getValue(), file.getAbsolutePath(), true);
        } else {
            System.out.println("User cancelled selecting template save path.");
        }

    }

    public void upload(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить шаблон как...");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xls", "*.xls"),
                new FileChooser.ExtensionFilter("xlsx", "*.xlsx")
        );
        File file = chooser.showOpenDialog(root.getScene().getWindow());

        boolean hasSelectedFile = file != null;
        if (hasSelectedFile) {
            System.out.println("Import \'" + file + "\'");

            String path = file.getAbsolutePath();
            String targetTableName = tableNamesBox.getValue();
            String targetColumnName = columnNamesBox.getValue();

            try {
                int affected = UpdateService.importAndUpdate(path, targetTableName, targetColumnName);
                String message;
                if (affected > 0) {
                    message = "Обновлено " + affected + " записей в таблице " + targetTableName + ".";
                } else {
                    message = "Ни одна запись не была обновлена в таблице " + targetTableName + ".";
                }
                affectedRowsCountLabel.setText("" + affected);
                resultsTextArea.setText(message);
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
                String msgPrefix = "Произошла ошибка во время выполнения запроса: " + ex.getMessage();
                affectedRowsCountLabel.setText("0");
                StringWriter stackTraceWriter = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTraceWriter));
                resultsTextArea.setText(msgPrefix + "\n" + stackTraceWriter.toString());
            } catch (CheckException e1) {
                System.out.println("Error: " + e1.getMessage());
                affectedRowsCountLabel.setText("0");
                resultsTextArea.setText("Не удалось обновить записи в таблице. Не прошла проверка: " + e1.getMessage());
                e1.printStackTrace();
            } catch (Throwable ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
                String msgPrefix = "Произошла ошибка во время импорта данных: " + ex.getMessage();
                affectedRowsCountLabel.setText("0");
                StringWriter stackTraceWriter = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTraceWriter));
                resultsTextArea.setText(msgPrefix + "\n" + stackTraceWriter.toString());
            }
            resultsPane.setExpanded(true);

        } else {
            System.out.println("User cancelled selecting file to upload.");
        }
    }
}
