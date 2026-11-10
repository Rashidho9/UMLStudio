package com.rashid.umlstudio.model.catalog;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;

/** Prüft den Katalog am Beispiel des Klassendiagramms. */
class CatalogTest {

    @Test
    void klassendiagrammIstVorhanden() {
        DiagramType typ = Catalog.typ(Catalog.KLASSENDIAGRAMM).orElseThrow();
        assertEquals("Klassendiagramm", typ.label());
        assertEquals(5, typ.nodeKinds().size());
        assertEquals(7, typ.edgeKinds().size());
    }

    @Test
    void erlaubteUndVerboteneArten() {
        DiagramType typ = Catalog.typ(Catalog.KLASSENDIAGRAMM).orElseThrow();
        assertTrue(typ.erlaubtElement("interface"));
        assertTrue(typ.erlaubtVerbindung("generalization"));
        assertFalse(typ.erlaubtElement("actor"));
    }

    @Test
    void neuesElementBekommtStandardgroesse() {
        Node klasse = Catalog.neuesElement(Catalog.KLASSENDIAGRAMM, "class", "Kunde", 0, 0);
        assertEquals("class", klasse.getKind());
        assertEquals(160.0, klasse.getW());
        assertEquals(100.0, klasse.getH());
    }

    @Test
    void unerlaubtesElementWirdAbgelehnt() {
        assertThrows(IllegalArgumentException.class,
                () -> Catalog.neuesElement(Catalog.KLASSENDIAGRAMM, "actor", "Kunde", 0, 0));
    }

    @Test
    void unbekannterDiagrammtypWirdAbgelehnt() {
        assertThrows(IllegalArgumentException.class,
                () -> Catalog.neuesDiagramm("gibt-es-nicht", "Test"));
    }

    @Test
    void neuesDiagrammHatRichtigenTyp() {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Datenmodell");
        assertEquals("class", d.getType());
    }

    @Test
    void katalogIstVonAussenNichtAenderbar() {
        DiagramType typ = Catalog.typ(Catalog.KLASSENDIAGRAMM).orElseThrow();
        assertThrows(UnsupportedOperationException.class,
                () -> typ.nodeKinds().add(new NodeKind("x", "X", 10, 10, false)));
    }
}