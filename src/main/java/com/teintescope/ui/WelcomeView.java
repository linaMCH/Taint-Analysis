package com.teintescope.ui;

import com.teintescope.model.DemoData;
import com.teintescope.model.VulnCategory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;

public class WelcomeView extends ScrollPane {

    public WelcomeView(AppState s, Runnable open) {
        // bandeau des 3 étapes
        HBox steps = new HBox(40, Ui.label("1. Ouvrez un projet", "welcome-step"),
                Ui.label("2. Lancez l'analyse", "welcome-step"),
                Ui.label("3. Consultez les problèmes", "welcome-step"));
        steps.setAlignment(Pos.CENTER);
        steps.getStyleClass().add("steps-banner");

        Label title = Ui.label("Suivez chaque donnée,\njusqu'à sa destination.", "welcome-title");
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        Label sub = Ui.label("Analysez les flux de données et détectez plusieurs familles\nde vulnérabilités depuis une interface unique.", "welcome-sub");
        sub.setAlignment(Pos.CENTER);

        Button openBtn = Ui.button("Ouvrir un projet", Icons.FOLDER, "btn-primary", open);

        // zone de dépôt
        VBox dzTxt = new VBox(2, Ui.label("ou déposez un dossier ici", "dz-title"),
                Ui.label("Projet PHP · dossier ou fichiers .php", "dz-sub"));
        HBox drop = new HBox(12, Icons.of(Icons.UPLOAD, 18, Icons.MUTED), dzTxt);
        drop.setAlignment(Pos.CENTER_LEFT);
        drop.getStyleClass().add("drop-zone");
        drop.setMaxWidth(340);
        drop.setOnMouseClicked(e -> open.run());
        drop.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        drop.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                File f = db.getFiles().get(0);
                File dir = f.isDirectory() ? f : f.getParentFile();
                s.openProject(dir.getName());
            }
            e.setDropCompleted(db.hasFiles());
            e.consume();
        });

        // fonctionnalités
        HBox features = new HBox(12,
                chip(Icons.DROPLET, "Propagation de teinte"),
                chip(Icons.SHIELD, VulnCategory.values().length + " familles de vulnérabilités"),
                chip(Icons.CHART, "Rapports exportables"));
        features.setAlignment(Pos.CENTER);

        // projets récents
        VBox recent = new VBox(8, Ui.label("PROJETS RÉCENTS", "panel-heading-text"));
        recent.setMaxWidth(440);
        for (String r : DemoData.recentProjects()) {
            String[] p = r.split("\\|");
            HBox row = new HBox(10, Icons.of(Icons.FOLDER, 16, Icons.TEAL), Ui.label(p[0], "recent-name"),
                    Ui.spacer(), Ui.label(p[1], "summary-text"));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("recent-row");
            row.setOnMouseClicked(e -> s.openProject(p[0]));
            recent.getChildren().add(row);
        }

        VBox center = new VBox(18, title, sub, openBtn, drop, features, recent);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(56, 24, 40, 24));
        VBox.setMargin(features, new Insets(14, 0, 8, 0));

        VBox page = new VBox(steps, center);
        page.setPadding(new Insets(20, 24, 0, 24));
        VBox.setMargin(steps, new Insets(0));

        setContent(page);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
    }

    private Node chip(String icon, String text) {
        HBox c = new HBox(8, Icons.of(icon, 16, Icons.TEAL), Ui.label(text, "feature-chip-text"));
        c.setAlignment(Pos.CENTER_LEFT);
        c.getStyleClass().add("feature-chip");
        return c;
    }
}