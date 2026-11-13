package com.rashid.umlstudio.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/** Testet Änderungserkennung und Fenstertitel. */
class DateiZustandTest {

    @Test
    void neuIstUnveraendertUndUnbenannt() {
        DateiZustand z = new DateiZustand();
        assertFalse(z.istGeaendert(0));
        assertTrue(z.getDatei().isEmpty());
        assertEquals("UMLStudio – Unbenannt", z.titel("UMLStudio", 0));
    }

    @Test
    void neueVersionGiltAlsAenderung() {
        DateiZustand z = new DateiZustand();
        assertTrue(z.istGeaendert(1));
        assertEquals("UMLStudio – Unbenannt *", z.titel("UMLStudio", 1));
    }

    @Test
    void speichernSetztDateiUndVersion() {
        DateiZustand z = new DateiZustand();
        z.gespeichert(Path.of("projekt.umls"), 5);
        assertFalse(z.istGeaendert(5));
        assertTrue(z.istGeaendert(6));
        assertEquals("UMLStudio – projekt.umls", z.titel("UMLStudio", 5));
    }

    @Test
    void ungespeichertMarkierenGiltImmerAlsGeaendert() {
        DateiZustand z = new DateiZustand();
        z.alsUngespeichertMarkieren();
        assertTrue(z.istGeaendert(0));
    }

    @Test
    void neuEntferntDatei() {
        DateiZustand z = new DateiZustand();
        z.gespeichert(Path.of("a.umls"), 3);
        z.neu(0);
        assertTrue(z.getDatei().isEmpty());
        assertFalse(z.istGeaendert(0));
    }
}