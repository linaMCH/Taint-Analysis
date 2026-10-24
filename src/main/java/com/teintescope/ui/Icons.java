package com.teintescope.ui;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;

/** Icônes vectorielles (style « Feather » : grille 24x24, tracé sans remplissage). */
public final class Icons {
    private Icons() {}

    public static final String FOLDER = "M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z";
    public static final String FILE = "M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z M13 2v7h7";
    public static final String SEARCH = "M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z M21 21l-4.35-4.35";
    public static final String PLAY = "M5 3l14 9-14 9z";
    public static final String DROP = "M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z";
    public static final String TRIANGLE = "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z M12 9v4 M12 17h.01";
    public static final String CHECK = "M20 6L9 17l-5-5";
    public static final String SHIELD = "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z";
    public static final String CHEVRON_RIGHT = "M9 18l6-6-6-6";
    public static final String CLOSE = "M18 6L6 18 M6 6l12 12";
    public static final String ARROW_RIGHT = "M5 12h14 M12 5l7 7-7 7";
    public static final String CHART = "M18 20V10 M12 20V4 M6 20v-6";
    public static final String SETTINGS = "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z "
            + "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z";

    public static Node of(String path, double size, Color color) {
        return of(path, size, color, 2);
    }

    public static Node of(String path, double size, Color color, double strokeWidth) {
        SVGPath p = new SVGPath();
        p.setContent(path);
        p.setFill(null);
        p.setStroke(color);
        p.setStrokeWidth(strokeWidth);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        double k = size / 24.0;
        p.getTransforms().add(new Scale(k, k, 12, 12));   // redimensionne autour du centre
        p.setLayoutX(size / 2 - 12);
        p.setLayoutY(size / 2 - 12);

        Pane box = new Pane(p);
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        box.setMouseTransparent(true);
        return box;
    }
}
