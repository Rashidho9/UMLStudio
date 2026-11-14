package com.rashid.umlstudio.controller;

import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.KlassenLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.rashid.umlstudio.command.Command;
import com.rashid.umlstudio.command.CommandHistory;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.ElementBearbeitung;
import com.rashid.umlstudio.model.ElementStand;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/**
 * Vermittelt zwischen Oberfläche und Modell, ohne JavaFX zu benötigen.
 * Änderungen aus der Oberfläche werden hier als Befehle gespeichert.
 */
public class DocumentController {

    private final Diagram diagram;
    private final CommandHistory history = new CommandHistory();
    private Runnable nachAenderung;
    private String ausgewaehltId;

    private Node zugElement;
    private List<ElementStand> zugStart;
    private boolean zugAktiv;

    /** NEU Schritt 9: zählt jede echte Änderung (auch Undo/Redo), nicht das Auswählen. */
    private long version;

    public DocumentController(Diagram diagram) {
        this.diagram = diagram;
    }

    public Diagram getDiagram() {
        return diagram;
    }

    public CommandHistory getHistory() {
        return history;
    }

    /** NEU Schritt 9: aktuelle Änderungsnummer zum Erkennen ungespeicherter Änderungen. */
    public long getVersion() {
        return version;
    }

    /** Meldet Änderungen an die Oberfläche, auch während einer Ziehvorschau. */
    public void setNachAenderung(Runnable nachAenderung) {
        this.nachAenderung = nachAenderung;
    }

    public Optional<Node> getAusgewaehlt() {
        return ausgewaehltId == null
                ? Optional.empty() : diagram.node(ausgewaehltId);
    }

    public void auswaehlen(Node n) {
        ausgewaehltId = n == null ? null : n.getId();
        benachrichtigen();
    }

    /** Während des Ziehens ist Undo gesperrt. */
    public boolean kannRueckgaengig() {
        return !zugAktiv && history.kannRueckgaengig();
    }

    /** Während des Ziehens ist Redo gesperrt. */
    public boolean kannWiederholen() {
        return !zugAktiv && history.kannWiederholen();
    }

    public void rueckgaengig() {
        if (!kannRueckgaengig()) {
            return;
        }
        history.rueckgaengig();
        version++; // NEU Schritt 9
        if (ausgewaehltId != null && diagram.node(ausgewaehltId).isEmpty()) {
            ausgewaehltId = null;
        }
        benachrichtigen();
    }

    public void wiederholen() {
        if (!kannWiederholen()) {
            return;
        }
        history.wiederholen();
        version++; // NEU Schritt 9
        benachrichtigen();
    }

    /** Fügt ein Element hinzu; Redo verwendet dasselbe Objekt mit derselben ID. */
    public Node elementHinzufuegen(String nodeKindId, String name, double x, double y) {
        zugZuerstBeenden();
        Node neu = Catalog.neuesElement(diagram.getType(), nodeKindId, name, x, y);
        Command befehl = new Command("Element hinzufügen",
                () -> {
                    if (!diagram.getNodes().contains(neu)) {
                        diagram.addNode(neu);
                    }
                    ausgewaehltId = neu.getId();
                },
                () -> {
                    diagram.getNodes().remove(neu);
                    ausgewaehltId = null;
                });
        history.ausfuehren(befehl);
        version++; // NEU Schritt 9
        benachrichtigen();
        return neu;
    }

    /** Verbindet vorhandene, unterschiedliche Elemente. */
    public boolean verbindungHinzufuegen(String art, Node von, Node nach) {
        zugZuerstBeenden();
        if (von == null || nach == null || von == nach) {
            return false;
        }
        if (diagram.node(von.getId()).isEmpty()
                || diagram.node(nach.getId()).isEmpty()) {
            return false;
        }
        Edge kante = new Edge(art, von.getId(), nach.getId());
        Command befehl = new Command("Verbindung hinzufügen",
                () -> {
                    if (!diagram.getEdges().contains(kante)) {
                        diagram.addEdge(kante);
                    }
                },
                () -> diagram.getEdges().remove(kante));
        history.ausfuehren(befehl);
        version++; // NEU Schritt 9
        benachrichtigen();
        return true;
    }

    /**
     * Übernimmt geprüfte Eigenschaften und merkt Vorher-/Nachher-Zustände.
     * Ungültige oder unveränderte Eingaben erzeugen keinen neuen Befehl.
     * Attribute und Methoden bleiben dabei unverändert.
     *
     * @return Fehlermeldung oder null bei Erfolg
     */
    public String eigenschaftenUebernehmen(Node element,
            String name, String xText, String yText,
            String breiteText, String hoeheText) {
        return eigenschaftenUebernehmen(element, name, xText, yText,
                breiteText, hoeheText, null, null);
    }

