package com.rashid.umlstudio.model.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** NEU Schritt 11: Prüfung doppelter Attributnamen. */
class DiagramPrueferKlassenTest {

    private static Diagram mit(Node n) {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Test");
        d.addNode(n);
        return d;
    }

    @Test
    void doppeltesAttributWirdGemeldet() {
        Node kunde = Catalog.neuesElement("class", "class", "Kunde", 0, 0);
        KlassenInhalt.setzen(kunde, List.of("- name: String", "+ name: int"), List.of());

        List<Pruefmeldung> funde = DiagramPruefer.pruefe(mit(kunde));
        assertEquals(1, funde.size());
        assertEquals("ATTRIBUT_DOPPELT", funde.get(0).code());
        assertEquals(kunde.getId(), funde.get(0).elementId());
        assertTrue(funde.get(0).text().contains("name"));
    }

    @Test
    void verschiedeneAttributeSindOhneBefund() {
        Node kunde = Catalog.neuesElement("class", "class", "Kunde", 0, 0);
        KlassenInhalt.setzen(kunde, List.of("- name: String", "- alter: int"), List.of());
        assertEquals(List.of(), DiagramPruefer.pruefe(mit(kunde)));
    }

    @Test
    void notizWirdNichtGeprueft() {
        Node notiz = Catalog.neuesElement("class", "note", "Hinweis", 0, 0);
        KlassenInhalt.setzen(notiz, List.of("- a: int", "- a: int"), List.of());
        assertEquals(List.of(), DiagramPruefer.pruefe(mit(notiz)));
    }
}