package com.teintescope.ui;

import com.teintescope.model.FileState;
import com.teintescope.model.ProjectFile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Panneau de gauche : arborescence du projet + résumé. */
public class ExplorerPanel extends VBox {

    private static final Color MUTED = Color.web("#64748b");

    private final Map<String, HBox> rows = new LinkedHashMap<>();
    private Consumer<String> onFileSelected = name -> {};

    public ExplorerPanel(String projectName, List<ProjectFile> files, int alertCount) {
        getStyleClass().add("sidebar");
        setMinWidth(224);
        setPrefWidth(224);
        setMaxWidth(224);

        Label heading = new Label("EXPLORATEUR");
        heading.getStyleClass().add("panel-heading-text");
        Button search = new Button();
        search.setGraphic(Icons.of(Icons.SEARCH, 18, MUTED));
        search.getStyleClass().add("btn-flat");
        HBox head = new HBox(heading, Ui.spacer(), search);
        head.getStyleClass().add("panel-heading");
        head.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(head,
                treeRow(projectName, 12, true),
                treeRow("src", 28, false));

        for (ProjectFile f : files) {
            HBox row = fileRow(f);
            rows.put(f.name(), row);
            getChildren().add(row);
        }

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        getChildren().addAll(grow, summary(files.size(), alertCount));
    }

    /** Appelé quand l'utilisateur clique sur un fichier. */
    public void setOnFileSelected(Consumer<String> handler) {
        this.onFileSelected = handler;
    }

    /** Surligne un fichier (sans déclencher le gestionnaire). */
    public void select(String name) {
        rows.forEach((n, row) -> {
            if (n.equals(name)) {
                if (!row.getStyleClass().contains("selected")) row.getStyleClass().add("selected");
            } else {
                row.getStyleClass().remove("selected");
            }
        });
    }

    private HBox treeRow(String text, double indent, boolean bold) {
        Label label = new Label(text);
        label.getStyleClass().add(bold ? "tree-label-bold" : "tree-label");
        HBox row = new HBox(8,
                Icons.of(Icons.CHEVRON_RIGHT, 14, MUTED),
                Icons.of(Icons.FOLDER, 18, Color.web("#334155")),
                label);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 12, 0, indent));
        row.getStyleClass().add("tree-row");
        return row;
    }

    private HBox fileRow(ProjectFile f) {
        Label name = new Label(f.name());
        name.setFont(Ui.mono(12));
        name.setMinWidth(Region.USE_PREF_SIZE);   // évite que le nom soit tronqué (...)
        name.getStyleClass().add("file-name");

        Circle dot = new Circle(3.5, switch (f.state()) {
            case CLEAN -> Color.web("#0e9f8e");
            case WARNING -> Color.web("#b7791f");
            case DANGER -> Color.web("#dc2626");
        });

        HBox row = new HBox(10, Icons.of(Icons.FILE, 18, MUTED), name, Ui.spacer(), dot);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 14, 0, 36));
        row.getStyleClass().add("file-row");
        row.setOnMouseClicked(e -> {
            select(f.name());
            onFileSelected.accept(f.name());
        });
        return row;
    }

    private Node summary(int fileCount, int alertCount) {
        Label files = new Label(fileCount + " fichiers analysés");
        files.getStyleClass().add("summary-text");
        HBox top = new HBox(10, Icons.of(Icons.SHIELD, 18, MUTED), files);
        top.setAlignment(Pos.CENTER_LEFT);

        Label alerts = new Label(alertCount + (alertCount > 1 ? " alertes" : " alerte"));
        alerts.getStyleClass().add("alerts-text");
        VBox.setMargin(alerts, new Insets(0, 0, 0, 28));

        VBox box = new VBox(8, top, alerts);
        box.getStyleClass().add("sidebar-summary");
        return box;
    }
}
