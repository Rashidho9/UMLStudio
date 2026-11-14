package com.rashid.umlstudio.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.catalog.Catalog;

/** Tests für Methodensignaturen und doppelte Methoden. */
class KlassenInhaltMethodenTest {

    @Test
    void signaturOhneParameter() {
        assertEquals("getName()", KlassenInhalt.methodenSignatur("+ getName(): String"));
    }

    @Test
    void signaturNimmtNurParametertypen() {
        assertEquals("setze(int, String)",
                KlassenInhalt.methodenSignatur("+ setze(a: int, b : String): void"));
    }

    @Test
    void keineMethodeErgibtNull() {
        assertNull(KlassenInhalt.methodenSignatur("- name: String"));
        assertNull(KlassenInhalt.methodenSignatur(null));
    }

    @Test
    void ueberladeneMethodenSindNichtDoppelt() {
        Node n = Catalog.neuesElement("class", "class", "Kasse", 0, 0);
        KlassenInhalt.setzen(n, List.of(), List.of(
                "+ bezahlen(betrag: double): void",
                "+ bezahlen(betrag: int): void",
                "+ bezahlen(summe: double): boolean"));
        assertEquals(List.of("bezahlen(double)"), KlassenInhalt.doppelteMethoden(n));
    }
}