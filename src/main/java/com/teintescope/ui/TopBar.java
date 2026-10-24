package com.teintescope.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TopBar extends HBox {

    public final Button openButton = new Button("Ouvrir un projet");
    public final Button runButton = new Button("Lancer l'analyse");
    public final Button settingsButton = new Button();
    public final Button themeToggleButton = new Button(); // <-- Nouveau bouton Dark/Light

    public TopBar() {
        getStyleClass().add("top-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);

        openButton.getStyleClass().addAll("btn", "btn-secondary");
        openButton.setGraphic(Icons.of(Icons.FOLDER, 18, Color.web("#CD5C5C")));

        runButton.getStyleClass().addAll("btn", "btn-primary");
        runButton.setGraphic(Icons.of(Icons.PLAY, 16, Color.WHITE));

        settingsButton.getStyleClass().addAll("btn", "btn-secondary", "btn-icon");
        settingsButton.setGraphic(Icons.of(Icons.SETTINGS, 20, Color.web("#CD5C5C")));

        // Configuration du bouton Dark/Light mode
        themeToggleButton.getStyleClass().addAll("btn", "btn-secondary", "btn-icon");
        themeToggleButton.setGraphic(Icons.of(Icons.CHECK, 16, Color.web("#CD5C5C"))); // Remplace par une icône adaptée si besoin

        Label language = new Label("PHP");
        language.getStyleClass().add("lang-pill");

        // Ajout du bouton dans la barre
        getChildren().addAll(buildBrand(), Ui.spacer(), openButton, language, runButton, themeToggleButton, settingsButton);
    }

    private HBox buildBrand() {
        StackPane logo = new StackPane(Icons.of(Icons.DROP, 22, Color.WHITE));
        logo.getStyleClass().add("logo-mark");

        Circle dot = new Circle(5, Color.web("#CD5C5C")); // Ajusté en rose
        dot.setStroke(Color.WHITE);
        dot.setStrokeWidth(2);
        StackPane.setAlignment(dot, Pos.TOP_RIGHT);
        StackPane.setMargin(dot, new Insets(-4, -4, 0, 0));
        logo.getChildren().add(dot);

        Label teinte = new Label("Teinte");
        teinte.getStyleClass().add("brand-title");
        Label scope = new Label("Scope");
        scope.getStyleClass().addAll("brand-title", "brand-accent");

        Label subtitle = new Label("ANALYSE STATIQUE DE SÉCURITÉ");
        subtitle.getStyleClass().add("brand-sub");

        VBox texts = new VBox(new HBox(teinte, scope), subtitle);
        texts.setAlignment(Pos.CENTER_LEFT);
        HBox brand = new HBox(12, logo, texts);
        brand.setAlignment(Pos.CENTER_LEFT);
        return brand;
    }
}