package com.teintescope.ui;

import com.teintescope.model.Vulnerability;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Panneau de droite : liste des problèmes détectés + accès au rapport. */
public class ProblemsPanel extends VBox {

    private final Map<Vulnerability, VBox> cards = new LinkedHashMap<>();
    private Consumer<Vulnerability> onSelected = v -> {};
    private Consumer<Vulnerability> onTraceRequested = v -> {};
    private Runnable onReportRequested = () -> {};

    public ProblemsPanel(List<Vulnerability> vulnerabilities) {
        getStyleClass().add("problems-panel");
        setMinWidth(320);
        setPrefWidth(360);

        int n = vulnerabilities.size();
        Label title = new Label("Problèmes détectés");
        title.getStyleClass().add("panel-title");
        Label sub = new Label(n + (n > 1 ? " éléments à examiner" : " élément à examiner"));
        sub.getStyleClass().add("panel-sub");
        VBox header = new VBox(3, title, sub);
        header.getStyleClass().add("problems-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox list = new VBox(12);
        list.getStyleClass().add("problems-list");
        for (Vulnerability v : vulnerabilities) {
            VBox card = buildCard(v);
            cards.put(v, card);
            list.getChildren().add(card);
        }
        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("problems-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, scroll, buildReportBar());
    }

    public void setOnSelected(Consumer<Vulnerability> handler) { this.onSelected = handler; }
    public void setOnTraceRequested(Consumer<Vulnerability> handler) { this.onTraceRequested = handler; }
    public void setOnReportRequested(Runnable handler) { this.onReportRequested = handler; }

    /** Sélectionne une carte et prévient l'écouteur. */
    public void select(Vulnerability v) {
        markSelected(v);
        onSelected.accept(v);
    }

    /** Surligne une carte sans prévenir l'écouteur. */
    public void markSelected(Vulnerability v) {
        cards.forEach((vuln, card) -> {
            if (vuln.equals(v)) {
                if (!card.getStyleClass().contains("selected")) card.getStyleClass().add("selected");
            } else {
                card.getStyleClass().remove("selected");
            }
        });
    }

    private VBox buildCard(Vulnerability v) {
        Label badge = new Label(v.severity().label().toUpperCase());
        badge.getStyleClass().addAll("severity", v.severity().cssClass());

        Label ref = new Label(v.file() + ":" + v.line());
        ref.setFont(Ui.mono(11));
        ref.getStyleClass().add("file-ref");

        HBox top = new HBox(badge, Ui.spacer(), ref);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(v.title());
        title.getStyleClass().add("card-title");

        Label desc = new Label(v.description());
        desc.setWrapText(true);
        desc.getStyleClass().add("card-desc");

        Button trace = new Button("Voir la trace");
        trace.setGraphic(Icons.of(Icons.ARROW_RIGHT, 16, Color.web("#087f72")));
        trace.setContentDisplay(ContentDisplay.RIGHT);
        trace.getStyleClass().add("link-btn");
        trace.setOnAction(e -> onTraceRequested.accept(v));

        VBox card = new VBox(8, top, title, desc, trace);
        card.getStyleClass().add("card");
        card.setOnMouseClicked(e -> select(v));
        return card;
    }

    private HBox buildReportBar() {
        Label label = new Label("Ouvrir le rapport complet");
        label.getStyleClass().add("report-label");

        HBox button = new HBox(10,
                Icons.of(Icons.CHART, 18, Color.web("#334155")),
                label,
                Ui.spacer(),
                Icons.of(Icons.ARROW_RIGHT, 18, Color.web("#334155")));
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("report-button");
        button.setOnMouseClicked(e -> onReportRequested.run());

        HBox footer = new HBox(button);
        footer.getStyleClass().add("problems-footer");
        HBox.setHgrow(button, Priority.ALWAYS);
        return footer;
    }
}
