package com.rashid.umlstudio;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX-Anwendung von UMLStudio.
 * Erzeugt das Hauptfenster. In späteren Schritten kommen hier Menü,
 * Palette, Zeichenfläche und Eigenschaftsfenster dazu.
 */
public class UmlStudioApp extends Application {

    /** Fenstertitel, der überall gleich verwendet wird. */
    public static final String TITEL = "UMLStudio";

    /**
     * Wird von JavaFX aufgerufen, sobald die Oberfläche bereit ist.
     *
     * @param fenster das Hauptfenster, das JavaFX bereitstellt
     */
    @Override
    public void start(Stage fenster) {
        BorderPane wurzel = new BorderPane();
        wurzel.setCenter(new Label("UMLStudio gestartet – hier entsteht die Zeichenfläche."));

        Scene szene = new Scene(wurzel, 1000, 700);
        fenster.setTitle(TITEL);
        fenster.setScene(szene);
        fenster.show();
    }
}