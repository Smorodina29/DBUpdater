package com.company.ui.jfx;

import com.company.ui.jfx.login.AuthenticationCallback;
import com.company.ui.jfx.login.LoginDialog;
import com.company.ui.jfx.login.Role;
import com.company.ui.jfx.login.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import java.util.ArrayList;

public class AppController {

    public Tab addTab;
    public Tab updateTab;
    public Tab adminTab;
    public Tab settingsTab;
    public BorderPane root;
    public TabPane tabPane;
    private ArrayList<Tab> adminTabs;
    private ArrayList<Tab> userTabs;

    public AppController() {
        System.out.println("Constructor called.");
    }

    @FXML protected void handleSubmitButtonAction(ActionEvent event) {
//        actiontarget.setText("Sign in button pressed");
        System.out.println("handleSubmitButtonAction - " + event);
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
