package com.rashid.umlstudio.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.catalog.Catalog;

/** NEU Schritt 11: Prüft die Aufteilung des Klassenkastens in drei Abschnitte. */
class KlassenLayoutTest {

    @Test
    void leereKlasseTeiltWieBisher() {
        Node n = Catalog.neuesElement("class", "class", "A", 0, 0); // 160 x 100
        KlassenLayout l = KlassenLayout.fuer(n);
        assertEquals(28, l.kopf(), 0.001);
        assertEquals(36, l.attributHoehe(), 0.001);
        assertEquals(36, l.methodenHoehe(), 0.001);
    }

    @Test
    void schnittstelleHatHoeherenKopf() {
        Node n = Catalog.neuesElement("class", "interface", "I", 0, 0); // 160 x 80
        assertEquals(40, KlassenLayout.fuer(n).kopf(), 0.001);
        assertEquals("«interface»", KlassenLayout.stereotyp("interface"));
        assertNull(KlassenLayout.stereotyp("class"));
        assertNull(KlassenLayout.stereotyp(null));
    }

    @Test
    void mindestHoeheZaehltZeilen() {
        Node n = new Node("class", "A", 0, 0);
        KlassenInhalt.setzen(n, List.of("- a: int", "- b: int"), List.of("+ m(): void"));
        // 28 Kopf + (2 * 16 + 8) Attribute + (16 + 8) Methoden
        assertEquals(92, KlassenLayout.mindestHoehe(n), 0.001);
    }

    @Test
    void freierPlatzWirdVerteilt() {
        Node n = Catalog.neuesElement("class", "class", "A", 0, 10); // 160 x 100
        KlassenInhalt.setzen(n, List.of("- a: int", "- b: int"), List.of("+ m(): void"));
        KlassenLayout l = KlassenLayout.fuer(n);
        assertEquals(44, l.attributHoehe(), 0.001);
        assertEquals(28, l.methodenHoehe(), 0.001);
        assertEquals(38, l.attributeOben(n.getY()), 0.001);
        assertEquals(82, l.methodenOben(n.getY()), 0.001);
    }

    @Test
    void groesseAnpassenVergroessert() {
        Node n = new Node("class", "A", 0, 0); // 100 x 60
        KlassenInhalt.setzen(n, List.of("- a: int", "- b: int", "- c: int"),
                List.of("+ " + "x".repeat(30) + "()"));
        assertTrue(KlassenLayout.groesseAnpassen(n));
        // 34 Zeichen * 7 + 2 * 6 = 250; 28 + 56 + 24 = 108
        assertEquals(250, n.getW(), 0.001);
        assertEquals(108, n.getH(), 0.001);
    }

    @Test
    void groesseAnpassenVerkleinertNie() {
        Node n = Catalog.neuesElement("class", "class", "A", 0, 0);
        assertFalse(KlassenLayout.groesseAnpassen(n));
        assertEquals(160, n.getW(), 0.001);
        assertEquals(100, n.getH(), 0.001);
    }

    @Test
    void zeilenPositionen() {
        assertEquals(112, KlassenLayout.zeileY(100, 0), 0.001);
        assertEquals(144, KlassenLayout.zeileY(100, 2), 0.001);
    }
}