package com.teintescope.ui;

import com.teintescope.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

/** Visualisation verticale du chemin de teinte : SOURCE → PROPAGATION → TRANSFORMATION → ASSAINISSEUR → SINK. */
public class TraceView extends VBox {

    private final AppState s;
    private final VBox steps = new VBox();
    private final Label sub = Ui.label("", "panel-sub");

    public TraceView(AppState s) {
        this.s = s;

        Button back = new Button("Problèmes détectés");
        back.getStyleClass().add("link-btn");
        back.setGraphic(Icons.of(Icons.ARROW_LEFT, 14, Icons.TEAL));
        back.setOnAction(e -> s.backToList());

        VBox header = new VBox(6, back, Ui.label("Chemin vulnérable", "panel-title"), sub);
        header.getStyleClass().add("problems-header");

        steps.setPadding(new Insets(16, 16, 16, 18));
        ScrollPane sp = Ui.scroll(steps);
        VBox.setVgrow(sp, Priority.ALWAYS);
        getChildren().addAll(header, sp);

        s.selectedVuln.addListener((o, a, b) -> rebuild());
        s.selectedStep.addListener((o, a, b) -> rebuild());
        rebuild();
    }

    private void rebuild() {
        steps.getChildren().clear();
        Vulnerability v = s.selectedVuln.get();
        if (v == null) return;
        List<TraceStep> list = v.trace();
        sub.setText(list.size() + " étapes · " + v.category().label());
        for (int i = 0; i < list.size(); i++)
            steps.getChildren().add(stepNode(list.get(i), i == list.size() - 1));

        Label t = Ui.label("Chemin assaini", "fix-title");
        Label f = Ui.label(v.fix(), "fix-text");
        f.setWrapText(true);
        HBox th = new HBox(8, Icons.of(Icons.SHIELD, 16, Color.web("#159B72")), t);
        th.setAlignment(Pos.CENTER_LEFT);
        VBox fix = new VBox(6, th, f);
        fix.getStyleClass().add("fix-box");
        VBox.setMargin(fix, new Insets(14, 0, 0, 0));
        steps.getChildren().add(fix);
    }

    private Node stepNode(TraceStep st, boolean last) {
        String icon = switch (st.kind()) {
            case SOURCE -> Icons.DROPLET;
            case PROPAGATION -> Icons.ARROW_RIGHT;
            case TRANSFORMATION -> Icons.CODE;
            case SANITIZER -> Icons.SHIELD;
            case SINK -> Icons.ALERT;
        };
        StackPane dot = new StackPane(Icons.of(icon, 16, Color.web(st.kind().color())));
        dot.getStyleClass().addAll("trace-dot", st.kind().css());

        VBox left = new VBox(4, dot);
        left.setAlignment(Pos.TOP_CENTER);
        if (!last) {
            Region line = new Region();
            line.getStyleClass().add("trace-line");
            line.setMinWidth(2);
            line.setMaxWidth(2);
            line.setMinHeight(40);
            VBox.setVgrow(line, Priority.ALWAYS);
            left.getChildren().add(line);
        }

        String where = st.line() > 0 ? " · Ligne " + st.line() : " · Non rencontré";
        Label code = Ui.label(st.code(), "trace-code", "mono");
        code.setWrapText(true);
        Label expl = Ui.label(st.explanation(), "trace-expl");
        expl.setWrapText(true);
        boolean tainted = st.state() == TaintState.TAINTED;
        Label badge = Ui.label(tainted ? "TEINTÉ" : "ASSAINI", "badge", tainted ? "badge-tainted" : "badge-safe");

        VBox right = new VBox(3, Ui.label(st.kind().label() + where, "trace-kind", st.kind().css()), code, expl, badge);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox row = new HBox(12, left, right);
        row.getStyleClass().add("trace-step");
        if (Objects.equals(st, s.selectedStep.get())) row.getStyleClass().add("selected");
        row.setOnMouseClicked(e -> s.selectedStep.set(st));
        return row;
    }
}