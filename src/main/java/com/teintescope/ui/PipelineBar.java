package com.teintescope.ui;

import com.teintescope.model.StepInfo;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Barre des 5 phases : lexicale → syntaxique → sémantique → teinte → rapport. */
public class PipelineBar extends HBox {

    public PipelineBar(List<StepInfo> steps) {
        getStyleClass().add("pipeline");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);

        for (int i = 0; i < steps.size(); i++) {
            StepInfo st = steps.get(i);
            StackPane dot = new StackPane();
            dot.getStyleClass().addAll("step-dot", "dot-" + st.status().name().toLowerCase());
            if (st.status() == com.teintescope.model.StepStatus.DONE)
                dot.getChildren().add(Icons.of(Icons.CHECK, 11, Icons.WHITE));
            else
                dot.getChildren().add(Ui.label(String.valueOf(i + 1), "dot-number"));

            VBox txt = new VBox(0, Ui.label(st.title(), "step-title"), Ui.label(st.info(), "step-info"));
            HBox step = new HBox(10, dot, txt);
            step.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(step);

            if (i < steps.size() - 1) {
                Region line = new Region();
                line.getStyleClass().add("step-line");
                line.setMinWidth(16);
                HBox.setHgrow(line, Priority.ALWAYS);
                getChildren().add(line);
            }
        }
    }
}