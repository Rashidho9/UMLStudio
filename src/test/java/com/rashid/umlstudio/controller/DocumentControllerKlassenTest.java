package com.rashid.umlstudio.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** NEU Schritt 11: Attribute und Methoden über den Controller mit Undo/Redo. */
class DocumentControllerKlassenTest {

    private Diagram diagram;
    private DocumentController controller;

    @BeforeEach
    void aufbauen() {
        diagram = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D");
        controller = new DocumentController(diagram);
    }

    /** Übernimmt nur neuen Inhalt; Name, Position und Größe bleiben gleich. */
    private String uebernehmen(Node n, String attribute, String methoden) {
        return controller.eigenschaftenUebernehmen(n, n.getName(),
                Double.toString(n.getX()), Double.toString(n.getY()),
                Double.toString(n.getW()), Double.toString(n.getH()),
                attribute, methoden);
    }

    @Test
    void inhaltUebernehmenIstEinUndoSchritt() {
        Node n = controller.elementHinzufuegen("class", "Kunde", 10, 20);
        int undo = controller.getHistory().undoAnzahl();
        long version = controller.getVersion();

        assertNull(uebernehmen(n, "- name: String\n- alter: int", "+ getName(): String"));
        assertEquals(List.of("- name: String", "- alter: int"), KlassenInhalt.attribute(n));
        assertEquals(undo + 1, controller.getHistory().undoAnzahl());
        assertEquals(version + 1, controller.getVersion());

        controller.rueckgaengig();
        assertEquals(List.of(), KlassenInhalt.attribute(n));
        assertEquals(List.of(), KlassenInhalt.methoden(n));

        controller.wiederholen();
        assertEquals(List.of("+ getName(): String"), KlassenInhalt.methoden(n));
    }

    @Test
    void ungueltigeZeileAendertNichts() {
        Node n = controller.elementHinzufuegen("class", "Kunde", 10, 20);
        int undo = controller.getHistory().undoAnzahl();
        long version = controller.getVersion();

        String fehler = controller.eigenschaftenUebernehmen(
                n, "Neu", "99", "20", "160", "100", "- a: int", "+ kaputt");
        assertNotNull(fehler);
        assertTrue(fehler.startsWith("Methode 1:"));
        assertEquals("Kunde", n.getName());
        assertEquals(10, n.getX(), 0.001);
        assertEquals(List.of(), KlassenInhalt.attribute(n));
        assertEquals(undo, controller.getHistory().undoAnzahl());
        assertEquals(version, controller.getVersion());
    }

    @Test
    void kastenWaechstUndUndoStelltGroesseWiederHer() {
        Node n = controller.elementHinzufuegen("class", "Kunde", 10, 20); // 160 x 100
        String fuenf = "- a1: int\n- a2: int\n- a3: int\n- a4: int\n- a5: int";

        assertNull(uebernehmen(n, fuenf, "+ m(): void"));
        // 28 Kopf + (5 * 16 + 8) Attribute + (16 + 8) Methoden = 140
        assertEquals(140, n.getH(), 0.001);

        controller.rueckgaengig();
        assertEquals(100, n.getH(), 0.001);

        controller.wiederholen();
        assertEquals(140, n.getH(), 0.001);
    }

    @Test
    void unveraenderterInhaltErzeugtKeinenBefehl() {
        Node n = controller.elementHinzufuegen("class", "Kunde", 10, 20);
        assertNull(uebernehmen(n, "- a: int", ""));
        int undo = controller.getHistory().undoAnzahl();
        long version = controller.getVersion();

        // Leerzeilen und Randleerzeichen zählen nicht als Änderung.
        assertNull(uebernehmen(n, "  - a: int  \n\n", null));
        assertEquals(undo, controller.getHistory().undoAnzahl());
        assertEquals(version, controller.getVersion());
    }

    @Test
    void paketIgnoriertInhalt() {
        Node paket = controller.elementHinzufuegen("package", "p", 0, 0);
        assertNull(controller.eigenschaftenUebernehmen(paket, "p",
                "0", "0", "300", "200", "- a: int", "+ m(): void"));
        assertEquals(List.of(), KlassenInhalt.attribute(paket));
        assertFalse(paket.getProps().containsKey(KlassenInhalt.ATTRIBUTE));
    }

    @Test
    void verschiebenBehaeltInhalt() {
        Node n = controller.elementHinzufuegen("class", "Kunde", 10, 20);
        assertNull(uebernehmen(n, "- a: int", "+ m(): void"));

        controller.zugBeginnen(n);
        controller.zugAktualisieren(30, 0);
        controller.zugAbschliessen();
        assertEquals(40, n.getX(), 0.001);
        assertEquals(List.of("- a: int"), KlassenInhalt.attribute(n));

        controller.rueckgaengig();
        assertEquals(10, n.getX(), 0.001);
        assertEquals(List.of("- a: int"), KlassenInhalt.attribute(n));
    }

    @Test
    void alteMethodeLaesstInhaltUnveraendert() {
        Node n = controller.elementHinzufuegen("class", "Kunde", 10, 20);
        assertNull(uebernehmen(n, "- a: int", "+ m(): void"));

        assertNull(controller.eigenschaftenUebernehmen(
                n, "Neu", "10", "20", "160", "100"));
        assertEquals("Neu", n.getName());
        assertEquals(List.of("- a: int"), KlassenInhalt.attribute(n));
        assertEquals(List.of("+ m(): void"), KlassenInhalt.methoden(n));
    }
}