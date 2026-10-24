package com.teintescope.ui;

import com.teintescope.data.SampleData;
import com.teintescope.model.Vulnerability;
import com.teintescope.ui.PipelineBar.StepState;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/** Fenêtre principale : assemble tous les panneaux et relie leurs actions. */
public class MainView extends BorderPane {

    public MainView() {
        getStyleClass().add("app-root");

        List<Vulnerability> vulnerabilities = SampleData.vulnerabilities();

        TopBar topBar = new TopBar();
        PipelineBar pipeline = new PipelineBar();
        ExplorerPanel explorer = new ExplorerPanel(SampleData.PROJECT, SampleData.files(), vulnerabilities.size());
        EditorPanel editor = new EditorPanel();
        ProblemsPanel problems = new ProblemsPanel(vulnerabilities);

        HBox workspace = new HBox(explorer, editor, problems);
        workspace.getStyleClass().add("workspace");
        HBox.setHgrow(editor, Priority.ALWAYS);

        setTop(new VBox(topBar, pipeline));
        setCenter(workspace);
        setBottom(new StatusBar());

        // --- liaisons entre panneaux
        explorer.setOnFileSelected(editor::openFile);
        editor.setOnFileChanged(explorer::select);
        problems.setOnSelected(v -> {
            editor.openFile(v.file());
            editor.scrollToLine(v.line());
        });

        // --- actions à brancher plus tard (TODO)
        topBar.openButton.setOnAction(e -> System.out.println("TODO : choisir un dossier de projet"));
        topBar.runButton.setOnAction(e -> System.out.println("TODO : lancer l'analyse"));
        topBar.settingsButton.setOnAction(e -> System.out.println("TODO : fenêtre de configuration"));
        editor.detailsButton.setOnAction(e -> System.out.println("TODO : panneau « Détails techniques »"));
        problems.setOnTraceRequested(v -> System.out.println("TODO : afficher la trace du flux de " + v.file()));
        problems.setOnReportRequested(() -> System.out.println("TODO : fenêtre du rapport"));

        // --- état initial de la maquette (données d'exemple)
        String[] times = {"0,3 s", "0,8 s", "1,2 s", "2,4 s", "0,4 s"};
        for (int i = 0; i < times.length; i++) {
            pipeline.setState(i, StepState.DONE, "Terminé · " + times[i]);
        }
        editor.openFile("connexion.php");
        editor.openFile("recherche.php");
        editor.openFile("connexion.php");
        problems.markSelected(vulnerabilities.get(0));

        // --- Gestion du bouton Dark / Light Mode ---
        topBar.themeToggleButton.setOnAction(e -> {
            if (getStyleClass().contains("dark-mode")) {
                getStyleClass().remove("dark-mode");
            } else {
                getStyleClass().add("dark-mode");
            }
        });
    }
}
