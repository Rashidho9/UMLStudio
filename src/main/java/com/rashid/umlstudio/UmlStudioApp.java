package com.rashid.umlstudio;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.rashid.umlstudio.controller.DateiZustand;
import com.rashid.umlstudio.controller.DocumentController;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.Project;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.persistence.Autosave;
import com.rashid.umlstudio.persistence.ProjectStore;
import com.rashid.umlstudio.persistence.ProjektDateiException;
import com.rashid.umlstudio.view.DiagramView;
import com.rashid.umlstudio.view.EigenschaftenPanel;
import com.rashid.umlstudio.view.Palette;
import com.rashid.umlstudio.view.PruefDialog;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Hauptfenster mit Datei-Menü, Autosave, Undo/Redo und Modellprüfung. */
public class UmlStudioApp extends Application {

    public static final String TITEL = "UMLStudio";

    /** Abstand der automatischen Sicherung in Sekunden. */
    private static final int AUTOSAVE_SEKUNDEN = 30;

    private static final List<String> VERBINDUNGEN = List.of(
            "association", "aggregation", "composition",
            "generalization", "realization", "dependency", "note_link");

    private final BorderPane wurzel = new BorderPane();
    private final DateiZustand zustand = new DateiZustand();
    private final Autosave autosave = Autosave.standard();

    private Stage stage;
    private Project projekt;
    private DocumentController controller;
    private DiagramView ansicht;
    private long autosaveVersion;
    private Timeline autosaveTimer;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        wurzel.setTop(menue());

        // 1. Gibt es eine Sicherung vom letzten (abgestürzten) Lauf?
        boolean wiederhergestellt = false;
        Project start = null;
        if (autosave.vorhanden()) {
            if (wiederherstellenFragen()) {
                try {
                    start = autosave.laden();
                    wiederhergestellt = true;
                } catch (ProjektDateiException ex) {
                    fehler("Wiederherstellen fehlgeschlagen", ex.getMessage());
                }
            } else {
                autosave.loeschen();
            }
        }
        if (start == null) {
            start = beispielProjekt();
        }
        projektAnzeigen(start, null);
        if (wiederhergestellt) {
            // Inhalt stammt aus der Sicherung und ist noch in keiner Datei.
            zustand.alsUngespeichertMarkieren();
            titelAktualisieren();
        }

        // 2. Fenster aufbauen
        Scene szene = new Scene(wurzel, 1250, 750);
        szene.addEventFilter(KeyEvent.KEY_PRESSED, this::tastenkuerzel);
        stage.setScene(szene);
        stage.setOnCloseRequest(e -> {
            if (aenderungenKlaeren()) {
                aufraeumen();
            } else {
                e.consume(); // Schließen abbrechen
            }
        });
        stage.show();
        ansicht.zeichnen();

