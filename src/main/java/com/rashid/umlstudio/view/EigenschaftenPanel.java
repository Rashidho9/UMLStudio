package com.rashid.umlstudio.view;

import com.rashid.umlstudio.controller.DocumentController;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.Node;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Zeigt die Eigenschaften des ausgewählten Elements an.
 * Änderungen laufen über den Controller, damit Undo und Redo möglich sind.
 *
 * GEÄNDERT Schritt 11: Bei Klassen, Schnittstellen und Aufzählungen gibt es
 * zwei Textbereiche für Attribute und Methoden (eine Zeile pro Eintrag).
 */
public class EigenschaftenPanel extends VBox {

    private final DocumentController controller;

    private final TextField nameFeld = new TextField();
    private final TextField xFeld = new TextField();
    private final TextField yFeld = new TextField();
    private final TextField breiteFeld = new TextField();
    private final TextField hoeheFeld = new TextField();

    // NEU Schritt 11
    private final TextArea attributFeld = new TextArea();
    private final TextArea methodenFeld = new TextArea();

    private final Label fehler = new Label();
    private final Button uebernehmen = new Button("Übernehmen");

    private Node aktuell;

    /**
     * Erstellt das Eigenschaftsfenster.
     *
     * @param controller Controller des bearbeiteten Diagramms
     */
    public EigenschaftenPanel(DocumentController controller) {
        this.controller = controller;

        setSpacing(8);
        setPadding(new Insets(10));
        setPrefWidth(260);

        Label titel = new Label("Eigenschaften");
        titel.setStyle("-fx-font-weight: bold;");

        GridPane gitter = new GridPane();
        gitter.setHgap(6);
        gitter.setVgap(6);

        zeile(gitter, 0, "Name", nameFeld);
        zeile(gitter, 1, "X", xFeld);
        zeile(gitter, 2, "Y", yFeld);
        zeile(gitter, 3, "Breite", breiteFeld);
        zeile(gitter, 4, "Höhe", hoeheFeld);

        textbereich(attributFeld, "- name: String");
        textbereich(methodenFeld, "+ getName(): String");

        fehler.setTextFill(Color.web("#c62828"));
        fehler.setWrapText(true);

        uebernehmen.setMaxWidth(Double.MAX_VALUE);
        uebernehmen.setOnAction(e -> uebernehmen());

        getChildren().addAll(titel, gitter,
                new Label("Attribute (eine Zeile pro Eintrag):"), attributFeld,
                new Label("Methoden (eine Zeile pro Eintrag):"), methodenFeld,
                uebernehmen, fehler);

        // Zu Beginn ist noch kein Element ausgewählt.
        anzeigen(null);
    }

    /** Fügt eine Beschriftung und das zugehörige Eingabefeld hinzu. */
    private static void zeile(GridPane gitter, int zeile,
            String text, TextField feld) {
        gitter.add(new Label(text + ":"), 0, zeile);
        feld.setPrefColumnCount(10);
        gitter.add(feld, 1, zeile);
    }

    /** NEU Schritt 11: richtet einen mehrzeiligen Eingabebereich ein. */
    private static void textbereich(TextArea feld, String beispiel) {
        feld.setPrefRowCount(4);
        feld.setWrapText(false);
        feld.setPromptText(beispiel);
    }

    /**
     * Zeigt die aktuellen Eigenschaften an, ohne das Modell zu verändern.
     * Ohne Auswahl werden die Felder geleert und deaktiviert.
     * Attribute und Methoden sind nur bei Klassenarten bearbeitbar.
     *
     * @param n ausgewähltes Element oder null
     */
    public void anzeigen(Node n) {
        aktuell = n;
        boolean an = n != null;
        boolean klasse = KlassenInhalt.unterstuetzt(n);

        nameFeld.setDisable(!an);
        xFeld.setDisable(!an);
        yFeld.setDisable(!an);
        breiteFeld.setDisable(!an);
        hoeheFeld.setDisable(!an);
        attributFeld.setDisable(!klasse);
        methodenFeld.setDisable(!klasse);
        uebernehmen.setDisable(!an);

        if (n == null) {
            nameFeld.clear();
            xFeld.clear();
            yFeld.clear();
            breiteFeld.clear();
            hoeheFeld.clear();
        } else {
            nameFeld.setText(n.getName());
            xFeld.setText(Double.toString(n.getX()));
            yFeld.setText(Double.toString(n.getY()));
            breiteFeld.setText(Double.toString(n.getW()));
            hoeheFeld.setText(Double.toString(n.getH()));
        }

        if (klasse) {
            attributFeld.setText(KlassenInhalt.alsText(KlassenInhalt.attribute(n)));
            methodenFeld.setText(KlassenInhalt.alsText(KlassenInhalt.methoden(n)));
        } else {
            attributFeld.clear();
            methodenFeld.clear();
        }

        fehler.setText("");
    }

    /** Übergibt die Eingaben zur Prüfung und Bearbeitung an den Controller. */
    private void uebernehmen() {
        if (aktuell == null) {
            return;
        }

        // Bei Paketen und Notizen bleiben Attribute und Methoden unberührt (null).
        boolean klasse = KlassenInhalt.unterstuetzt(aktuell);
        String meldung = controller.eigenschaftenUebernehmen(
                aktuell,
                nameFeld.getText(),
                xFeld.getText(),
                yFeld.getText(),
                breiteFeld.getText(),
                hoeheFeld.getText(),
                klasse ? attributFeld.getText() : null,
                klasse ? methodenFeld.getText() : null);

        if (meldung != null) {
            fehler.setText(meldung);
        }
    }
}