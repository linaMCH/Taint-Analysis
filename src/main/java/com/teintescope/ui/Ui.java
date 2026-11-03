package com.teintescope.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.Node;

/** Petits utilitaires de construction d'interface. */
final class Ui {
    private Ui() {}

    static Label label(String text, String... classes) {
        Label l = new Label(text);
        l.getStyleClass().addAll(classes);
        return l;
    }

    static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    static Button button(String text, String icon, String variant, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().addAll("btn", variant);
        if (icon != null) b.setGraphic(Icons.of(icon, 16, variant.equals("btn-primary") ? Icons.WHITE : Icons.TEXT));
        if (action != null) b.setOnAction(e -> action.run());
        return b;
    }

    static Button iconButton(String icon, String variant, Runnable action) {
        Button b = new Button();
        b.getStyleClass().addAll("btn", "btn-icon", variant);
        b.setGraphic(Icons.of(icon, 18, Icons.TEXT));
        if (action != null) b.setOnAction(e -> action.run());
        return b;
    }

    static ScrollPane scroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }
}