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

import java.util.ArrayList;
import java.util.List;

/** Détails techniques : Tokens, AST, Table des symboles, Journal / Erreurs. */
public class TechPanel extends VBox {

    private final StackPane content = new StackPane();
    private final List<Button> buttons = new ArrayList<>();

    public TechPanel(AppState s) {
        getStyleClass().add("tech-panel");
        setPrefHeight(300);
        content.setAlignment(Pos.TOP_LEFT);

        String[] names = {"Tokens", "AST", "Table des symboles", "Journal / Erreurs"};
        long issues = DemoData.logs().stream().filter(l -> !l.level().equals("INFO")).count();
        HBox tabBar = new HBox(4);
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            Button b = new Button(names[i]);
            b.getStyleClass().add("tech-tab");
            if (i == 3 && issues > 0) {
                b.setGraphic(Ui.label(String.valueOf(issues), "tech-badge"));
                b.setContentDisplay(ContentDisplay.RIGHT);
            }
            b.setOnAction(e -> select(idx));
            buttons.add(b);
            tabBar.getChildren().add(b);
        }
        Button close = new Button();
        close.getStyleClass().add("btn-flat");
        close.setGraphic(Icons.of(Icons.X, 16, Icons.MUTED));
        close.setOnAction(e -> s.techVisible.set(false));

        HBox bar = new HBox(tabBar, Ui.spacer(), close);
        bar.getStyleClass().add("tech-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        ScrollPane sp = Ui.scroll(content);
        VBox.setVgrow(sp, Priority.ALWAYS);
        getChildren().addAll(bar, sp);
        select(0);
    }

    private void select(int i) {
        for (int k = 0; k < buttons.size(); k++) {
            buttons.get(k).getStyleClass().remove("active");
            if (k == i) buttons.get(k).getStyleClass().add("active");
        }
        Node n = switch (i) {
            case 0 -> tokens();
            case 1 -> ast();
            case 2 -> symbols();
            default -> log();
        };
        content.getChildren().setAll(n);
    }

    // ---------------------------------------------------------------- tokens
    private Node tokens() {
        FlowPane fp = new FlowPane(8, 8);
        fp.setPadding(new Insets(16));
        for (TokenInfo t : DemoData.tokens()) {
            HBox chip = new HBox(8, Ui.label(t.type(), "token-type"), Ui.label(t.value(), "mono"));
            chip.setAlignment(Pos.CENTER_LEFT);
            chip.getStyleClass().add("token-chip");
            if (t.source()) chip.getStyleClass().add("token-source");
            fp.getChildren().add(chip);
        }
        return fp;
    }

    // ---------------------------------------------------------------- AST
    private Node ast() {
        VBox box = new VBox(astNode(DemoData.ast(), 0));
        box.setPadding(new Insets(10, 16, 10, 16));
        return box;
    }

    private Node astNode(AstNode n, int depth) {
        boolean has = !n.children().isEmpty();
        StackPane chev = new StackPane(has ? Icons.of(Icons.CHEVRON_DOWN, 14, Icons.MUTED) : new Region());
        chev.setMinWidth(14);
        HBox row = new HBox(8, chev, Ui.label(n.kind(), "ast-kind"), Ui.label(n.label(), "ast-label", "mono"));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("ast-row");
        row.setPadding(new Insets(4, 0, 4, depth * 22));

        VBox kids = new VBox();
        for (AstNode c : n.children()) kids.getChildren().add(astNode(c, depth + 1));
        if (has) {
            row.setOnMouseClicked(e -> {
                boolean show = !kids.isVisible();
                kids.setVisible(show);
                kids.setManaged(show);
                chev.getChildren().setAll(Icons.of(show ? Icons.CHEVRON_DOWN : Icons.CHEVRON_RIGHT, 14, Icons.MUTED));
            });
        }
        return new VBox(row, kids);
    }

    // ---------------------------------------------------------------- symboles
    private Node symbols() {
        GridPane g = new GridPane();
        double[] pct = {30, 15, 22, 13, 20};
        for (double p : pct) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(p);
            g.getColumnConstraints().add(cc);
        }
        String[] heads = {"NOM DE LA VARIABLE", "TYPE", "PORTÉE", "DÉCLARATION", "ÉTAT DE TEINTE"};
        for (int c = 0; c < heads.length; c++) g.add(cell(Ui.label(heads[c], "table-head-text"), "table-head"), c, 0);

        int r = 1;
        for (SymbolEntry e : DemoData.symbols()) {
            boolean t = e.state() == TaintState.TAINTED;
            g.add(cell(Ui.label(e.name(), "symbol-name", "mono"), "table-cell"), 0, r);
            g.add(cell(new Label(e.type()), "table-cell"), 1, r);
            g.add(cell(Ui.label(e.scope(), "mono"), "table-cell"), 2, r);
            g.add(cell(new Label(String.valueOf(e.line())), "table-cell"), 3, r);
            g.add(cell(Ui.label(t ? "TAINTED" : "SAFE", "badge", t ? "badge-tainted" : "badge-safe"), "table-cell"), 4, r);
            r++;
        }
        return g;
    }

    private HBox cell(Node n, String cls) {
        HBox h = new HBox(n);
        h.setAlignment(Pos.CENTER_LEFT);
        h.getStyleClass().add(cls);
        return h;
    }

    // ---------------------------------------------------------------- journal
    private Node log() {
        VBox box = new VBox(2);
        box.setPadding(new Insets(10, 16, 10, 16));
        List<LogEntry> logs = new ArrayList<>(DemoData.logs());
        java.util.Collections.reverse(logs);
        for (LogEntry l : logs) {
            Label time = Ui.label(l.time(), "log-time", "mono");
            Label phase = Ui.label(l.phase(), "log-phase", "mono");
            phase.setMinWidth(100);
            HBox row = new HBox(12, time, phase);
            row.setAlignment(Pos.CENTER_LEFT);
            if (!l.level().equals("INFO"))
                row.getChildren().add(Ui.label(l.level().equals("WARN") ? "AVERTISSEMENT" : "ERREUR", "log-level",
                        l.level().equals("WARN") ? "log-warn" : "log-error"));
            row.getChildren().add(Ui.label(l.message(), "mono"));
            row.setPadding(new Insets(6, 0, 6, 0));
            box.getChildren().add(row);
        }
        return box;
    }
}