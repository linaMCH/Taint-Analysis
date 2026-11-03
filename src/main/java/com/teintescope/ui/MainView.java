package com.teintescope.ui;

import com.teintescope.model.DemoData;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class MainView extends BorderPane {

    private final AppState state = new AppState();

    public MainView() {
        getStyleClass().add("app-root");

        setTop(new TopBar(state, this::chooseProject, this::launchAnalysis, this::openConfig));

        WelcomeView welcome = new WelcomeView(state, this::chooseProject);
        Node workspace = buildWorkspace();
        setCenter(welcome);
        state.projectOpen.addListener((o, was, now) -> setCenter(now ? workspace : welcome));

        setBottom(new StatusBar(state));
    }

    private Node buildWorkspace() {
        ExplorerPanel explorer = new ExplorerPanel(state);
        EditorPanel editor = new EditorPanel(state);
        ProblemsPanel problems = new ProblemsPanel(state, this::openReport);

        explorer.setMinWidth(210);
        editor.setMinWidth(380);
        problems.setMinWidth(320);

        SplitPane split = new SplitPane(explorer, editor, problems);
        split.getStyleClass().add("workspace-split");
        split.setDividerPositions(0.16, 0.73);
        SplitPane.setResizableWithParent(explorer, false);
        SplitPane.setResizableWithParent(problems, false);

        VBox box = new VBox(new PipelineBar(DemoData.pipeline()), split);
        VBox.setVgrow(split, Priority.ALWAYS);
        box.getStyleClass().add("workspace");
        return box;
    }

    private void chooseProject() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Ouvrir un projet PHP");
        File dir = dc.showDialog(getScene().getWindow());
        if (dir != null) state.openProject(dir.getName());   // TODO : scanner réellement le dossier
    }

    private void launchAnalysis() {
        state.status.set("Moteur d'analyse non connecté : résultats de démonstration affichés");
    }

    private void openConfig() {
        ConfigDialog.show(getScene().getWindow(), state);
    }

    private void openReport() {
        ReportDialog.show(getScene().getWindow(), DemoData.report(state.projectName.get()), state);
    }
}