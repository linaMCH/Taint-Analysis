package com.teintescope.ui;

import com.teintescope.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ReportDialog {
    private ReportDialog() {}

    public static void show(Window owner, ReportData r, AppState s) {
        Stage[] ref = new Stage[1];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRENCH);
        String duration = String.format(Locale.FRANCE, "%.1f s", r.seconds());

        HBox head = DialogShell.header("Rapport d'analyse",
                r.project() + " · Analyse du " + fmt.format(r.date()) + " · durée " + duration, () -> ref[0].close());

        Map<Severity, Integer> bySev = r.bySeverity();
        Map<VulnCategory, Integer> byCat = r.byCategory();
        int total = r.vulns().size();
        List<String> parts = new ArrayList<>();
        bySev.forEach((k, v) -> { if (v > 0) parts.add(v + " " + k.label().toLowerCase()); });

        HBox stats = new HBox(14,
                stat("Fichiers analysés", String.valueOf(r.files()), r.lines() + " lignes de code"),
                stat("Vulnérabilités", String.valueOf(total), parts.isEmpty() ? "Aucune" : String.join(" · ", parts)),
                stat("Durée de l'analyse", duration, "le " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(r.date())));

        // gravité
        VBox sevPanel = panel("Vulnérabilités par gravité", total + " au total");
        int maxSev = Math.max(1, bySev.values().stream().mapToInt(i -> i).max().orElse(1));
        bySev.forEach((k, v) -> sevPanel.getChildren().add(bar(k.label(), v, maxSev, "fill-" + k.name().toLowerCase())));

        // catégorie
        VBox catPanel = panel("Répartition par catégorie", "7 familles");
        int maxCat = Math.max(1, byCat.values().stream().mapToInt(i -> i).max().orElse(1));
        byCat.forEach((k, v) -> catPanel.getChildren().add(bar(
                k.maturity() == Maturity.AVAILABLE ? k.label() : k.label() + " (" + k.maturity().label().toLowerCase() + ")",
                v, maxCat, "fill-cat")));

        // flux
        VBox fluxPanel = panel("État des flux", (r.taintedFlows() + r.safeFlows()) + " flux");
        HBox legend = new HBox(14, Ui.label("● " + r.safeFlows() + " non teintés", "legend-safe"),
                Ui.label("● " + r.taintedFlows() + " teintés", "legend-tainted"));
        legend.setAlignment(Pos.CENTER);
        fluxPanel.getChildren().addAll(donut(r.taintedFlows(), r.safeFlows()), legend);
        fluxPanel.setAlignment(Pos.TOP_CENTER);

        HBox panels = new HBox(14, sevPanel, catPanel, fluxPanel);
        VBox body = new VBox(14, stats, panels);
        body.setPadding(new Insets(20, 24, 20, 24));
        ScrollPane sp = Ui.scroll(body);
        VBox.setVgrow(sp, Priority.ALWAYS);

        Button pdf = Ui.button("Exporter PDF", Icons.DOWNLOAD, "btn-primary",
                () -> s.status.set("Export PDF : à brancher (bibliothèque PDF à ajouter)"));
        Button html = Ui.button("Exporter HTML", Icons.DOWNLOAD, "btn-secondary", () -> exportHtml(ref[0], r, s));
        HBox footer = new HBox(10, Ui.label("Statistiques issues de la dernière analyse", "summary-text"), Ui.spacer(), html, pdf);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("dialog-footer");

        VBox dialog = new VBox(head, sp, footer);
        ref[0] = DialogShell.show(owner, dialog, 1000, 640);
    }

    private static VBox stat(String label, String value, String sub) {
        VBox v = new VBox(4, Ui.label(label.toUpperCase(), "stat-label"), Ui.label(value, "stat-value"), Ui.label(sub, "stat-sub"));
        v.getStyleClass().add("stat-card");
        v.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    private static VBox panel(String title, String right) {
        HBox h = new HBox(Ui.label(title, "panel-card-title"), Ui.spacer(), Ui.label(right, "summary-text"));
        VBox v = new VBox(12, h);
        v.getStyleClass().add("stat-card");
        v.setMaxWidth(Double.MAX_VALUE);
        v.setPrefWidth(300);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    private static Node bar(String name, int value, int max, String fillClass) {
        Region track = new Region();
        track.getStyleClass().add("bar-track");
        Region fill = new Region();
        fill.getStyleClass().addAll("bar-fill", fillClass);
        StackPane bar = new StackPane(track, fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        fill.maxWidthProperty().bind(bar.widthProperty().multiply(value / (double) max));
        bar.setMinHeight(8);
        bar.setPrefHeight(8);
        bar.setMaxHeight(8);
        HBox.setHgrow(bar, Priority.ALWAYS);

        javafx.scene.control.Label n = Ui.label(name, "bar-name");
        n.setMinWidth(110);
        javafx.scene.control.Label c = Ui.label(String.valueOf(value), "summary-text");
        c.setMinWidth(18);
        HBox row = new HBox(10, n, bar, c);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Node donut(int tainted, int safe) {
        int total = Math.max(1, tainted + safe);
        Circle base = new Circle(46);
        base.setFill(Color.TRANSPARENT);
        base.setStroke(Color.web("#079F91"));
        base.setStrokeWidth(14);
        Arc arc = new Arc(0, 0, 46, 46, 90, -360.0 * tainted / total);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.web("#F07878"));
        arc.setStrokeWidth(14);
        VBox center = new VBox(0, Ui.label(String.valueOf(tainted + safe), "donut-value"), Ui.label("flux", "stat-sub"));
        center.setAlignment(Pos.CENTER);
        StackPane d = new StackPane(base, arc, center);
        d.setPrefSize(130, 130);
        d.setMaxSize(130, 130);
        d.setPadding(new Insets(4));
        return d;
    }

    private static void exportHtml(Window owner, ReportData r, AppState s) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("rapport-teintescope.html");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html"));
        File f = fc.showSaveDialog(owner);
        if (f == null) return;

        StringBuilder h = new StringBuilder("<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"utf-8\"><title>Rapport TeinteScope</title>")
                .append("<style>body{font-family:Inter,Arial,sans-serif;color:#172B3A;margin:40px}h1{color:#079F91}")
                .append("table{border-collapse:collapse;width:100%}td,th{border-bottom:1px solid #E3EAF0;padding:8px;text-align:left}</style></head><body>")
                .append("<h1>Rapport d'analyse : ").append(esc(r.project())).append("</h1>")
                .append("<p>").append(r.files()).append(" fichiers · ").append(r.vulns().size()).append(" vulnérabilités · ")
                .append(r.taintedFlows()).append(" flux teintés / ").append(r.safeFlows()).append(" non teintés</p>")
                .append("<table><tr><th>Gravité</th><th>Catégorie</th><th>Nom</th><th>Fichier</th><th>Ligne</th></tr>");
        for (Vulnerability v : r.vulns())
            h.append("<tr><td>").append(v.severity().label()).append("</td><td>").append(esc(v.category().label()))
                    .append("</td><td>").append(esc(v.name())).append("</td><td>").append(esc(v.file()))
                    .append("</td><td>").append(v.line()).append("</td></tr>");
        h.append("</table></body></html>");
        try {
            Files.writeString(f.toPath(), h.toString());
            s.status.set("Rapport exporté : " + f.getName());
        } catch (IOException e) {
            s.status.set("Échec de l'export : " + e.getMessage());
        }
    }

    private static String esc(String t) {
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}