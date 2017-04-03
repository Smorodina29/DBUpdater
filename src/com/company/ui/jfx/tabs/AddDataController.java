package com.company.ui.jfx.tabs;

import com.company.UpdateService;
import com.company.check.CheckException;
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
public class AddDataController implements TabController{

    public SplitPane root;
    public TitledPane resultsPane;
    public ComboBox<String> tableNamesBox;
    public Button uploadButton;
    public Button exportTemplateButton;
    public Label affectedRowsCountLabel;
    public TextArea resultsTextArea;

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

            try {
                int affected = UpdateService.importAndAdd(path, targetTableName);
                affectedRowsCountLabel.setText("" + affected);
                resultsTextArea.setText("Добавлено " + affected + " записей в таблицу " + targetTableName);
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
                resultsTextArea.setText("Не удалось добавить записи в таблице. Не прошла проверка: " + e1.getMessage());
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

    @Override
    public void load() {
        List<String> tableNamesForUpdate = UpdateService.getTableNamesForUpdate();
        System.out.println("tableNamesForUpdate:" + tableNamesForUpdate);
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
            UpdateService.exportTableToFile(tableNamesBox.getValue(), file.getAbsolutePath(), false);
        } else {
            System.out.println("User cancelled selecting template save path.");
        }

    }
}
