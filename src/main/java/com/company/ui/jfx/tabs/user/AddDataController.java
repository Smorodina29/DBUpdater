package com.company.ui.jfx.tabs.user;

import com.company.*;
import com.company.check.Check;
import com.company.check.CheckException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Александр on 01.04.2017.
 */
public class AddDataController implements TabController {

    public SplitPane root;
    public TitledPane resultsPane;
    public ComboBox<String> tableNamesBox;
    public Button uploadButton;
    public Button exportTemplateButton;
    public TextArea resultsTextArea;
    public Button finishImportButton;
    public Button cancelAdding;
    private String tempTableName;
    private String targetTableName;
    private List<Column> columns;

    public AddDataController() {
    }

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
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("User selected table \'" + newValue + "\' for add.");
                boolean hasSelectedTable = newValue != null && !newValue.isEmpty();
                exportTemplateButton.setDisable(!hasSelectedTable);
                uploadButton.setDisable(!hasSelectedTable);
            }
        });
    }

    public void upload(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл для импорта...");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xls", "*.xls"),
                new FileChooser.ExtensionFilter("xlsx", "*.xlsx")
        );
        File file = chooser.showOpenDialog(root.getScene().getWindow());

        boolean hasSelectedFile = file != null;
        if (hasSelectedFile) {
            System.out.println("Import \'" + file + "\'");

            String path = file.getAbsolutePath();
            clearImportDataAndUI();
            targetTableName = tableNamesBox.getValue();
            resultsPane.setExpanded(true);
            try {
//                int affected = UpdateService.importAndAdd(path, targetTableName);
                resultsTextArea.setText("Начинаем добавление в таблицу \'" + targetTableName + "\'");
                columns = UpdateService.filterForAdd(UpdateService.getTableStructure(targetTableName));
                List<Map<Column, KeyValue>> data = FileService.readForAdd(path, targetTableName, columns);
                resultsTextArea.appendText("\nНайдено записей в файле: " + data.size() + ".");
                tempTableName = UpdateService.createTempTable(targetTableName, columns);
                resultsTextArea.appendText("\nСоздана временная таблица " + tempTableName + ".");
                UpdateService.fillTable(tempTableName, data);
                resultsTextArea.appendText("\nНаполнили временную таблицу данными из файла.");

                List<Check> checks = ChecksService.getChecksForAdd(targetTableName);
                if (checks == null || checks.isEmpty()) {
                    throw new RuntimeException("Проверки не найдены для таблицы `" + targetTableName + "\'");
                }

                resultsTextArea.appendText("\nНайдено проверок: " + checks.size());

                for (Check check : checks) {
                    boolean passed = UpdateService.checkForUpdate(targetTableName, null, tempTableName, check,data.size());
                    if (!passed) {
                        switch (check.getType()) {
                            case ERROR:
                                String query = check.getSqlQuery(tempTableName, targetTableName, null);
                                throw new CheckException(check.getName() + "(validation=" + check.getValidationMethod() + ")" + "\n(" + query + ")");
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
                resultsTextArea.appendText(msgPrefix + "\n" + stackTraceWriter.toString());
                finishImportButton.setDisable(true);
            } catch (CheckException e1) {
                System.out.println("Error: " + e1.getMessage());
                resultsTextArea.appendText("\nНе удалось добавить записи в таблице. Не прошла проверка: " + e1.getMessage());
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

    @Override
    public void load() {
        Set<String> tableNamesForUpdate = new HashSet<>();
        try {
            tableNamesForUpdate = UpdateService.getTableNamesForAdd();
            System.out.println("Loaded tables for add:" + tableNamesForUpdate);
        } catch (Throwable e) {
            System.out.println("Failed to load table names for add:" + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Нe удалось получить список доступных на добавление таблиц. Ошибка: " + e.getMessage(), ButtonType.OK).show();
        }
        tableNamesBox.getItems().clear();
        tableNamesBox.getItems().addAll(tableNamesForUpdate);

        if (!tableNamesForUpdate.isEmpty()) {
            tableNamesBox.setValue(tableNamesBox.getItems().get(0));
        } else{
            new Alert(Alert.AlertType.INFORMATION, "Список доступных для добавления таблиц пуст.", ButtonType.OK).show();
        }
    }

    public void exportTemplate(ActionEvent actionEvent) {
        if (Utils.isNotEmpty(tableNamesBox.getValue())){
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
                UpdateService.exportTableToFile(tableNamesBox.getValue(), file.getAbsolutePath(), false);
            } else {
                System.out.println("User cancelled selecting template save path.");
            }
        } else {
            System.out.println("No one table is selected. Cancel export.");
            new Alert(Alert.AlertType.INFORMATION, "Не выбрана таблица для добавления.", ButtonType.OK).show();
        }
    }

    public void onCancel(ActionEvent event) {
        try {
            UpdateService.deleteTable(tempTableName);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to remove temp table \'" + tempTableName + "\'.");
            new Alert(Alert.AlertType.WARNING, "Не удалось удалить временную таблицу.", ButtonType.OK).show();
        }
        clearImportDataAndUI();
    }

    private void clearImportDataAndUI() {
        finishImportButton.setDisable(true);
        resultsTextArea.clear();
        targetTableName = null;
        tempTableName = null;
        columns = null;
    }

    public void onFinish(ActionEvent event) {
        finishImportButton.setDisable(true);
        try {
            int affected = UpdateService.addDataFromTempTable(targetTableName, tempTableName, columns);
            resultsTextArea.appendText("\nУспешно добавлено " + affected + " записей в таблицу \'" + targetTableName + "\'");
        } catch (Throwable e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Произошла ошибка при переносе данных. Ошибка:" + e.getMessage(), ButtonType.OK).show();
        }
        try {
            UpdateService.deleteTable(tempTableName);
        } catch (SQLException e) {
            System.out.println("Failed to remove temp table \'" + tempTableName + "\'.");
            e.printStackTrace();
            new Alert(Alert.AlertType.WARNING, "Не удалось удалить временную таблицу.", ButtonType.OK).show();
        }
    }
}
