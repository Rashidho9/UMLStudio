package com.rashid.umlstudio.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.catalog.Catalog;

/** Prüft die Eingabeprüfung und Bearbeitung ohne JavaFX-Oberfläche. */
class ElementBearbeitungTest {

    private static Diagram diagramMit(Node... nodes) {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D");
        for (Node n : nodes) {
            d.addNode(n);
        }
        return d;
    }

    @Test
    void nameWirdGetrimmtGesetzt() {
        Node n = Catalog.neuesElement("class", "class", "Alt", 10, 20);
        Diagram d = diagramMit(n);
        assertNull(ElementBearbeitung.anwenden(
                d, n, "  Kunde  ", "10", "20", "160", "100"));
        assertEquals("Kunde", n.getName());
    }

    @Test
    void ungueltigeZahlLaesstModellUnveraendert() {
        Node n = Catalog.neuesElement("class", "class", "A", 10, 20);
        Diagram d = diagramMit(n);
        String meldung = ElementBearbeitung.anwenden(
                d, n, "Neu", "abc", "20", "160", "100");
        assertEquals("X ist keine gültige Zahl.", meldung);
        assertEquals(10, n.getX(), 0.001);
        assertEquals(20, n.getY(), 0.001);
        assertEquals("A", n.getName());
    }

    @Test
    void unendlicheZahlWirdAbgelehnt() {
        Node n = Catalog.neuesElement("class", "class", "A", 10, 20);
        Diagram d = diagramMit(n);
        assertEquals("Y muss eine endliche Zahl sein.",
                ElementBearbeitung.anwenden(
                        d, n, "A", "10", "Infinity", "160", "100"));
        assertEquals("Breite muss eine endliche Zahl sein.",
                ElementBearbeitung.anwenden(
                        d, n, "A", "10", "20", "NaN", "100"));
        assertEquals(10, n.getX(), 0.001);
        assertEquals(160, n.getW(), 0.001);
    }

    @Test
    void ungueltigeGroesseAendertNichts() {
        Node n = Catalog.neuesElement("class", "class", "A", 10, 20);
        Diagram d = diagramMit(n);
        String meldung = ElementBearbeitung.anwenden(
                d, n, "Neu", "90", "80", "0", "80");
        assertEquals("Breite und Höhe müssen größer als 0 sein.", meldung);
        assertEquals("A", n.getName());
        assertEquals(10, n.getX(), 0.001);
        assertEquals(160, n.getW(), 0.001);
        assertEquals(100, n.getH(), 0.001);
    }

    @Test
    void gueltigeWerteSetzenPositionUndGroesse() {
        Node n = Catalog.neuesElement("class", "class", "A", 10, 20);
        Diagram d = diagramMit(n);
        assertNull(ElementBearbeitung.anwenden(
                d, n, "B", "80,5", "90", "200", "120"));
        assertEquals("B", n.getName());
        assertEquals(80.5, n.getX(), 0.001);
        assertEquals(90, n.getY(), 0.001);
        assertEquals(200, n.getW(), 0.001);
        assertEquals(120, n.getH(), 0.001);
    }

    @Test
    void paketVerschiebenNimmtNachkommenMit() {
        Node paket = Catalog.neuesElement("class", "package", "p", 0, 0);
        Node kind = Catalog.neuesElement("class", "class", "K", 20, 40);
        kind.setParentId(paket.getId());
        Diagram d = diagramMit(paket, kind);

        assertNull(ElementBearbeitung.anwenden(
                d, paket, "p", "100", "50", "300", "200"));
        assertEquals(100, paket.getX(), 0.001);
        assertEquals(50, paket.getY(), 0.001);
        assertEquals(120, kind.getX(), 0.001);
        assertEquals(90, kind.getY(), 0.001);
    }

    @Test
    void kindBearbeitenLaesstElternUnveraendert() {
        Node paket = Catalog.neuesElement("class", "package", "p", 0, 0);
        Node kind = Catalog.neuesElement("class", "class", "K", 20, 40);
        kind.setParentId(paket.getId());
        Diagram d = diagramMit(paket, kind);

        assertEquals("Höhe darf nicht leer sein.",
                ElementBearbeitung.anwenden(
                        d, kind, "Neu", "99", "88", "160", "  "));
        assertEquals("K", kind.getName());
        assertEquals(20, kind.getX(), 0.001);

        assertNull(ElementBearbeitung.anwenden(
                d, kind, "K", "30", "60", "160", "100"));
        assertEquals(30, kind.getX(), 0.001);
        assertEquals(60, kind.getY(), 0.001);
        assertEquals(0, paket.getX(), 0.001);
        assertEquals(0, paket.getY(), 0.001);
    }

    @Test
    void leererNameAendertNichts() {
        Node n = Catalog.neuesElement("class", "class", "A", 10, 20);
        Diagram d = diagramMit(n);
        assertEquals("Name darf nicht leer sein.",
                ElementBearbeitung.anwenden(
                        d, n, "   ", "99", "88", "200", "120"));
        assertEquals("Name darf nicht leer sein.",
                ElementBearbeitung.anwenden(
                        d, n, null, "99", "88", "200", "120"));
        assertEquals("A", n.getName());
        assertEquals(10, n.getX(), 0.001);
        assertEquals(20, n.getY(), 0.001);
        assertEquals(160, n.getW(), 0.001);
        assertEquals(100, n.getH(), 0.001);
    }
}