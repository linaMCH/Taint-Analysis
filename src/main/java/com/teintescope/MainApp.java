package com.teintescope;

import com.teintescope.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        loadFonts();

        Scene scene = new Scene(new MainView(), 1200, 700);
        scene.getStylesheets().add(
                getClass().getResource("/styles/teintescope.css").toExternalForm());

        stage.setTitle("TeinteScope");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    /** Charge les polices placées dans src/main/resources/fonts (facultatif). */
    private void loadFonts() {
        String[] files = {
                "Manrope-Regular", "Manrope-Medium", "Manrope-SemiBold", "Manrope-Bold",
                "DMMono-Regular", "DMMono-Medium"
        };
        for (String file : files) {
            InputStream in = getClass().getResourceAsStream("/fonts/" + file + ".ttf");
            if (in != null) {
                Font.loadFont(in, 12);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
