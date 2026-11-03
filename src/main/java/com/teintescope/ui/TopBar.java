package com.teintescope.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;

public class TopBar extends HBox {

    public TopBar(AppState s, Runnable open, Runnable launch, Runnable config) {
        getStyleClass().add("top-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);

        HBox brand = new HBox(0, Ui.label("Teinte", "brand-title"), Ui.label("Scope", "brand-title", "brand-accent"));
        VBox titles = new VBox(0, brand, Ui.label("ANALYSE STATIQUE DE SÉCURITÉ", "brand-sub"));
        HBox logoBox = new HBox(12, buildLogo(), titles);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Button openBtn = Ui.button("Ouvrir un projet", Icons.FOLDER, "btn-secondary", open);
        Label php = Ui.label("PHP", "lang-pill");
        Button run = Ui.button("Lancer l'analyse", Icons.PLAY, "btn-primary", launch);
        run.disableProperty().bind(s.projectOpen.not());
        Button cfg = Ui.iconButton(Icons.SLIDERS, "btn-secondary", config);

        getChildren().addAll(logoBox, Ui.spacer(), openBtn, php, run, cfg);
    }

    private Node buildLogo() {
        InputStream in = getClass().getResourceAsStream("/images/logo.png");
        if (in != null) {
            ImageView iv = new ImageView(new Image(in, 80, 80, true, true));
            iv.setFitWidth(40);
            iv.setFitHeight(40);
            iv.setPreserveRatio(true);
            return iv;
        }
        StackPane p = new StackPane(Icons.of(Icons.DROPLET, 20, Icons.WHITE));
        p.getStyleClass().add("logo-mark");
        return p;
    }
}