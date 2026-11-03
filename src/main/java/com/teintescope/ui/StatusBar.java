package com.teintescope.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class StatusBar extends HBox {

    public StatusBar(AppState s) {
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.getStyleClass().addAll("state-dot", "state-clean");

        Label msg = new Label();
        msg.textProperty().bind(s.status);

        Label pos = new Label();
        pos.textProperty().bind(s.cursorLine.asString("Ligne %d"));
        pos.visibleProperty().bind(s.projectOpen);

        getChildren().addAll(dot, msg, Ui.spacer(), new Label("PHP"), pos);
    }
}