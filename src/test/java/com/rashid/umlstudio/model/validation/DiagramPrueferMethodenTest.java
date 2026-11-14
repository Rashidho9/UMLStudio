package com.rashid.umlstudio.model.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Prüfung doppelter Methoden im Diagramm. */
class DiagramPrueferMethodenTest {

    private static Diagram mit(Node n) {
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Test");
        d.addNode(n);
        return d;
    }

    @Test
    void doppelteMethodeWirdGemeldet() {
        Node kunde = Catalog.neuesElement("class", "class", "Kunde", 0, 0);
        KlassenInhalt.setzen(kunde, List.of(),
                List.of("+ bestellen(): void", "+ bestellen(): void"));

        List<Pruefmeldung> funde = DiagramPruefer.pruefe(mit(kunde));
        assertEquals(1, funde.size());
        assertEquals("METHODE_DOPPELT", funde.get(0).code());
        assertEquals(kunde.getId(), funde.get(0).elementId());
        assertTrue(funde.get(0).text().contains("bestellen()"));
    }

    @Test
    void ueberladenIstOhneBefund() {
        Node kasse = Catalog.neuesElement("class", "class", "Kasse", 0, 0);
        KlassenInhalt.setzen(kasse, List.of(), List.of(
                "+ bezahlen(betrag: double): void",
                "+ bezahlen(betrag: int): void"));
        assertEquals(List.of(), DiagramPruefer.pruefe(mit(kasse)));
    }

    @Test
    void notizWirdNichtGeprueft() {
        Node notiz = Catalog.neuesElement("class", "note", "Hinweis", 0, 0);
        KlassenInhalt.setzen(notiz, List.of(), List.of("+ a(): void", "+ a(): void"));
        assertEquals(List.of(), DiagramPruefer.pruefe(mit(notiz)));
    }
}