package com.teintescope.ui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

/** Petits utilitaires partagés par les composants. */
final class Ui {
    private static String monoFamily;

    private Ui() {}

    /** Espace extensible (pousse les éléments suivants vers la droite dans un HBox). */
    static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Police à chasse fixe : DM Mono si elle est installée, sinon la police monospace du système. */
    static Font mono(double size) {
        if (monoFamily == null) {
            monoFamily = Font.getFamilies().contains("DM Mono") ? "DM Mono" : "Monospaced";
        }
        return Font.font(monoFamily, size);
    }
}
