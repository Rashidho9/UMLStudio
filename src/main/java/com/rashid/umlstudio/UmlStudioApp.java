package com.rashid.umlstudio;

import java.util.List;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.view.DiagramView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Hauptfenster von UMLStudio: oben Werkzeugleiste, in der Mitte die Zeichenfläche.
 */
public class UmlStudioApp extends Application {

    public static final String TITEL = "UMLStudio";

    /** Verbindungsarten des Klassendiagramms (Palette kommt in Schritt 6). */
    private static final List<String> VERBINDUNGEN = List.of(
            "association", "aggregation", "composition",
            "generalization", "realization", "dependency", "note_link");

    @Override
    public void start(Stage stage) {
        Diagram diagram = beispielDiagramm();
        DiagramView ansicht = new DiagramView(diagram);

        ComboBox<String> art = new ComboBox<>();
        art.getItems().addAll(VERBINDUNGEN);
        art.setValue("association");
        art.setOnAction(e -> ansicht.setVerbindungsart(art.getValue()));

        ToggleButton verbinden = new ToggleButton("Verbinden");
        verbinden.setOnAction(e -> ansicht.setVerbindenModus(verbinden.isSelected()));

        HBox leiste = new HBox(10,
                new Label("Verbindungsart:"), art, verbinden,
                new Label("Ziehen = verschieben   |   Rechtsklick-Ziehen oder \"Verbinden\" = verbinden"));

        BorderPane wurzel = new BorderPane(ansicht);
        wurzel.setTop(leiste);

        stage.setTitle(TITEL);
        stage.setScene(new Scene(wurzel, 1000, 700));
        stage.show();
        ansicht.zeichnen();
    }

    /** Kleines Beispiel, damit sofort etwas zu sehen ist. */
    private Diagram beispielDiagramm() {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Beispiel");
        Node paket = Catalog.neuesElement("class", "package", "model", 40, 40);
        Node person = Catalog.neuesElement("class", "class", "Person", 70, 90);
        Node kunde = Catalog.neuesElement("class", "class", "Kunde", 450, 60);
        Node zahlbar = Catalog.neuesElement("class", "interface", "Zahlbar", 450, 300);
        Node status = Catalog.neuesElement("class", "enum", "Status", 750, 300);
        Node notiz = Catalog.neuesElement("class", "note", "Beispieldiagramm", 750, 60);
        person.setParentId(paket.getId());

        for (Node n : List.of(paket, person, kunde, zahlbar, status, notiz)) {
            d.addNode(n);
        }
        d.addEdge(new Edge("generalization", kunde.getId(), person.getId()));
        d.addEdge(new Edge("realization", kunde.getId(), zahlbar.getId()));
        d.addEdge(new Edge("dependency", zahlbar.getId(), status.getId()));
        d.addEdge(new Edge("note_link", notiz.getId(), kunde.getId()));
        return d;
    }
}