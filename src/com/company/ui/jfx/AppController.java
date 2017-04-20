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
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;

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
    private ArrayList<TabController> adminControllers;
    private ArrayList<TabController> userControllers;

    public AppController() {
        System.out.println("Constructor called.");
    }

    /*
    *  Специальный метод с имененем FXMLLoader.INITIALIZE_METHOD_NAME
    *  Вызывается после инициализации экземпляра контроллера (см. javafx.fxml.FXMLLoader.loadImpl(java.io.InputStream, java.lang.Class<?>))
    * */
    public void initialize() {
        System.out.println("Initializing AppController.");
        userTabs = new ArrayList<>();
        userTabs.add(addTab);
        userTabs.add(updateTab);

        adminTabs = new ArrayList<>();
        adminTabs.add(checksTab);
        adminTabs.add(tablesTab);
        adminTabs.add(settingsTab);
        hide(adminTabs);


        adminControllers = new ArrayList<>();
        adminControllers.add(checksController);
        adminControllers.add(tablesController);
        adminControllers.add(tableSettingsController);
        adminControllers.add(settingsController);

        userControllers = new ArrayList<>();
        userControllers.add(addDataController);
        userControllers.add(updateDataController);
    }

    void onUserLogin(User user) {
        if (Role.USER.equals(user.getRole())) {
            hide(adminTabs);
            show(userTabs);
            for (TabController controller : userControllers) {
                controller.load();
            }
        } else {
            hide(userTabs);
            show(adminTabs);
            System.out.println("adminControllers");
            for (TabController controller : adminControllers) {
                controller.load();
            }
        }
    }

    private void show(ArrayList<Tab> userTabs) {
        tabPane.getTabs().addAll(userTabs);
    }

    private void hide(ArrayList<Tab> tabs) {
        tabPane.getTabs().removeAll(tabs);
    }
}
