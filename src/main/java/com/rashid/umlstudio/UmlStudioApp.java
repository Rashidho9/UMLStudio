package com.rashid.umlstudio;

import java.util.List;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.view.DiagramView;
import com.rashid.umlstudio.view.EigenschaftenPanel;
import com.rashid.umlstudio.view.Palette;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/** Hauptfenster mit Palette, Zeichenfläche und Eigenschaftsfenster. */
public class UmlStudioApp extends Application {

    public static final String TITEL = "UMLStudio";

    private static final List<String> VERBINDUNGEN = List.of(
            "association", "aggregation", "composition",
            "generalization", "realization", "dependency", "note_link");

    @Override
    public void start(Stage stage) {
        Diagram diagram = beispielDiagramm();
        DiagramView ansicht = new DiagramView(diagram);
        ansicht.setMinSize(1100, 750);
        ansicht.setPrefSize(1100, 750);

        EigenschaftenPanel eigenschaften =
                new EigenschaftenPanel(diagram, ansicht::zeichnen);
        Palette palette = new Palette(diagram, ansicht::auswaehlen);
        ansicht.setAuswahlListener(eigenschaften::anzeigen);

        ComboBox<String> art = new ComboBox<>();
        art.getItems().addAll(VERBINDUNGEN);
        art.setValue("association");
        art.setOnAction(e -> ansicht.setVerbindungsart(art.getValue()));

        ToggleButton verbinden = new ToggleButton("Verbinden");
        verbinden.setOnAction(
                e -> ansicht.setVerbindenModus(verbinden.isSelected()));

        HBox leiste = new HBox(10,
                new Label("Verbindungsart:"), art, verbinden,
                new Label("Ziehen = verschieben | Verbinden = Verbindung ziehen"));
        leiste.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(ansicht);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(false);

        BorderPane wurzel = new BorderPane();
        wurzel.setTop(leiste);
        wurzel.setLeft(palette);
        wurzel.setCenter(scroll);
        wurzel.setRight(eigenschaften);

        stage.setTitle(TITEL);
        stage.setScene(new Scene(wurzel, 1200, 700));
        stage.show();
        ansicht.zeichnen();
    }

    /** Erstellt das bekannte Beispieldiagramm. */
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