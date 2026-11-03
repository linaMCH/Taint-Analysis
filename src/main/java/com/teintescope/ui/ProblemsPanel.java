package com.teintescope.ui;

import com.teintescope.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProblemsPanel extends BorderPane {

    private final AppState s;
    private final VBox cards = new VBox(10);
    private final Label sub = Ui.label("", "panel-sub");
    private final FlowPane chips = new FlowPane(8, 8);

    public ProblemsPanel(AppState s, Runnable openReport) {
        this.s = s;
        getStyleClass().add("problems-panel");

        VBox listView = buildListView();
        TraceView traceView = new TraceView(s);
        StackPane body = new StackPane(listView, traceView);
        listView.visibleProperty().bind(s.traceMode.not());
        traceView.visibleProperty().bind(s.traceMode);
        setCenter(body);

        HBox report = new HBox(10, Icons.of(Icons.CHART, 16, Icons.TEAL),
                Ui.label("Ouvrir le rapport complet", "report-label"), Ui.spacer(),
                Icons.of(Icons.ARROW_RIGHT, 16, Icons.MUTED));
        report.setAlignment(Pos.CENTER_LEFT);
        report.getStyleClass().add("report-button");
        report.setOnMouseClicked(e -> openReport.run());
        BorderPane.setMargin(report, new Insets(0, 14, 14, 14));
        setBottom(report);

        s.filter.addListener((o, a, b) -> refresh());
        s.selectedVuln.addListener((o, a, b) -> refresh());
        refresh();
    }

    private VBox buildListView() {
        VBox header = new VBox(3, Ui.label("Problèmes détectés", "panel-title"), sub);
        header.getStyleClass().add("problems-header");

        Node banner = banner();
        VBox top = new VBox(12, chips, banner);
        top.setPadding(new Insets(14, 14, 4, 14));

        cards.setPadding(new Insets(10, 14, 14, 14));
        ScrollPane sp = Ui.scroll(cards);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return new VBox(header, top, sp);
    }

    private Node banner() {
        String available = Arrays.stream(VulnCategory.values()).filter(c -> c.maturity() == Maturity.AVAILABLE)
                .map(VulnCategory::label).collect(Collectors.joining(", "));
        long others = Arrays.stream(VulnCategory.values()).filter(c -> c.maturity() != Maturity.AVAILABLE).count();
        Label l = Ui.label("Détection active : " + available + ". " + others + " autres catégories en cours de développement ou prévues.", "banner-text");
        l.setWrapText(true);
        HBox b = new HBox(10, Icons.of(Icons.INFO, 16, Icons.TEAL), l);
        b.setAlignment(Pos.CENTER_LEFT);
        b.getStyleClass().add("banner");
        return b;
    }

    private List<Vulnerability> filtered() {
        VulnFamily f = s.filter.get();
        return DemoData.vulnerabilities().stream()
                .filter(v -> f == VulnFamily.ALL || v.category().family() == f).toList();
    }

    private void refresh() {
        List<Vulnerability> list = filtered();
        sub.setText(list.size() + (list.size() > 1 ? " éléments" : " élément") + " à examiner");

        chips.getChildren().clear();
        for (VulnFamily fam : VulnFamily.values()) {
            Button b = new Button(fam.label());
            b.getStyleClass().add("chip");
            if (fam == s.filter.get()) b.getStyleClass().add("active");
            b.setOnAction(e -> s.filter.set(fam));
            chips.getChildren().add(b);
        }

        cards.getChildren().clear();
        if (list.isEmpty()) {
            boolean anyAvailable = Arrays.stream(VulnCategory.values())
                    .anyMatch(c -> c.family() == s.filter.get() && c.maturity() == Maturity.AVAILABLE);
            Label empty = Ui.label(anyAvailable || s.filter.get() == VulnFamily.ALL
                    ? "Aucune vulnérabilité détectée dans cette catégorie."
                    : "La détection de cette famille est en cours de développement.", "empty-state");
            empty.setWrapText(true);
            cards.getChildren().add(empty);
            return;
        }
        list.forEach(v -> cards.getChildren().add(card(v)));
    }

    private Node card(Vulnerability v) {
        HBox top = new HBox(8, Ui.label(v.severity().label().toUpperCase(), "severity", v.severity().css()),
                Ui.spacer(), Ui.label(v.category().shortName(), "category-pill"));
        top.setAlignment(Pos.CENTER_LEFT);

        Label desc = Ui.label(v.description(), "card-desc");
        desc.setWrapText(true);

        Button trace = new Button("Voir la trace");
        trace.getStyleClass().add("link-btn");
        trace.setGraphic(Icons.of(Icons.ARROW_RIGHT, 14, Icons.TEAL));
        trace.setContentDisplay(ContentDisplay.RIGHT);
        trace.setOnAction(e -> s.showTrace(v));

        VBox c = new VBox(8, top, Ui.label(v.name(), "card-title"),
                Ui.label(v.file() + " · ligne " + v.line(), "file-ref", "mono"), desc, trace);
        c.getStyleClass().addAll("card", "bar-" + v.severity().name().toLowerCase());
        if (v.equals(s.selectedVuln.get())) c.getStyleClass().add("selected");
        c.setOnMouseClicked(e -> s.selectCard(v));
        return c;
    }
}