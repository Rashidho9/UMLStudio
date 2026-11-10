package com.rashid.umlstudio.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Prüft Erzeugung, Geometrie und Validierung von Node. */
class NodeTest {

    @Test
    void neuesElementHatIdUndStandardgroesse() {
        Node n = new Node("class", "Kunde", 10, 20);
        assertNotNull(n.getId());
        assertEquals(100.0, n.getW());
        assertEquals(60.0, n.getH());
    }

    @Test
    void mittelpunktWirdRichtigBerechnet() {
        Node n = new Node("class", "Kunde", 0, 0);
        assertArrayEquals(new double[] { 50.0, 30.0 }, n.center());
    }

    @Test
    void verschiebenAendertPosition() {
        Node n = new Node("class", "Kunde", 10, 10);
        n.verschieben(5, -3);
        assertEquals(15.0, n.getX());
        assertEquals(7.0, n.getY());
    }

    @Test
    void leereElementartWirdAbgelehnt() {
        assertThrows(IllegalArgumentException.class, () -> new Node(" ", "X", 0, 0));
    }

    @Test
    void ungueltigeGroesseWirdAbgelehnt() {
        Node n = new Node("class", "Kunde", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> n.setGroesse(0, 50));
    }

    @Test
    void elementKannNichtInSichSelbstLiegen() {
        Node n = new Node("package", "model", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> n.setParentId(n.getId()));
    }
}