        // 3. Autosave-Timer starten
        autosaveTimer = new Timeline(new KeyFrame(
                Duration.seconds(AUTOSAVE_SEKUNDEN), e -> autoSichern()));
        autosaveTimer.setCycleCount(Animation.INDEFINITE);
        autosaveTimer.play();
    }

    // ------------------------------------------------------------ Aufbau

    /** Baut den Arbeitsbereich mit einem neuen Controller für das Projekt auf. */
    private void projektAnzeigen(Project p, Path datei) {
        if (p.getDiagrams().isEmpty()) {
            p.addDiagram(Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Klassendiagramm"));
        }
        projekt = p;
        // Neuer Controller = leere Undo-Historie, passend zum geladenen Projekt.
        controller = new DocumentController(p.getDiagrams().get(0));

        ansicht = new DiagramView(controller);
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

        BorderPane arbeitsbereich = new BorderPane();
        arbeitsbereich.setTop(leiste);
        arbeitsbereich.setLeft(palette);
        arbeitsbereich.setCenter(scroll);
        arbeitsbereich.setRight(eigenschaften);

        // Alle Ansichten werden zentral nach einer Änderung aktualisiert.
        controller.setNachAenderung(() -> {
            ansicht.aktualisieren();
            eigenschaften.anzeigen(controller.getAusgewaehlt().orElse(null));
            rueckgaengig.setDisable(!controller.kannRueckgaengig());
            wiederholen.setDisable(!controller.kannWiederholen());
            titelAktualisieren();
        });
        rueckgaengig.setDisable(true);
        wiederholen.setDisable(true);

        wurzel.setCenter(arbeitsbereich);

        if (datei == null) {
            zustand.neu(controller.getVersion());
        } else {
            zustand.gespeichert(datei, controller.getVersion());
        }
        autosaveVersion = controller.getVersion();
        titelAktualisieren();
        ansicht.zeichnen();
    }

    /** Datei-Menü mit Tastenkürzeln. */
    private MenuBar menue() {
        MenuItem neu = new MenuItem("Neu");
        neu.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        neu.setOnAction(e -> neu());

        MenuItem oeffnen = new MenuItem("Öffnen …");
        oeffnen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        oeffnen.setOnAction(e -> oeffnen());

        MenuItem speichern = new MenuItem("Speichern");
        speichern.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        speichern.setOnAction(e -> speichern());

        MenuItem speichernUnter = new MenuItem("Speichern unter …");
        speichernUnter.setAccelerator(new KeyCodeCombination(KeyCode.S,
                KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        speichernUnter.setOnAction(e -> speichernUnter());

        MenuItem beenden = new MenuItem("Beenden");
        beenden.setOnAction(e -> beenden());

        Menu datei = new Menu("Datei");
        datei.getItems().addAll(neu, oeffnen, new SeparatorMenuItem(),
                speichern, speichernUnter, new SeparatorMenuItem(), beenden);
        return new MenuBar(datei);
    }

    // ------------------------------------------------------------ Datei-Aktionen

    private void neu() {
        if (!aenderungenKlaeren()) {
            return;
        }
        projektAnzeigen(leeresProjekt(), null);
        autosave.loeschen();
    }

    private void oeffnen() {
        if (!aenderungenKlaeren()) {
            return;
        }
        File f = dateiauswahl("Projekt öffnen").showOpenDialog(stage);
        if (f == null) {
            return; // abgebrochen
        }
        try {
            Project geladen = ProjectStore.laden(f.toPath());
            projektAnzeigen(geladen, f.toPath());
            autosave.loeschen();
        } catch (ProjektDateiException ex) {
            fehler("Öffnen fehlgeschlagen", ex.getMessage());
        }
    }

    /** @return true, wenn gespeichert wurde */
    private boolean speichern() {
        Optional<Path> datei = zustand.getDatei();
        return datei.isPresent() ? schreiben(datei.get()) : speichernUnter();
    }

    /** @return true, wenn gespeichert wurde */
    private boolean speichernUnter() {
        FileChooser auswahl = dateiauswahl("Projekt speichern unter");
        auswahl.setInitialFileName(projekt.getName() + ProjectStore.ENDUNG);
        File f = auswahl.showSaveDialog(stage);
        return f != null && schreiben(f.toPath());
    }

    private boolean schreiben(Path ziel) {
        try {
            Path gespeichert = ProjectStore.speichern(projekt, ziel);
            zustand.gespeichert(gespeichert, controller.getVersion());
            autosaveVersion = controller.getVersion();
            autosave.loeschen(); // Datei ist jetzt sicher gespeichert
            titelAktualisieren();
            return true;
        } catch (ProjektDateiException ex) {
            fehler("Speichern fehlgeschlagen", ex.getMessage());
            return false;
        }
    }

    private void beenden() {
        if (aenderungenKlaeren()) {
            aufraeumen();
            Platform.exit();
        }
    }

    /**
     * Fragt bei ungespeicherten Änderungen nach.
     *
     * @return true, wenn weitergemacht werden darf
     */
    private boolean aenderungenKlaeren() {
        if (!zustand.istGeaendert(controller.getVersion())) {
            return true;
        }
        ButtonType ja = new ButtonType("Speichern");
        ButtonType nein = new ButtonType("Nicht speichern");
        ButtonType abbrechen = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert frage = new Alert(Alert.AlertType.CONFIRMATION,
                "Möchtest du die Änderungen speichern?", ja, nein, abbrechen);
        frage.initOwner(stage);
        frage.setTitle(TITEL);
        frage.setHeaderText("Ungespeicherte Änderungen");
        Optional<ButtonType> wahl = frage.showAndWait();
        if (wahl.isEmpty() || wahl.get() == abbrechen) {
            return false;
        }
        if (wahl.get() == ja) {
            return speichern();
        }
        return true; // Nicht speichern
    }

    // ------------------------------------------------------------ Autosave

    /** Sichert nur, wenn es seit der letzten Sicherung Änderungen gab. */
    private void autoSichern() {
        long v = controller.getVersion();
        if (v == autosaveVersion || !zustand.istGeaendert(v)) {
            return;
        }
        try {
            autosave.speichern(projekt);
            autosaveVersion = v;
        } catch (ProjektDateiException ex) {
            System.err.println("Autosave fehlgeschlagen: " + ex.getMessage());
        }
    }

    private boolean wiederherstellenFragen() {
        ButtonType ja = new ButtonType("Wiederherstellen");
        ButtonType nein = new ButtonType("Verwerfen", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert frage = new Alert(Alert.AlertType.CONFIRMATION,
                "Beim letzten Mal wurde UMLStudio nicht normal beendet.\n"
                        + "Soll die automatische Sicherung wiederhergestellt werden?",
                ja, nein);
        frage.setTitle(TITEL);
        frage.setHeaderText("Automatische Sicherung gefunden");
        return frage.showAndWait().orElse(nein) == ja;
    }

    /** Normales Ende: Timer stoppen, Sicherung entfernen. */
    private void aufraeumen() {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
        }
        autosave.loeschen();
    }

    // ------------------------------------------------------------ Hilfen

    private void titelAktualisieren() {
        if (stage != null && controller != null) {
            stage.setTitle(zustand.titel(TITEL, controller.getVersion()));
        }
    }

    private FileChooser dateiauswahl(String titel) {
        FileChooser auswahl = new FileChooser();
        auswahl.setTitle(titel);
        auswahl.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "UMLStudio-Projekt (*" + ProjectStore.ENDUNG + ")", "*" + ProjectStore.ENDUNG));
        zustand.getDatei()
                .map(p -> p.toAbsolutePath().getParent())
                .map(Path::toFile)
                .filter(File::isDirectory)
                .ifPresent(auswahl::setInitialDirectory);
        return auswahl;
    }

    private void fehler(String kopf, String text) {
        Alert a = new Alert(Alert.AlertType.ERROR, text);
        if (stage != null && stage.isShowing()) {
            a.initOwner(stage);
        }
        a.setTitle(TITEL);
        a.setHeaderText(kopf);
        a.showAndWait();
    }

    /** Bedient die Diagrammhistorie, auch wenn ein Textfeld aktiv ist. */
    private void tastenkuerzel(KeyEvent e) {
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

    private Project leeresProjekt() {
        Project p = new Project("Neues Projekt");
        p.addDiagram(Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Klassendiagramm"));
        return p;
    }

    private Project beispielProjekt() {
        Project p = new Project("Beispielprojekt");
        p.addDiagram(beispielDiagramm());
        return p;
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