    /**
     * NEU Schritt 11: Übernimmt Name, Position, Größe, Attribute und Methoden
     * als einen einzigen Undo-Schritt. Bei Klassen, Schnittstellen und
     * Aufzählungen wächst der Kasten automatisch, wenn der Inhalt nicht passt.
     *
     * @param attributeText eine Zeile pro Attribut; null = unverändert lassen
     * @param methodenText  eine Zeile pro Methode; null = unverändert lassen
     * @return Fehlermeldung oder null bei Erfolg
     */
    public String eigenschaftenUebernehmen(Node element,
            String name, String xText, String yText,
            String breiteText, String hoeheText,
            String attributeText, String methodenText) {
        if (element == null) {
            return "Kein Element ausgewählt.";
        }
        zugZuerstBeenden();

        // Erst alles prüfen, dann ändern: so bleibt das Modell bei Fehlern unberührt.
        boolean klasse = KlassenInhalt.unterstuetzt(element);
        List<String> attribute = attributeText == null
                ? KlassenInhalt.attribute(element) : KlassenInhalt.zeilen(attributeText);
        List<String> methoden = methodenText == null
                ? KlassenInhalt.methoden(element) : KlassenInhalt.zeilen(methodenText);
        if (klasse) {
            String inhaltFehler = KlassenInhalt.pruefe(attribute, methoden);
            if (inhaltFehler != null) {
                return inhaltFehler;
            }
        }

        List<ElementStand> vorher = standVon(element);
        String fehler = ElementBearbeitung.anwenden(
                diagram, element, name, xText, yText, breiteText, hoeheText);
        if (fehler != null) {
            return fehler;
        }
        if (klasse) {
            KlassenInhalt.setzen(element, attribute, methoden);
            KlassenLayout.groesseAnpassen(element);
        }
        List<ElementStand> nachher = standVon(element);
        if (vorher.equals(nachher)) {
            return null;
        }
        String id = element.getId();
        Command befehl = new Command("Eigenschaften ändern",
                () -> {
                    stellenHer(nachher);
                    ausgewaehltId = id;
                },
                () -> {
                    stellenHer(vorher);
                    ausgewaehltId = id;
                });
        history.aufnehmen(befehl);
        version++; // NEU Schritt 9
        ausgewaehltId = id;
        benachrichtigen();
        return null;
    }
    

    /** Speichert den Ausgangszustand von Element und Nachkommen. */
    public void zugBeginnen(Node element) {
        if (element == null) {
            return;
        }
        zugZuerstBeenden();
        zugElement = element;
        zugStart = standVon(element);
        zugAktiv = true;
        ausgewaehltId = element.getId();
        benachrichtigen();
    }

    /**
     * Zeigt die Verschiebung relativ zum Startpunkt an.
     * Noch kein Eintrag in der Befehlshistorie.
     */
    public void zugAktualisieren(double dx, double dy) {
        if (!zugAktiv || zugElement == null || zugStart == null) {
            return;
        }
        stellenHer(zugStart);
        ElementBearbeitung.verschiebenMitNachkommen(diagram, zugElement, dx, dy);
        benachrichtigen();
    }

    /** Fasst den gesamten Mauszug zu einem Undo-Schritt zusammen. */
    public void zugAbschliessen() {
        if (!zugAktiv || zugElement == null || zugStart == null) {
            zugAktiv = false;
            return;
        }
        List<ElementStand> ende = standVon(zugElement);
        List<ElementStand> start = zugStart;
        String id = zugElement.getId();
        zugAktiv = false;
        zugElement = null;
        zugStart = null;
        if (start.equals(ende)) {
            benachrichtigen();
            return;
        }
        Command befehl = new Command("Verschieben",
                () -> {
                    stellenHer(ende);
                    ausgewaehltId = id;
                },
                () -> {
                    stellenHer(start);
                    ausgewaehltId = id;
                });
        history.aufnehmen(befehl);
        version++; // NEU Schritt 9
        benachrichtigen();
    }

    private void zugZuerstBeenden() {
        if (zugAktiv) {
            zugAbschliessen();
        }
    }

    /** Erstellt Momentaufnahmen des Elements und aller Nachkommen. */
    private List<ElementStand> standVon(Node element) {
        List<ElementStand> liste = new ArrayList<>();
        liste.add(new ElementStand(element));
        for (String id : diagram.descendants(element.getId())) {
            diagram.node(id).ifPresent(n -> liste.add(new ElementStand(n)));
        }
        return liste;
    }

    private void stellenHer(List<ElementStand> staende) {
        for (ElementStand stand : staende) {
            stand.wiederherstellen(diagram);
        }
    }

    private void benachrichtigen() {
        if (nachAenderung != null) {
            nachAenderung.run();
        }
    }
}