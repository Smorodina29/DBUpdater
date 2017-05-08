package com.company.ui.jfx;

import com.company.ui.jfx.login.Role;
import com.company.ui.jfx.login.User;
import com.company.ui.jfx.tabs.*;
import com.company.ui.jfx.tabs.admin.ChecksController;
import com.company.ui.jfx.tabs.admin.SettingsController;
import com.company.ui.jfx.tabs.admin.TableSettingsController;
import com.company.ui.jfx.tabs.admin.TablesController;
import com.company.ui.jfx.tabs.user.AddDataController;
import com.company.ui.jfx.tabs.user.UpdateDataController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.HashMap;

public class AppController {

    @FXML public Tab addTab;
    @FXML public Tab updateTab;
    @FXML public Tab checksTab;
    @FXML public Tab tablesTab;
    @FXML public Tab tableSettingsTab;
    @FXML public Tab settingsTab;
    @FXML public BorderPane root;
    @FXML public TabPane tabPane;
    @FXML public AddDataController addDataController;
    @FXML public UpdateDataController updateDataController;
    @FXML public ChecksController checksController;
    @FXML public TablesController tablesController;
    @FXML public TableSettingsController tableSettingsController;
    @FXML public SettingsController settingsController;

    private ArrayList<Tab> adminTabs;
    private ArrayList<Tab> userTabs;
    private HashMap<Tab, TabController> tabToController = new HashMap<>();

    public AppController() {
        System.out.println("Constructor called.");
    }

    /*
    *  Специальный метод с имененем FXMLLoader.INITIALIZE_METHOD_NAME
    *  Вызывается после инициализации экземпляра контроллера (см. javafx.fxml.FXMLLoader.loadImpl(java.io.InputStream, java.lang.Class<?>))
    * */
    public void initialize() {
        System.out.println("Initializing AppController.");
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                System.out.println("Clicked on tab: \'" + safeName(newValue) + "\' (was \'" + safeName(oldValue) + "\')");
                if (newValue != null) {
                    TabController controller = tabToController.get(newValue);
                    if (controller != null) {
                        controller.load();
                    }
                }
            }
        });
        userTabs = new ArrayList<>();
        userTabs.add(addTab);
        userTabs.add(updateTab);

        hide(userTabs);

        adminTabs = new ArrayList<>();
        adminTabs.add(checksTab);
        adminTabs.add(tablesTab);
        adminTabs.add(tableSettingsTab);
        adminTabs.add(settingsTab);
        hide(adminTabs);

        tabToController.put(checksTab, checksController);
        tabToController.put(tablesTab, tablesController);
        tabToController.put(tableSettingsTab, tableSettingsController);
        tabToController.put(settingsTab, settingsController);

        tabToController.put(addTab, addDataController);
        tabToController.put(updateTab, updateDataController);
    }

    private String safeName(Tab tab) {
        if (tab == null) {
            return "NULL";
        } else {
            return tab.getText();
        }
    }

    void onUserLogin(User user) {
        if (Role.USER.equals(user.getRole())) {
            hide(adminTabs);
            show(userTabs);
            /*for (TabController controller : userControllers) {
                controller.load();
            }*/
        } else {
            hide(userTabs);
            show(adminTabs);
            System.out.println("adminControllers");
            /*for (TabController controller : adminControllers) {
                controller.load();
            }*/
        }
    }

    private void show(ArrayList<Tab> userTabs) {
        tabPane.getTabs().addAll(userTabs);
    }

    private void hide(ArrayList<Tab> tabs) {
        tabPane.getTabs().removeAll(tabs);
    }
}
