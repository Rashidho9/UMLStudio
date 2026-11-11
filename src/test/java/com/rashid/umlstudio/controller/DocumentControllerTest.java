package com.rashid.umlstudio.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Prüft Befehle, Auswahl und gruppierte Mausbewegungen ohne JavaFX. */
class DocumentControllerTest {

    private DocumentController controller;
    private Diagram diagram;

    @BeforeEach
    void aufbauen() {
        diagram = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D");
        controller = new DocumentController(diagram);
    }

    @Test
    void hinzufuegenUndoRedoBewahrtId() {
        Node neu = controller.elementHinzufuegen("class", "Kunde", 10, 20);
        String id = neu.getId();
        assertEquals(1, diagram.getNodes().size());
        assertEquals(id, controller.getAusgewaehlt().orElseThrow().getId());
        controller.rueckgaengig();
        assertTrue(diagram.getNodes().isEmpty());
        assertTrue(controller.getAusgewaehlt().isEmpty());
        controller.wiederholen();
        assertSame(neu, diagram.getNodes().get(0));
        assertEquals(id, controller.getAusgewaehlt().orElseThrow().getId());
    }

    @Test
    void verbindungUndoRedoBewahrtDieselbeKante() {
        Node a = controller.elementHinzufuegen("class", "A", 0, 0);
        Node b = controller.elementHinzufuegen("class", "B", 200, 0);
        assertTrue(controller.verbindungHinzufuegen("association", a, b));
        Edge kante = diagram.getEdges().get(0);
        controller.rueckgaengig();
        assertTrue(diagram.getEdges().isEmpty());
        controller.wiederholen();
        assertEquals(1, diagram.getEdges().size());
        assertSame(kante, diagram.getEdges().get(0));
    }

    @Test
    void verbindungOhneZielErzeugtKeinenBefehl() {
        Node a = controller.elementHinzufuegen("class", "A", 0, 0);
        int undo = controller.getHistory().undoAnzahl();
        assertFalse(controller.verbindungHinzufuegen("association", a, a));
        assertFalse(controller.verbindungHinzufuegen("association", a, null));
        assertEquals(undo, controller.getHistory().undoAnzahl());
        assertTrue(diagram.getEdges().isEmpty());
    }

    @Test
    void eigenschaftenAendernUndoRedo() {
        Node n = controller.elementHinzufuegen("class", "A", 10, 20);
        assertNull(controller.eigenschaftenUebernehmen(
                n, "B", "80", "90", "200", "120"));
        assertEquals("B", n.getName());
        assertEquals(80, n.getX(), 0.001);
        controller.rueckgaengig();
        assertEquals("A", n.getName());
        assertEquals(10, n.getX(), 0.001);
        assertEquals(20, n.getY(), 0.001);
        controller.wiederholen();
        assertEquals("B", n.getName());
        assertEquals(200, n.getW(), 0.001);
        assertEquals(120, n.getH(), 0.001);
    }

    @Test
    void ungueltigeBearbeitungErhaeltHistorie() {
        Node n = controller.elementHinzufuegen("class", "A", 10, 20);
        assertNull(controller.eigenschaftenUebernehmen(
                n, "B", "30", "40", "160", "100"));
        controller.rueckgaengig();
        int redo = controller.getHistory().redoAnzahl();
        assertEquals("X ist keine gültige Zahl.",
                controller.eigenschaftenUebernehmen(
                        n, "A", "abc", "40", "160", "100"));
        assertEquals("A", n.getName());
        assertEquals(10, n.getX(), 0.001);
        assertEquals(redo, controller.getHistory().redoAnzahl());
        assertTrue(controller.kannWiederholen());
    }

    @Test
    void unveraenderteEigenschaftenErhaltenRedo() {
        Node n = controller.elementHinzufuegen("class", "A", 10, 20);
        assertNull(controller.eigenschaftenUebernehmen(
                n, "B", "10", "20", "160", "100"));
        controller.rueckgaengig();
        assertNull(controller.eigenschaftenUebernehmen(
                n, "A", "10", "20", "160", "100"));
        assertTrue(controller.kannWiederholen());
        assertEquals(1, controller.getHistory().redoAnzahl());
    }

