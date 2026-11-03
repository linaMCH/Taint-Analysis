package com.teintescope.ui;

import com.teintescope.model.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ConfigDialog {
    private ConfigDialog() {}

    public static void show(Window owner, AppState s) {
        ConfigModel cfg = ConfigModel.INSTANCE;
        Runnable restore = cfg.snapshot();
        Stage[] ref = new Stage[1];

        HBox head = DialogShell.header("Configuration de l'analyse",
                "Personnalisez les règles de propagation de teinte.", () -> { restore.run(); ref[0].close(); });

        // pages
        VBox engine = new VBox(16,
                section(owner, "Sources", "Données venant de l'utilisateur et considérées comme non fiables.", cfg.sources),
                section(owner, "Sinks", "Fonctions dangereuses où une donnée teintée peut créer une faille.", cfg.sinks),
                section(owner, "Assainisseurs", "Fonctions qui nettoient une donnée avant son utilisation.", cfg.sanitizers));
        VBox rules = new VBox(10);
        cfg.rules.forEach((name, prop) -> {
            CheckBox cb = new CheckBox(name);
            cb.setSelected(prop.get());
            cb.setDisable(name.contains("(prévu)"));
            cb.selectedProperty().addListener((o, a, b) -> prop.set(b));
            HBox row = new HBox(cb);
            row.getStyleClass().add("config-row");
            rules.getChildren().add(row);
        });
        VBox cats = new VBox(10);
        for (VulnCategory c : VulnCategory.values()) {
            CheckBox cb = new CheckBox();
            cb.setSelected(cfg.categories.get(c).get());
            cb.setDisable(c.maturity() != Maturity.AVAILABLE);
            cb.selectedProperty().addListener((o, a, b) -> cfg.categories.get(c).set(b));
            HBox row = new HBox(12, Ui.label(c.label(), "bar-name"),
                    Ui.label(c.maturity().label(), "maturity", "maturity-" + c.maturity().name().toLowerCase()),
                    Ui.spacer(), cb);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("config-row");
            cats.getChildren().add(row);
        }

        StackPane pages = new StackPane();
        Node[] nodes = {pad(engine), pad(rules), pad(cats)};
        String[] navNames = {"Moteur de teinte", "Règles d'analyse", "Catégories"};
        List<Button> navBtns = new ArrayList<>();
        VBox nav = new VBox(4);
        nav.getStyleClass().add("config-nav");
        for (int i = 0; i < navNames.length; i++) {
            final int idx = i;
            Button b = new Button(navNames[i]);
            b.getStyleClass().add("nav-item");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> {
                pages.getChildren().setAll(nodes[idx]);
                navBtns.forEach(x -> x.getStyleClass().remove("active"));
                b.getStyleClass().add("active");
            });
            navBtns.add(b);
            nav.getChildren().add(b);
        }
        navBtns.get(0).fire();

        ScrollPane sp = Ui.scroll(pages);
        HBox middle = new HBox(nav, sp);
        HBox.setHgrow(sp, Priority.ALWAYS);
        VBox.setVgrow(middle, Priority.ALWAYS);

        Button imp = Ui.button("Importer JSON", Icons.UPLOAD, "btn-flat-text",
                () -> s.status.set("Import JSON : à brancher (ajouter Gson/Jackson)"));
        Button exp = Ui.button("Exporter JSON", Icons.DOWNLOAD, "btn-flat-text", () -> {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("teintescope-config.json");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
            File f = fc.showSaveDialog(ref[0]);
            if (f != null) writeConfig(f.toPath(), s);
        });
        Button cancel = Ui.button("Annuler", null, "btn-secondary", () -> { restore.run(); ref[0].close(); });
        Button save = Ui.button("Enregistrer", null, "btn-primary", () -> {
            writeConfig(Path.of("teintescope-config.json"), s);
            ref[0].close();
        });
        HBox footer = new HBox(10, imp, exp, Ui.spacer(), cancel, save);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("dialog-footer");

        VBox dialog = new VBox(head, middle, footer);
        ref[0] = DialogShell.show(owner, dialog, 900, 640);
    }

    private static Node pad(Node n) {
        VBox v = new VBox(n);
        v.setPadding(new Insets(20, 24, 20, 24));
        return v;
    }

    private static void writeConfig(Path p, AppState s) {
        try {
            Files.writeString(p, ConfigModel.INSTANCE.toJson());
            s.status.set("Configuration enregistrée : " + p.getFileName());
        } catch (IOException e) {
            s.status.set("Échec de l'enregistrement : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ section (tableau + CRUD)
    private static Node section(Window owner, String title, String desc, ObservableList<ConfigEntry> list) {
        VBox table = new VBox();
        Runnable rebuild = () -> {
            table.getChildren().clear();
            HBox head = new HBox(Ui.label("NOM", "table-head-text"), Ui.spacer(), Ui.label("CATÉGORIE", "table-head-text"));
            head.getStyleClass().add("table-head");
            table.getChildren().add(head);
            for (int i = 0; i < list.size(); i++) {
                final int idx = i;
                ConfigEntry e = list.get(i);
                Label name = Ui.label(e.name(), "mono");
                name.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(name, Priority.ALWAYS);
                Label cat = Ui.label(e.category(), "category-pill");
                Button edit = new Button();
                edit.getStyleClass().add("btn-flat");
                edit.setGraphic(Icons.of(Icons.PENCIL, 15, Icons.MUTED));
                edit.setOnAction(ev -> askEntry(owner, e).ifPresent(n -> list.set(idx, n)));
                Button del = new Button();
                del.getStyleClass().add("btn-flat");
                del.setGraphic(Icons.of(Icons.TRASH, 15, Icons.DANGER));
                del.setOnAction(ev -> list.remove(e));
                HBox row = new HBox(12, name, cat, edit, del);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("table-cell");
                table.getChildren().add(row);
            }
        };
        list.addListener((ListChangeListener<ConfigEntry>) c -> rebuild.run());
        rebuild.run();

        Button add = Ui.button("Ajouter", Icons.PLUS, "btn-secondary", () -> askEntry(owner, null).ifPresent(list::add));
        add.getStyleClass().add("btn-small");
        HBox top = new HBox(new VBox(2, Ui.label(title, "panel-card-title"), Ui.label(desc, "summary-text")), Ui.spacer(), add);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, top, table);
        card.getStyleClass().add("stat-card");
        return card;
    }

    private static Optional<ConfigEntry> askEntry(Window owner, ConfigEntry init) {
        Dialog<ConfigEntry> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle(init == null ? "Ajouter une entrée" : "Modifier l'entrée");
        d.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField name = new TextField(init == null ? "" : init.name());
        name.setPromptText("ex. mysqli_query");
        ComboBox<String> cat = new ComboBox<>();
        cat.getItems().add("Toutes");
        for (VulnCategory c : VulnCategory.values()) cat.getItems().add(c.shortName());
        cat.setValue(init == null ? "Toutes" : init.category());

        VBox box = new VBox(8, new Label("Nom"), name, new Label("Catégorie"), cat);
        box.setPadding(new Insets(16));
        d.getDialogPane().setContent(box);
        d.getDialogPane().lookupButton(ok).disableProperty().bind(name.textProperty().isEmpty());
        d.setResultConverter(bt -> bt == ok ? new ConfigEntry(name.getText().trim(), cat.getValue()) : null);
        return d.showAndWait();
    }
}