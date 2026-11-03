package com.teintescope.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/** Fenêtre modale moderne (coins arrondis + ombre) partagée par le rapport et la configuration. */
final class DialogShell {
    private DialogShell() {}

    static Stage show(Window owner, Region content, double w, double h) {
        double width = Math.min(w, owner.getWidth() - 80);
        double height = Math.min(h, owner.getHeight() - 80);

        Stage st = new Stage();
        st.initOwner(owner);
        st.initModality(Modality.WINDOW_MODAL);
        st.initStyle(StageStyle.TRANSPARENT);

        content.getStyleClass().add("dialog");
        content.setPrefSize(width, height);
        content.setMaxSize(width, height);

        StackPane root = new StackPane(content);
        root.getStyleClass().add("dialog-root");
        root.setPadding(new Insets(24));

        Scene sc = new Scene(root, width + 48, height + 48);
        sc.setFill(Color.TRANSPARENT);
        sc.getStylesheets().addAll(owner.getScene().getStylesheets());
        sc.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) st.close(); });

        st.setScene(sc);
        st.setX(owner.getX() + (owner.getWidth() - (width + 48)) / 2);
        st.setY(owner.getY() + (owner.getHeight() - (height + 48)) / 2);
        st.show();
        return st;
    }

    static HBox header(String title, String subtitle, Runnable onClose) {
        VBox t = new VBox(4, Ui.label(title, "dialog-title"), Ui.label(subtitle, "dialog-sub"));
        Button x = Ui.iconButton(Icons.X, "btn-secondary", onClose);
        HBox h = new HBox(t, Ui.spacer(), x);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add("dialog-header");
        return h;
    }
}