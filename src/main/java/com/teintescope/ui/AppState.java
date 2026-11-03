package com.teintescope.ui;

import com.teintescope.model.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** État partagé entre tous les panneaux (sélection, onglets, filtres, mode trace…). */
public class AppState {
    public final BooleanProperty projectOpen = new SimpleBooleanProperty(false);
    public final StringProperty projectName = new SimpleStringProperty("");
    public final StringProperty currentFile = new SimpleStringProperty();
    public final ObservableList<String> openTabs = FXCollections.observableArrayList();
    public final ObjectProperty<Vulnerability> selectedVuln = new SimpleObjectProperty<>();
    public final ObjectProperty<TraceStep> selectedStep = new SimpleObjectProperty<>();
    public final BooleanProperty traceMode = new SimpleBooleanProperty(false);
    public final ObjectProperty<VulnFamily> filter = new SimpleObjectProperty<>(VulnFamily.ALL);
    public final BooleanProperty techVisible = new SimpleBooleanProperty(false);
    public final IntegerProperty cursorLine = new SimpleIntegerProperty(1);
    public final StringProperty status = new SimpleStringProperty("Prêt");

    public void openProject(String name) {
        openTabs.clear();
        currentFile.set(null);
        selectedVuln.set(null);
        selectedStep.set(null);
        traceMode.set(false);
        filter.set(VulnFamily.ALL);
        projectName.set(name);
        projectOpen.set(true);
        openFile("connexion.php");   // TODO : ouvrir le premier fichier du vrai projet
        status.set("Projet « " + name + " » ouvert · données de démonstration");
    }

    public void openFile(String name) {
        if (!openTabs.contains(name)) openTabs.add(name);
        currentFile.set(name);
    }

    public void closeTab(String name) {
        int i = openTabs.indexOf(name);
        openTabs.remove(name);
        if (name.equals(currentFile.get()))
            currentFile.set(openTabs.isEmpty() ? null : openTabs.get(Math.max(0, i - 1)));
    }

    public void selectCard(Vulnerability v) {
        selectedVuln.set(v);
        selectedStep.set(null);
        if (!v.file().equals(currentFile.get())) openFile(v.file());
        cursorLine.set(v.line());
    }

    public void showTrace(Vulnerability v) {
        selectCard(v);
        traceMode.set(true);
    }

    public void backToList() {
        traceMode.set(false);
        selectedStep.set(null);
    }
}