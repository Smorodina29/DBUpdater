package com.company.ui.jfx;

import com.company.ui.jfx.login.Role;
import com.company.ui.jfx.login.User;
import com.company.ui.jfx.tabs.AddDataController;
import com.company.ui.jfx.tabs.AdministratorController;
import com.company.ui.jfx.tabs.SettingsController;
import com.company.ui.jfx.tabs.UpdateDataController;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;

public class AppController {

    @FXML public Tab addTab;
    @FXML public Tab updateTab;
    @FXML public Tab adminTab;
    @FXML public Tab settingsTab;
    @FXML public BorderPane root;
    @FXML public TabPane tabPane;
    @FXML public AddDataController addDataController;
    @FXML public UpdateDataController updateDataController;
    @FXML public AdministratorController administratorController;
    @FXML public SettingsController settingsController;
    private ArrayList<Tab> adminTabs;
    private ArrayList<Tab> userTabs;

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
        adminTabs.add(adminTab);
        adminTabs.add(settingsTab);
        hide(adminTabs);
    }

    private void hide(ArrayList<Tab> tabs) {
        tabPane.getTabs().removeAll(tabs);
    }

    void onUserLogin(User user) {
        if (Role.USER.equals(user.getRole())) {
            hide(adminTabs);
            show(userTabs);
        } else {
            hide(userTabs);
            show(adminTabs);
        }
    }

    private void show(ArrayList<Tab> userTabs) {
        tabPane.getTabs().addAll(userTabs);
    }
}