    @Test
    void paketZiehenIstEinBefehlMitNachkommen() {
        Node paket = controller.elementHinzufuegen("package", "p", 0, 0);
        Node kind = controller.elementHinzufuegen("class", "K", 20, 40);
        kind.setParentId(paket.getId());
        int vorher = controller.getHistory().undoAnzahl();
        controller.zugBeginnen(paket);
        controller.zugAktualisieren(40, 10);
        controller.zugAktualisieren(100, 50);
        controller.zugAbschliessen();
        assertEquals(vorher + 1, controller.getHistory().undoAnzahl());
        assertEquals(100, paket.getX(), 0.001);
        assertEquals(50, paket.getY(), 0.001);
        assertEquals(120, kind.getX(), 0.001);
        assertEquals(90, kind.getY(), 0.001);
        controller.rueckgaengig();
        assertEquals(0, paket.getX(), 0.001);
        assertEquals(20, kind.getX(), 0.001);
        assertEquals(40, kind.getY(), 0.001);
        controller.wiederholen();
        assertEquals(100, paket.getX(), 0.001);
        assertEquals(120, kind.getX(), 0.001);
        assertEquals(90, kind.getY(), 0.001);
    }

    @Test
    void zugOhneBewegungErhaeltRedo() {
        Node n = controller.elementHinzufuegen("class", "A", 10, 20);
        assertNull(controller.eigenschaftenUebernehmen(
                n, "B", "10", "20", "160", "100"));
        controller.rueckgaengig();
        controller.zugBeginnen(n);
        controller.zugAktualisieren(0, 0);
        controller.zugAbschliessen();
        assertEquals(10, n.getX(), 0.001);
        assertEquals(20, n.getY(), 0.001);
        assertTrue(controller.kannWiederholen());
    }

    @Test
    void neuerBefehlLoeschtRedoZweig() {
        Node n = controller.elementHinzufuegen("class", "A", 10, 20);
        assertNull(controller.eigenschaftenUebernehmen(
                n, "B", "10", "20", "160", "100"));
        controller.rueckgaengig();
        assertTrue(controller.kannWiederholen());
        assertNull(controller.eigenschaftenUebernehmen(
                n, "C", "10", "20", "160", "100"));
        assertFalse(controller.kannWiederholen());
        controller.rueckgaengig();
        assertEquals("A", n.getName());
        controller.wiederholen();
        assertEquals("C", n.getName());
    }

    @Test
    void zweiZuegeErgebenZweiEintraege() {
        Node n = controller.elementHinzufuegen("class", "A", 0, 0);
        int start = controller.getHistory().undoAnzahl();
        controller.zugBeginnen(n);
        controller.zugAktualisieren(10, 0);
        controller.zugAbschliessen();
        controller.zugBeginnen(n);
        controller.zugAktualisieren(5, 8);
        controller.zugAbschliessen();
        assertEquals(start + 2, controller.getHistory().undoAnzahl());
        assertEquals(15, n.getX(), 0.001);
        assertEquals(8, n.getY(), 0.001);
        controller.rueckgaengig();
        assertEquals(10, n.getX(), 0.001);
        assertEquals(0, n.getY(), 0.001);
    }

    @Test
    void kindAlleinZiehenLaesstElternUnveraendert() {
        Node paket = controller.elementHinzufuegen("package", "p", 0, 0);
        Node kind = controller.elementHinzufuegen("class", "K", 20, 40);
        kind.setParentId(paket.getId());
        controller.zugBeginnen(kind);
        controller.zugAktualisieren(30, 10);
        controller.zugAbschliessen();
        assertEquals(50, kind.getX(), 0.001);
        assertEquals(50, kind.getY(), 0.001);
        assertEquals(0, paket.getX(), 0.001);
        assertEquals(0, paket.getY(), 0.001);
    }

    @Test
    void undoRedoWaehrendZugBlockiert() {
        Node n = controller.elementHinzufuegen("class", "A", 10, 20);
        assertNull(controller.eigenschaftenUebernehmen(
                n, "B", "10", "20", "160", "100"));
        controller.rueckgaengig();
        int undo = controller.getHistory().undoAnzahl();
        int redo = controller.getHistory().redoAnzahl();
        controller.zugBeginnen(n);
        assertFalse(controller.kannRueckgaengig());
        assertFalse(controller.kannWiederholen());
        controller.zugAktualisieren(30, 0);
        controller.rueckgaengig();
        controller.wiederholen();
        assertEquals(40, n.getX(), 0.001);
        assertEquals("A", n.getName());
        assertEquals(undo, controller.getHistory().undoAnzahl());
        assertEquals(redo, controller.getHistory().redoAnzahl());
        controller.zugAbschliessen();
        assertTrue(controller.kannRueckgaengig());
        controller.rueckgaengig();
        assertEquals(10, n.getX(), 0.001);
    }
}