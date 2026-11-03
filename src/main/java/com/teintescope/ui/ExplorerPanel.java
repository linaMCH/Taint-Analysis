package com.teintescope.ui;

import com.teintescope.model.DemoData;
import com.teintescope.model.ProjectFile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ExplorerPanel extends BorderPane {

    private final AppState s;
    private final VBox tree = new VBox();
    private final TextField filter = new TextField();
    private boolean expandedRoot = true, expandedSrc = true;

    public ExplorerPanel(AppState s) {
        this.s = s;
        getStyleClass().add("sidebar");

        Button search = new Button();
        search.getStyleClass().add("btn-flat");
        search.setGraphic(Icons.of(Icons.SEARCH, 16, Icons.MUTED));
        filter.setPromptText("Filtrer les fichiers");
        filter.setVisible(false);
        filter.setManaged(false);
        search.setOnAction(e -> {
            boolean on = !filter.isVisible();
            filter.setVisible(on);
            filter.setManaged(on);
            if (on) filter.requestFocus(); else filter.clear();
        });

        HBox head = new HBox(Ui.label("EXPLORATEUR", "panel-heading-text"), Ui.spacer(), search);
        head.getStyleClass().add("panel-heading");
        head.setAlignment(Pos.CENTER_LEFT);
        VBox top = new VBox(head, filter);
        VBox.setMargin(filter, new Insets(0, 12, 8, 12));
        setTop(top);
        setCenter(Ui.scroll(tree));
        setBottom(summary());

        filter.textProperty().addListener((o, a, b) -> rebuild());
        s.currentFile.addListener((o, a, b) -> rebuild());
        s.projectName.addListener((o, a, b) -> rebuild());
        rebuild();
    }

    private void rebuild() {
        tree.getChildren().clear();
        String name = s.projectName.get().isEmpty() ? "projet" : s.projectName.get();
        tree.getChildren().add(folderRow(name, 0, expandedRoot, () -> { expandedRoot = !expandedRoot; rebuild(); }));
        if (!expandedRoot) return;
        tree.getChildren().add(folderRow("src", 1, expandedSrc, () -> { expandedSrc = !expandedSrc; rebuild(); }));
        if (!expandedSrc) return;
        String q = filter.getText() == null ? "" : filter.getText().toLowerCase();
        for (ProjectFile f : DemoData.files())
            if (f.name().toLowerCase().contains(q)) tree.getChildren().add(fileRow(f));
    }

    private Node folderRow(String name, int depth, boolean open, Runnable toggle) {
        HBox row = new HBox(8,
                Icons.of(open ? Icons.CHEVRON_DOWN : Icons.CHEVRON_RIGHT, 14, Icons.MUTED),
                Icons.of(Icons.FOLDER, 16, Icons.TEXT),
                Ui.label(name, depth == 0 ? "tree-label-bold" : "tree-label"));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("tree-row");
        row.setPadding(new Insets(0, 8, 0, 12 + depth * 16));
        row.setOnMouseClicked(e -> toggle.run());
        return row;
    }

    private Node fileRow(ProjectFile f) {
        Region dot = new Region();
        dot.getStyleClass().addAll("state-dot", "state-" + f.state().name().toLowerCase());
        HBox row = new HBox(10, Icons.of(Icons.FILE, 15, Icons.MUTED), Ui.label(f.name(), "file-name", "mono"), Ui.spacer(), dot);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("file-row");
        if (f.name().equals(s.currentFile.get())) row.getStyleClass().add("selected");
        row.setPadding(new Insets(0, 16, 0, 44));
        row.setOnMouseClicked(e -> s.openFile(f.name()));
        return row;
    }

    private Node summary() {
        int files = DemoData.files().size();
        int alerts = DemoData.vulnerabilities().size();
        HBox a = new HBox(8, Icons.of(Icons.SHIELD, 16, Icons.MUTED), Ui.label(files + " fichiers analysés", "summary-text"));
        a.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(6, a, Ui.label(alerts + (alerts > 1 ? " alertes" : " alerte"), "alerts-text"));
        box.getStyleClass().add("sidebar-summary");
        return box;
    }
}