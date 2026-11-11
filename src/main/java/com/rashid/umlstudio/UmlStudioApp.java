package com.rashid.umlstudio;

import java.util.List;

import com.rashid.umlstudio.controller.DocumentController;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.view.DiagramView;
import com.rashid.umlstudio.view.EigenschaftenPanel;
import com.rashid.umlstudio.view.Palette;
import com.rashid.umlstudio.view.PruefDialog;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/** Hauptfenster mit Undo/Redo und struktureller Modellprüfung. */
public class UmlStudioApp extends Application {

    public static final String TITEL = "UMLStudio";

    private static final List<String> VERBINDUNGEN = List.of(
            "association", "aggregation", "composition",
            "generalization", "realization", "dependency", "note_link");

    @Override
    public void start(Stage stage) {
        Diagram diagram = beispielDiagramm();
        DocumentController controller = new DocumentController(diagram);
        DiagramView ansicht = new DiagramView(controller);
        ansicht.setMinSize(1100, 750);
        ansicht.setPrefSize(1100, 750);

        EigenschaftenPanel eigenschaften = new EigenschaftenPanel(controller);
        Palette palette = new Palette(controller);

        Button rueckgaengig = new Button("Rückgängig");
        Button wiederholen = new Button("Wiederholen");
        rueckgaengig.setOnAction(e -> controller.rueckgaengig());
        wiederholen.setOnAction(e -> controller.wiederholen());

        ComboBox<String> art = new ComboBox<>();
        art.getItems().addAll(VERBINDUNGEN);
        art.setValue("association");
        art.setOnAction(e -> ansicht.setVerbindungsart(art.getValue()));

        ToggleButton verbinden = new ToggleButton("Verbinden");
        verbinden.setOnAction(e -> ansicht.setVerbindenModus(verbinden.isSelected()));

        Button pruefen = new Button("Diagramm prüfen");
        pruefen.setOnAction(e -> PruefDialog.anzeigen(stage, controller.getDiagram()));

        HBox leiste = new HBox(10,
                rueckgaengig, wiederholen, pruefen,
                new Label("Verbindungsart:"), art, verbinden,
                new Label("Strg+Z / Strg+Y"));
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

        // Alle Ansichten werden zentral nach einer Änderung aktualisiert.
        controller.setNachAenderung(() -> {
            ansicht.aktualisieren();
            eigenschaften.anzeigen(controller.getAusgewaehlt().orElse(null));
            rueckgaengig.setDisable(!controller.kannRueckgaengig());
            wiederholen.setDisable(!controller.kannWiederholen());
        });
        rueckgaengig.setDisable(true);
        wiederholen.setDisable(true);

        Scene szene = new Scene(wurzel, 1200, 700);
        szene.addEventFilter(KeyEvent.KEY_PRESSED,
                e -> tastenkuerzel(e, controller));
        stage.setTitle(TITEL);
        stage.setScene(szene);
        stage.show();
        ansicht.zeichnen();
    }

    /** Bedient die Diagrammhistorie, auch wenn ein Textfeld aktiv ist. */
    private static void tastenkuerzel(KeyEvent e, DocumentController controller) {
        if (!e.isControlDown() || e.isAltDown() || e.isMetaDown()) {
            return;
        }
        if (e.getCode() == KeyCode.Z && e.isShiftDown()) {
            controller.wiederholen();
            e.consume();
        } else if (e.getCode() == KeyCode.Z) {
            controller.rueckgaengig();
            e.consume();
        } else if (e.getCode() == KeyCode.Y) {
            controller.wiederholen();
            e.consume();
        }
    }

    /** Erstellt das bekannte Startbeispiel außerhalb der Undo-Historie. */
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