package com.teintescope;

import com.teintescope.ui.MainView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        loadFonts();

        // Zone réellement utilisable (hors barre des tâches), tenant compte de la mise à l'échelle
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        double w = Math.min(1400, screen.getWidth() * 0.92);
        double h = Math.min(850, screen.getHeight() * 0.90);

        Scene scene = new Scene(new MainView(), w, h);
        scene.getStylesheets().add(
                getClass().getResource("/styles/teintescope.css").toExternalForm());

        InputStream logo = getClass().getResourceAsStream("/images/logo.png");
        if (logo != null) stage.getIcons().add(new Image(logo));

        stage.setTitle("TeinteScope");
        // Les minimums ne doivent JAMAIS dépasser la taille de l'écran
        stage.setMinWidth(Math.min(1000, screen.getWidth() * 0.95));
        stage.setMinHeight(Math.min(620, screen.getHeight() * 0.90));
        stage.setScene(scene);

        // Centrage dans la zone visible
        stage.setX(screen.getMinX() + (screen.getWidth() - w) / 2);
        stage.setY(screen.getMinY() + (screen.getHeight() - h) / 2);

        stage.show();
    }

    /** Charge les polices placées dans src/main/resources/fonts (facultatif). */
    private void loadFonts() {
        String[] files = {
                "Inter-Regular", "Inter-Medium", "Inter-SemiBold", "Inter-Bold",
                "JetBrainsMono-Regular", "JetBrainsMono-Medium"
        };
        for (String file : files) {
            InputStream in = getClass().getResourceAsStream("/fonts/" + file + ".ttf");
            if (in != null) Font.loadFont(in, 12);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}