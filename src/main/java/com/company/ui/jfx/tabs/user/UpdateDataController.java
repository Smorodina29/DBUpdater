package com.company.ui.jfx.tabs.user;

import com.company.ChecksService;
import com.company.Column;
import com.company.FileService;
import com.company.UpdateService;
import com.company.check.Check;
import com.company.check.CheckException;
import com.company.data.FloatKeyValue;
import com.company.data.KeyValue;
import com.company.ui.jfx.tabs.TabController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.*;

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
    public TextArea resultsTextArea;
    public Button finishImportButton;
    public Button cancelAdding;
    private String targetTableName;
    private String targetColumnName;
    private String tempTableName;


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
                    List<String> columnNames = UpdateService.getUpdatableTableColumns(selectedTable);
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
        Set<String> tableNamesForUpdate = new HashSet<>();
        try {
            tableNamesForUpdate = UpdateService.getTableNamesForUpdate();
            System.out.println("Loaded tables for update:" + tableNamesForUpdate);
        } catch (Throwable e) {
            System.out.println("Failed to load table names for update:" + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Нe удалось получить список доступных на обновление таблиц. Ошибка: " + e.getMessage(), ButtonType.OK).show();
        }
        tableNamesBox.getItems().clear();
        tableNamesBox.getItems().addAll(tableNamesForUpdate);
        columnNamesBox.getItems().clear();

        if (!tableNamesForUpdate.isEmpty()) {
            tableNamesBox.setValue(tableNamesBox.getItems().get(0));
        } else{
            new Alert(Alert.AlertType.INFORMATION, "Список доступных для обновления таблиц пуст.", ButtonType.OK).show();
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
            targetTableName = tableNamesBox.getValue();
            targetColumnName = columnNamesBox.getValue();

            resultsPane.setExpanded(true);
            try {
                resultsTextArea.setText("Начинаем обновление колонки \'" + targetColumnName + "\' в \'" + targetTableName + "\'.");
                List<Column> columns = UpdateService.filterForUpdate(UpdateService.getTableStructure(targetTableName), targetColumnName);

                Column targetColumn = UpdateService.findColumn(targetColumnName, columns);
                ArrayList<KeyValue> keyValues = FileService.readForUpdate(path, targetTableName, targetColumn);


                ArrayList<Map<Column, KeyValue>> data = new ArrayList<>();

                Column idColumn = UpdateService.findColumn("id", columns);
                for (KeyValue keyValue : keyValues) {
                    HashMap<Column, KeyValue> map = new HashMap<>();
                    map.put(idColumn, new FloatKeyValue((float) keyValue.key));
                    map.put(targetColumn, keyValue);
                    data.add(map);
                }

                resultsTextArea.appendText("\nНайдено записей в файле: " + keyValues.size() + ".");
                tempTableName = UpdateService.createTempTable(targetTableName, columns);
                resultsTextArea.appendText("\nСоздали временную таблица " + tempTableName + ".");
                UpdateService.fillTable(tempTableName, data);
                resultsTextArea.appendText("\nНаполнили временную таблицу данными из файла.");

                System.out.println("Start checking tables!");

                List<Check> checks = ChecksService.getChecksForUpdate(targetTableName, targetColumnName);

                if (checks == null || checks.isEmpty()) {
                    throw new RuntimeException("Проверки не найдены для таблицы `" + targetTableName + "\'");
                }
                System.out.println("Found checks:" + checks);
                resultsTextArea.appendText("\nНайдено проверок: " + checks.size());
                int updateRowsCount = keyValues.size();
                for (Check check : checks) {
                    boolean passed = UpdateService.checkForUpdate(targetTableName, targetColumnName, tempTableName, check, updateRowsCount);
                    if (!passed) {
                        switch (check.getType()) {
                            case ERROR:
                                throw new CheckException(check.getName());
                            case WARNING:
                                String wrn = "Предупреждение: проверка не пройдена: " + check.getName();
                                resultsTextArea.appendText("\n" + wrn);
//                                new Alert(Alert.AlertType.WARNING, wrn, ButtonType.OK).show();
                                break;
                            default:
                                throw new RuntimeException("Unknown check type:" + check.getType());
                        }
                    } else {
                        resultsTextArea.appendText("\nПройдена: " + check.getName());
                    }
                }

                resultsTextArea.appendText("\nВсе проверки пройдены.\n\nНажмите \'Завершить\' для переноса данных из временной таблицы в целевую");
                finishImportButton.setDisable(false);
            } catch (SQLException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
                String msgPrefix = "\nПроизошла ошибка во время выполнения запроса: " + ex.getMessage();
                StringWriter stackTraceWriter = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTraceWriter));
                resultsTextArea.setText(msgPrefix + "\n" + stackTraceWriter.toString());
                finishImportButton.setDisable(true);
            } catch (CheckException e1) {
                System.out.println("Error: " + e1.getMessage());
                resultsTextArea.appendText("\nНе удалось обновить записи в таблице. Не прошла проверка: " + e1.getMessage());
                e1.printStackTrace();
                finishImportButton.setDisable(true);
            } catch (Throwable ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
                String msgPrefix = "\nПроизошла ошибка во время импорта данных: " + ex.getMessage();
                StringWriter stackTraceWriter = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTraceWriter));
                resultsTextArea.appendText(msgPrefix + "\n" + stackTraceWriter.toString());
                finishImportButton.setDisable(true);
            }

            //scroll bottom
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    resultsTextArea.setScrollTop(Double.MAX_VALUE);
                }
            });
        } else {
            System.out.println("User cancelled selecting file to upload.");
        }
    }

    private void clearImportDataAndUI() {
        finishImportButton.setDisable(true);
        resultsTextArea.clear();
        targetTableName = null;
        targetColumnName = null;
        tempTableName = null;
    }

    public void onCancel(ActionEvent event) {
        clearImportDataAndUI();
        try {
            UpdateService.deleteTable(tempTableName);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to remove temp table \'" + tempTableName + "\'.");
            new Alert(Alert.AlertType.WARNING, "Не удалось удалить временную таблицу.", ButtonType.OK).show();
        }
    }

    public void onFinish(ActionEvent event) {
        finishImportButton.setDisable(true);
        int updateRowsCount = UpdateService.updateDataFromTempToTarget(targetTableName, targetColumnName, tempTableName);
        String message;
        if (updateRowsCount > 0) {
            message = "\nОбновлено " + updateRowsCount + " записей в таблице " + targetTableName + ".";
        } else {
            message = "\nНи одна запись не была обновлена в таблице " + targetTableName + ".";
        }
        resultsTextArea.appendText(message);

        try {
            UpdateService.deleteTable(tempTableName);
        } catch (SQLException e) {
            System.out.println("Failed to remove temp table \'" + tempTableName + "\'.");
            e.printStackTrace();
            new Alert(Alert.AlertType.WARNING, "Не удалось удалить временную таблицу.", ButtonType.OK).show();
        }
    }
}
