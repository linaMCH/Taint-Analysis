package com.teintescope.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Barre des 5 étapes de l'analyse (phases du cours). */
public class PipelineBar extends HBox {

    public enum StepState { PENDING, ACTIVE, DONE, ERROR }

    private static final String[] TITLES = {
            "Analyse lexicale", "Analyse syntaxique", "Analyse sémantique",
            "Propagation de teinte", "Rapport"
    };

    private final StackPane[] dots = new StackPane[TITLES.length];
    private final Label[] infos = new Label[TITLES.length];

    public PipelineBar() {
        getStyleClass().add("pipeline");
        setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < TITLES.length; i++) {
            dots[i] = new StackPane();
            dots[i].getStyleClass().add("step-dot");

            Label title = new Label(TITLES[i]);
            title.getStyleClass().add("step-title");
            infos[i] = new Label();
            infos[i].getStyleClass().add("step-info");

            VBox texts = new VBox(title, infos[i]);
            texts.setAlignment(Pos.CENTER_LEFT);
            HBox step = new HBox(8, dots[i], texts);
            step.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(step);

            if (i < TITLES.length - 1) {
                Region line = new Region();
                line.getStyleClass().add("step-line");
                line.setMinSize(20, 2);
                line.setMaxHeight(2);
                HBox.setHgrow(line, Priority.ALWAYS);
                HBox.setMargin(line, new Insets(0, 16, 0, 16));
                getChildren().add(line);
            }
            setState(i, StepState.PENDING, "En attente");
        }
    }

    /** Met à jour une étape (index de 0 à 4). Sera appelée par l'analyseur plus tard. */
    public void setState(int index, StepState state, String info) {
        StackPane dot = dots[index];
        dot.getStyleClass().removeAll("dot-pending", "dot-active", "dot-done", "dot-error");
        dot.getStyleClass().add("dot-" + state.name().toLowerCase());
        infos[index].setText(info);

        dot.getChildren().clear();
        if (state == StepState.DONE) {
            dot.getChildren().add(Icons.of(Icons.CHECK, 10, Color.WHITE, 3.2));
        } else {
            Label n = new Label(state == StepState.ERROR ? "!" : String.valueOf(index + 1));
            n.getStyleClass().add("dot-number");
            dot.getChildren().add(n);
        }
    }
}
