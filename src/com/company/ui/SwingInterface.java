package com.company.ui;

import com.company.Column;
import com.company.UpdateService;
import com.company.data.KeyValue;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.*;
import java.util.List;


/**
 * Created by Александр on 26.02.2017.
 */
public class SwingInterface extends JFrame {
    public  SwingInterface() {
        setTitle("Update data");

        JTabbedPane tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);
        setBounds(600, 250, 600, 600);
        tabbedPane.addTab("Добавление", createAddPane());
        tabbedPane.addTab("Обновление", createUpdatePane());
        tabbedPane.addTab("Настройки", createSettingsPane());
    }

    private Component createSettingsPane() {
        JPanel settingsPane = new JPanel();
        settingsPane.add(new JLabel("Settings"));

        return settingsPane;
    }

    private JPanel createUpdatePane() {
        JPanel update = new JPanel();
        JComboBox<String> tablesForUpdate = new JComboBox<>();



        JComboBox<String> columnsCombobox = new JComboBox<>();

        tablesForUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String targetTable = tablesForUpdate.getItemAt(tablesForUpdate.getSelectedIndex());
                System.out.println("User selected table `" + targetTable + "'");

                List<String> columnNames = UpdateService.getTableColumns(targetTable, true);

                for (int i = 0; i < columnNames.size(); i++) {
                    String name = columnNames.get(i);
                    if (name.equalsIgnoreCase("id")) {
                        columnNames.remove(i);
                        break;
                    }
                }

                System.out.println("Table " + targetTable + " has columns for update: " + columnNames);
                columnsCombobox.removeAllItems();
                for (String columnName : columnNames) {
                    columnsCombobox.addItem(columnName);
                }
            }
        });

        java.util.List<String> tablesName = UpdateService.getTableNamesForUpdate();//todo handle server not available exception
        for (String name : tablesName) {
            tablesForUpdate.addItem(name);
        }

        update.add(tablesForUpdate);
        update.add(columnsCombobox);

        JButton importTableU = new JButton("Загрузить файл");
        JButton exportTemplateU = new JButton("Выгрузить шаблон");
        exportTemplateU.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("xls", "xls"));
                if (chooser.showSaveDialog(SwingInterface.this) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    if (!path.endsWith(".xls")){
                        path += ".xls";
                    }
                    System.out.println("Path=" + path);
                    UpdateService.exportTableToFile(tablesForUpdate.getItemAt(tablesForUpdate.getSelectedIndex()), path, true);
                }
            }
        });
        JButton updateDataU = new JButton("Добавить");
        JButton deleteU = new JButton("Удалить временную таблицу");

        update.add(importTableU);
        update.add(exportTemplateU);
        update.add(updateDataU);
        update.add(deleteU);
        return update;
    }

    private JPanel createAddPane() {
        JPanel add = new JPanel();
        JComboBox<String> tablesCombobox = new JComboBox<>();
//        tablesCombobox.setPreferredSize(new Dimension(200, 25));

        java.util.List<String> tablesName = UpdateService.getTableNamesForUpdate();//todo handle server not available exception

        for (String name : tablesName) {
            tablesCombobox.addItem(name);
        }

        add.add(tablesCombobox);
        JButton importTableAdd = new JButton("Загрузить файл");
        importTableAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("xls", "xls"));
                if (chooser.showOpenDialog(SwingInterface.this) == JFileChooser.APPROVE_OPTION){
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    String targetTableName = tablesCombobox.getItemAt(tablesCombobox.getSelectedIndex());
                    List<Column> columns = UpdateService.filterForAdd(UpdateService.getTableStructure(targetTableName));
                    List<Map<Column, KeyValue>> data = UpdateService.readForAdd(path, targetTableName, columns);

                    String tempTableName = UpdateService.createTempTable(targetTableName, columns);
                    try {
                        UpdateService.fillTable(tempTableName, data);

                        int affected = UpdateService.addDataFromTempTable(targetTableName, tempTableName, columns);

                        JOptionPane.showMessageDialog(null, "Добавлено " + affected + " записей в таблицу " + targetTableName, "InfoBox: Добавление", JOptionPane.INFORMATION_MESSAGE);

                    } catch (SQLException e1) {
                        System.out.println("Error: " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        JButton exportTemplateAdd = new JButton("Выгрузить шаблон");
        exportTemplateAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("xls", "xls"));
                if (chooser.showSaveDialog(SwingInterface.this) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    if (!path.endsWith(".xls")){
                        path += ".xls";
                    }
                    String targetTableName = tablesCombobox.getItemAt(tablesCombobox.getSelectedIndex());
                    UpdateService.exportTableToFile(targetTableName, path, false);
                    System.out.println("Exported template for `" + targetTableName + "' to `" + path + "'");
                }
            }
        });
        JButton addDataAdd = new JButton("Добавить");
        JButton deleteAdd = new JButton("Удалить временную таблицу");

        add.add(importTableAdd);
        add.add(exportTemplateAdd);
        add.add(addDataAdd);
        add.add(deleteAdd);
        return add;
    }

    public static void main(String[] args) {
        SwingInterface tp = new SwingInterface();
        tp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tp.setVisible(true);
    }
}
