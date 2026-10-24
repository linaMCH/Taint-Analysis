package com.teintescope.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/** Barre d'état en bas de la fenêtre. */
public class StatusBar extends HBox {

    public StatusBar() {
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);

        HBox active = new HBox(6, new Circle(3, Color.web("#F08080")), new Label("Analyse active"));
        active.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(
                active,
                new Label("Branche : principale"),
                Ui.spacer(),
                new Label("PHP 8.2"),
                new Label("Ligne 18, colonne 21"));
    }
}
