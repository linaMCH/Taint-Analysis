package com.teintescope.ui;

import com.teintescope.model.*;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;

public class EditorPanel extends BorderPane {

    private final AppState s;
    private final VBox codeBox = new VBox();
    private final ScrollPane scroll = new ScrollPane(codeBox);
    private final HBox tabs = new HBox();
    private final HBox crumbs = new HBox(8);
    private final Map<Integer, HBox> rows = new HashMap<>();

    public EditorPanel(AppState s) {
        this.s = s;
        getStyleClass().add("editor-area");

        Button details = Ui.button("Détails techniques", Icons.CODE, "btn-secondary", () -> s.techVisible.set(!s.techVisible.get()));
        details.getStyleClass().add("btn-small");
        s.techVisible.addListener((o, a, on) -> {
            if (on) details.getStyleClass().add("pressed"); else details.getStyleClass().remove("pressed");
        });
        HBox header = new HBox(tabs, Ui.spacer(), details);
        header.getStyleClass().add("editor-header");
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(details, new Insets(0, 12, 0, 12));
        tabs.setAlignment(Pos.CENTER_LEFT);

        crumbs.getStyleClass().add("breadcrumb-row");
        crumbs.setAlignment(Pos.CENTER_LEFT);

        codeBox.getStyleClass().add("code-box");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox center = new VBox(crumbs, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        TechPanel tech = new TechPanel(s);
        tech.visibleProperty().bind(s.techVisible);
        tech.managedProperty().bind(s.techVisible);

        setTop(header);
        setCenter(center);
        setBottom(tech);

        s.openTabs.addListener((ListChangeListener<String>) c -> rebuildTabs());
        s.currentFile.addListener((o, a, b) -> { rebuildTabs(); render(); });
        s.selectedVuln.addListener((o, a, b) -> refreshHighlight());
        s.selectedStep.addListener((o, a, b) -> refreshHighlight());
        rebuildTabs();
        render();
    }

    // ------------------------------------------------------------------ onglets
    private void rebuildTabs() {
        tabs.getChildren().clear();
        for (String name : s.openTabs) {
            Button x = new Button();
            x.getStyleClass().add("tab-close");
            x.setGraphic(Icons.of(Icons.X, 12, Icons.MUTED));
            x.setOnAction(e -> s.closeTab(name));
            x.setOnMouseClicked(e -> e.consume());

            HBox tab = new HBox(8, Icons.of(Icons.FILE, 14, Icons.MUTED), Ui.label(name, "tab-label", "mono"), x);
            tab.setAlignment(Pos.CENTER_LEFT);
            tab.getStyleClass().add("editor-tab");
            if (name.equals(s.currentFile.get())) tab.getStyleClass().add("active");
            tab.setOnMouseClicked(e -> s.currentFile.set(name));
            tabs.getChildren().add(tab);
        }
    }

    // ------------------------------------------------------------------ code
    private void render() {
        rows.clear();
        codeBox.getChildren().clear();
        crumbs.getChildren().clear();
        String f = s.currentFile.get();
        if (f == null) {
            codeBox.getChildren().add(Ui.label("Aucun fichier ouvert : sélectionnez un fichier dans l'explorateur.", "empty-state"));
            return;
        }
        crumbs.getChildren().addAll(Ui.label("src", "breadcrumb", "mono"), Ui.label("›", "breadcrumb"),
                Ui.label(f, "breadcrumb", "mono"));
        String fn = DemoData.scopeOf(f);
        if (fn != null) crumbs.getChildren().addAll(Ui.label("›", "breadcrumb"), Ui.label(fn, "breadcrumb", "mono"));

        for (CodeLine cl : DemoData.code(f)) {
            HBox row = buildRow(cl);
            rows.put(cl.number(), row);
            codeBox.getChildren().add(row);
        }
        refreshHighlight();
    }

    private HBox buildRow(CodeLine cl) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("code-row");
        switch (cl.kind()) {
            case SOURCE -> row.getStyleClass().add("line-source");
            case FLOW -> row.getStyleClass().add("line-flow");
            case SINK -> row.getStyleClass().add("line-sink");
            default -> { }
        }

        StackPane gutter = new StackPane();
        gutter.setMinWidth(30);
        gutter.setPrefWidth(30);
        if (cl.kind() == LineKind.SINK) gutter.getChildren().add(Icons.of(Icons.ALERT, 14, Icons.DANGER));
        else if (cl.kind() != LineKind.NORMAL) gutter.getChildren().add(Icons.of(Icons.DROPLET, 12, Icons.ACCENT));

        Label num = Ui.label(String.valueOf(cl.number()), "line-number", "mono");
        num.setMinWidth(36);
        num.setAlignment(Pos.CENTER_RIGHT);

        TextFlow flow = new TextFlow();
        for (CodeSegment seg : cl.segments()) {
            Text t = new Text(seg.text());
            t.getStyleClass().addAll("code-text", "seg-" + seg.kind().name().toLowerCase());
            flow.getChildren().add(t);
        }
        HBox.setMargin(flow, new Insets(0, 0, 0, 14));

        row.getChildren().addAll(gutter, num, flow);
        row.setOnMouseClicked(e -> s.cursorLine.set(cl.number()));
        return row;
    }

    /** Met en évidence la ligne liée à l'étape de trace (ou à la vulnérabilité) sélectionnée. */
    private void refreshHighlight() {
        rows.values().forEach(r -> r.getStyleClass().remove("line-active"));
        Vulnerability v = s.selectedVuln.get();
        TraceStep st = s.selectedStep.get();
        int target = -1;
        if (v != null && v.file().equals(s.currentFile.get()))
            target = (st != null && st.line() > 0) ? st.line() : v.line();
        HBox r = rows.get(target);
        if (r == null) return;
        r.getStyleClass().add("line-active");
        Platform.runLater(() -> {
            double extra = codeBox.getHeight() - scroll.getViewportBounds().getHeight();
            if (extra > 0) {
                double y = r.getBoundsInParent().getMinY() - scroll.getViewportBounds().getHeight() / 3;
                scroll.setVvalue(Math.max(0, Math.min(1, y / extra)));
            }
        });
    }
}