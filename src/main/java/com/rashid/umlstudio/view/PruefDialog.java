package com.rashid.umlstudio.view;

import java.util.List;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.validation.DiagramPruefer;
import com.rashid.umlstudio.model.validation.Pruefmeldung;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

/** Zeigt die Befunde der Modellprüfung an. */
public final class PruefDialog {

    private PruefDialog() {
    }

    /** Prüft das Diagramm und zeigt das Ergebnis, ohne das Modell zu ändern. */
    public static void anzeigen(Window eigentuemer, Diagram diagram) {
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(diagram);
        Alert dialog = new Alert(
                funde.isEmpty() ? AlertType.INFORMATION : AlertType.WARNING);
        dialog.initOwner(eigentuemer);
        dialog.setTitle("Modellprüfung");
        dialog.setHeaderText(funde.isEmpty()
                ? "Keine Probleme im geprüften Umfang gefunden."
                : funde.size() + " Beanstandung(en) gefunden.");

        TextArea bereich = new TextArea(text(funde));
        bereich.setEditable(false);
        bereich.setWrapText(true);
        bereich.setPrefWidth(540);
        bereich.setPrefHeight(260);
        dialog.getDialogPane().setContent(bereich);
        dialog.showAndWait();
    }

    private static String text(List<Pruefmeldung> funde) {
        if (funde.isEmpty()) {
            return "Die Strukturprüfung hat keine Probleme erkannt. "
                    + "Eine vollständige Prüfung aller UML-Regeln ist dies noch nicht.";
        }
        StringBuilder sb = new StringBuilder();
        for (Pruefmeldung m : funde) {
            sb.append(m.code());
            if (m.elementId() != null && !m.elementId().isBlank()) {
                sb.append(" [").append(m.elementId()).append(']');
            }
            sb.append(": ").append(m.text()).append('\n');
        }
        return sb.toString();
    }
}