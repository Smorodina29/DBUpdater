package com.company.ui.jfx.tabs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;

/**
 * Created by Александр on 01.04.2017.
 */
public class UpdateDataController {

    public SplitPane root;
    public TitledPane resultsPane;

    public void upload(ActionEvent actionEvent) {

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
    }
}
