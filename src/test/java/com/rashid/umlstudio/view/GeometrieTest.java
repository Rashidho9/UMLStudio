package com.rashid.umlstudio.view;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Prüft die Rechenlogik der Zeichenfläche. */
class GeometrieTest {

    @Test
    void punktInnerhalbUndAusserhalb() {
        Node n = Catalog.neuesElement("class", "class", "A", 10, 10);   // 160 x 100
        assertTrue(Geometrie.enthaelt(n, 50, 50));
        assertFalse(Geometrie.enthaelt(n, 500, 50));
    }

    @Test
    void trefferBevorzugtKindImPaket() {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D");
        Node paket = Catalog.neuesElement("class", "package", "p", 0, 0);
        Node klasse = Catalog.neuesElement("class", "class", "K", 20, 40);
        klasse.setParentId(paket.getId());
        d.addNode(paket);
        d.addNode(klasse);

        assertSame(klasse, Geometrie.treffer(d, 50, 60));
        assertSame(paket, Geometrie.treffer(d, 280, 180));
        assertNull(Geometrie.treffer(d, 900, 900));
    }

    @Test
    void nachkommenUeberMehrereEbenen() {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D");
        Node aussen = Catalog.neuesElement("class", "package", "aussen", 0, 0);
        Node innen = Catalog.neuesElement("class", "package", "innen", 10, 10);
        Node k = Catalog.neuesElement("class", "class", "K", 20, 20);
        innen.setParentId(aussen.getId());
        k.setParentId(innen.getId());
        d.addNode(aussen);
        d.addNode(innen);
        d.addNode(k);

        assertEquals(2, Geometrie.nachkommen(d, aussen).size());
        assertEquals(2, Geometrie.tiefe(d, k));
    }

    @Test
    void randpunktLiegtAufDemRahmen() {
        Node n = Catalog.neuesElement("class", "class", "A", 0, 0);   // Mitte (80, 50)
        double[] rechts = Geometrie.randpunkt(n, 1000, 50);
        assertEquals(160, rechts[0], 0.001);
        assertEquals(50, rechts[1], 0.001);

        double[] unten = Geometrie.randpunkt(n, 80, 1000);
        assertEquals(80, unten[0], 0.001);
        assertEquals(100, unten[1], 0.001);
    }
}