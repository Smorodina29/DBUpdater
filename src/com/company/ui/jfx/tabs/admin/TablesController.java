package com.company.ui.jfx.tabs.admin;

import com.company.UpdateService;
import com.company.ui.jfx.tabs.TabController;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Александр on 01.04.2017.
 */
public class TablesController implements TabController, Initializable {


    public Button moveRight;
    public Button moveLeft;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        String buttonStyle = "-fx-font: 22 arial; -fx-base: #9effa6;";
        String baseStyle = "-fx-font: 22 arial; -fx-font-weight: bold;";
        moveRight.setStyle(baseStyle + " -fx-base: #c7ffb7;");
        moveLeft.setStyle(baseStyle + " -fx-base: #ffb7b7");
    }

    @Override
    public void load() {
        UpdateService.getTableNamesForUpdate();
    }

    public void saveChanges(ActionEvent event) {

    }

    public void refresh(ActionEvent event) {

    }

    public void moveRight(ActionEvent actionEvent) {

    }

    public void moveLeft(ActionEvent actionEvent) {

    }


}
