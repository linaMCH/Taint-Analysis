package com.teintescope.ui;

import com.teintescope.data.SampleData;
import com.teintescope.model.CodeLine;
import com.teintescope.model.CodeSegment;
import com.teintescope.model.SegKind;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Panneau central : onglets + fil d'Ariane + code avec les lignes teintées. */
public class EditorPanel extends VBox {

    public final Button detailsButton = new Button("Détails techniques");

    private final HBox tabsBox = new HBox();
    private final Label breadcrumb = new Label();
    private final VBox codeBox = new VBox();
    private final ScrollPane scroll = new ScrollPane(codeBox);
    private final Map<String, HBox> tabs = new LinkedHashMap<>();

    private List<CodeLine> currentLines = List.of();
    private String current;
    private Consumer<String> onFileChanged = name -> {};

    public EditorPanel() {
        getStyleClass().add("editor-area");

        detailsButton.getStyleClass().addAll("btn", "btn-secondary", "btn-small");
        HBox.setMargin(detailsButton, new Insets(0, 10, 0, 0));
        HBox header = new HBox(tabsBox, Ui.spacer(), detailsButton);
        header.getStyleClass().add("editor-header");
        header.setAlignment(Pos.CENTER_LEFT);

        breadcrumb.setFont(Ui.mono(12));
        breadcrumb.getStyleClass().add("breadcrumb");
        HBox crumbRow = new HBox(breadcrumb);
        crumbRow.getStyleClass().add("breadcrumb-row");
        crumbRow.setAlignment(Pos.CENTER_LEFT);

        codeBox.getStyleClass().add("code-box");
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(header, crumbRow, scroll);
    }

    /** Appelé quand le fichier affiché change (pour synchroniser l'explorateur). */
    public void setOnFileChanged(Consumer<String> handler) {
        this.onFileChanged = handler;
    }

    /** Ouvre un fichier dans un onglet (ou active l'onglet existant). */
    public void openFile(String name) {
        if (!tabs.containsKey(name)) addTab(name);
        show(name);
    }

    /** Fait défiler le code jusqu'à une ligne. */
    public void scrollToLine(int number) {
        for (int i = 0; i < currentLines.size(); i++) {
            if (currentLines.get(i).number() == number) {
                scroll.setVvalue(currentLines.size() <= 1 ? 0 : (double) i / (currentLines.size() - 1));
                return;
            }
        }
    }

    // ---------------------------------------------------------------- onglets

    private void addTab(String name) {
        Label label = new Label(name);
        label.setFont(Ui.mono(13));
        label.getStyleClass().add("tab-label");

        Button close = new Button();
        close.setGraphic(Icons.of(Icons.CLOSE, 12, Color.web("#64748b")));
        close.getStyleClass().add("tab-close");
        close.setOnMouseClicked(e -> {
            closeTab(name);
            e.consume();
        });

        HBox tab = new HBox(10, Icons.of(Icons.FILE, 16, Color.web("#64748b")), label, close);
        tab.setAlignment(Pos.CENTER_LEFT);
        tab.getStyleClass().add("editor-tab");
        tab.setOnMouseClicked(e -> show(name));

        tabs.put(name, tab);
        tabsBox.getChildren().add(tab);
    }

    private void closeTab(String name) {
        HBox tab = tabs.remove(name);
        if (tab == null) return;
        tabsBox.getChildren().remove(tab);
        if (name.equals(current)) {
            if (tabs.isEmpty()) {
                current = null;
                currentLines = List.of();
                codeBox.getChildren().clear();
                breadcrumb.setText("");
            } else {
                List<String> names = new ArrayList<>(tabs.keySet());
                show(names.get(names.size() - 1));
            }
        }
    }

    private void show(String name) {
        current = name;
        tabs.forEach((n, tab) -> {
            if (n.equals(name)) {
                if (!tab.getStyleClass().contains("active")) tab.getStyleClass().add("active");
            } else {
                tab.getStyleClass().remove("active");
            }
        });

        currentLines = SampleData.linesOf(name);   // plus tard : le vrai contenu du fichier
        List<HBox> rows = new ArrayList<>();
        for (CodeLine line : currentLines) rows.add(buildRow(line));
        codeBox.getChildren().setAll(rows);

        // fil d'Ariane d'exemple
        breadcrumb.setText("src  ›  " + name + (name.equals("connexion.php") ? "  ›  authentifier()" : ""));
        scroll.setVvalue(0);
        onFileChanged.accept(name);
    }

    // ------------------------------------------------------------------ lignes

    private HBox buildRow(CodeLine line) {
        Node marker;
        String rowClass;
        switch (line.kind()) {
            case SOURCE -> {
                marker = Icons.of(Icons.DROP, 14, Color.web("#d97796"));
                rowClass = "line-source";
            }
            case FLOW -> {
                marker = Icons.of(Icons.DROP, 14, Color.web("#d97796"));
                rowClass = "line-flow";
            }
            case SINK -> {
                marker = Icons.of(Icons.TRIANGLE, 16, Color.web("#dc2626"));
                rowClass = "line-sink";
            }
            default -> {
                marker = new Region();
                rowClass = "line-normal";
            }
        }

        StackPane markerBox = new StackPane(marker);
        markerBox.setMinWidth(34);
        markerBox.setPrefWidth(34);
        markerBox.setMaxWidth(34);

        Label number = new Label(String.valueOf(line.number()));
        number.setFont(Ui.mono(12));
        number.setMinWidth(48);
        number.setPrefWidth(48);
        number.setPadding(new Insets(0, 12, 0, 0));
        number.setAlignment(Pos.CENTER_RIGHT);
        number.getStyleClass().add("line-number");

        TextFlow code = new TextFlow();
        code.setMaxHeight(Region.USE_PREF_SIZE);   // reste centré verticalement dans la ligne
        for (CodeSegment seg : line.segments()) {
            Text t = new Text(seg.text());
            t.setFont(Ui.mono(15));
            t.getStyleClass().add(segClass(seg.kind()));
            if (seg.kind() == SegKind.SINK_CALL) t.setUnderline(true);
            code.getChildren().add(t);
        }

        HBox row = new HBox(markerBox, number, code);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("code-row", rowClass);
        return row;
    }

    private static String segClass(SegKind kind) {
        return switch (kind) {
            case PLAIN -> "seg-plain";
            case KEYWORD -> "seg-keyword";
            case STRING -> "seg-string";
            case SOURCE_VAR -> "seg-source";
            case TAINTED -> "seg-tainted";
            case SINK_CALL -> "seg-sink";
        };
    }
}
