package com.rashid.umlstudio.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Testet den Versionszähler des DocumentControllers. */
class DocumentControllerVersionTest {

    private DocumentController neu() {
        return new DocumentController(
                Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Test"));
    }

    @Test
    void startetBeiNull() {
        assertEquals(0, neu().getVersion());
    }

    @Test
    void hinzufuegenErhoehtVersion() {
        DocumentController c = neu();
        c.elementHinzufuegen("class", "A", 10, 10);
        assertEquals(1, c.getVersion());
    }

    @Test
    void auswaehlenAendertVersionNicht() {
        DocumentController c = neu();
        Node a = c.elementHinzufuegen("class", "A", 10, 10);
        c.auswaehlen(null);
        c.auswaehlen(a);
        assertEquals(1, c.getVersion());
    }

    @Test
    void undoUndRedoZaehlenAlsAenderung() {
        DocumentController c = neu();
        c.elementHinzufuegen("class", "A", 10, 10);
        c.rueckgaengig();
        assertEquals(2, c.getVersion());
        c.wiederholen();
        assertEquals(3, c.getVersion());
    }

    @Test
    void ungueltigeEigenschaftAendertVersionNicht() {
        DocumentController c = neu();
        Node a = c.elementHinzufuegen("class", "A", 10, 10);
        String fehler = c.eigenschaftenUebernehmen(a, "", "10", "10", "160", "100");
        assertNotNull(fehler);
        assertEquals(1, c.getVersion());
    }
}