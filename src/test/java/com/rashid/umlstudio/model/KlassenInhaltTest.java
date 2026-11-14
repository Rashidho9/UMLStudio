package com.rashid.umlstudio.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/** NEU Schritt 11: Prüft Lesen, Schreiben und Prüfen von Attributen und Methoden. */
class KlassenInhaltTest {

    private static Node klasse() {
        return new Node("class", "Kunde", 0, 0);
    }

    @Test
    void neueKlasseHatLeereListen() {
        Node n = klasse();
        assertEquals(List.of(), KlassenInhalt.attribute(n));
        assertEquals(List.of(), KlassenInhalt.methoden(n));
    }

    @Test
    void setzenUndLesen() {
        Node n = klasse();
        KlassenInhalt.setzen(n, List.of("- name: String"), List.of("+ getName(): String"));
        assertEquals(List.of("- name: String"), KlassenInhalt.attribute(n));
        assertEquals(List.of("+ getName(): String"), KlassenInhalt.methoden(n));
        assertTrue(n.getProps().containsKey(KlassenInhalt.ATTRIBUTE));
        assertTrue(n.getProps().containsKey(KlassenInhalt.METHODEN));
    }

    @Test
    void leereListeEntferntSchluessel() {
        Node n = klasse();
        KlassenInhalt.setzen(n, List.of("- a: int"), List.of("+ b(): void"));
        KlassenInhalt.setzen(n, List.of(), null);
        assertFalse(n.getProps().containsKey(KlassenInhalt.ATTRIBUTE));
        assertFalse(n.getProps().containsKey(KlassenInhalt.METHODEN));
    }

    @Test
    void zeilenEntferntLeerzeilenUndRaender() {
        assertEquals(List.of("- a: int", "+ b(): void"),
                KlassenInhalt.zeilen("  - a: int \n\n\r\n+ b(): void  \n"));
        assertEquals(List.of(), KlassenInhalt.zeilen(null));
        assertEquals(List.of(), KlassenInhalt.zeilen("   "));
    }

    @Test
    void alsTextVerbindetZeilen() {
        assertEquals("a\nb", KlassenInhalt.alsText(List.of("a", "b")));
        assertEquals("", KlassenInhalt.alsText(null));
    }

    @Test
    void gsonListeUndEinzelnerTextWerdenGelesen() {
        Node n = klasse();
        // So sieht eine Liste nach dem Laden mit Gson aus: List<Object>.
        List<Object> roh = new ArrayList<>();
        roh.add(" - a: int ");
        roh.add(null);
        roh.add("");
        n.getProps().put(KlassenInhalt.ATTRIBUTE, roh);
        n.getProps().put(KlassenInhalt.METHODEN, "+ x(): void\n+ y(): void");
        assertEquals(List.of("- a: int"), KlassenInhalt.attribute(n));
        assertEquals(List.of("+ x(): void", "+ y(): void"), KlassenInhalt.methoden(n));
    }

    @Test
    void propsNullAusDateiWirdLeereTabelle() {
        Node n = new Gson().fromJson(
                "{\"id\":\"1\",\"kind\":\"class\",\"props\":null}", Node.class);
        assertNotNull(n.getProps());
        assertEquals(List.of(), KlassenInhalt.attribute(n));
        KlassenInhalt.setzen(n, List.of("- a: int"), List.of());
        assertEquals(List.of("- a: int"), KlassenInhalt.attribute(n));
    }

    @Test
    void gueltigeZeilenOhneFehler() {
        assertNull(KlassenInhalt.pruefe(
                List.of("- name: String", "# /alter: int", "OFFEN",
                        "+ liste: List<String> = new ArrayList<>()"),
                List.of("+ getName(): String", "~ setze(a: int, b: int)")));
    }

    @Test
    void methodeOhneKlammernIstFehler() {
        String fehler = KlassenInhalt.pruefe(List.of(), List.of("+ getName: String"));
        assertNotNull(fehler);
        assertTrue(fehler.startsWith("Methode 1:"));
    }

    @Test
    void attributMitKlammernIstFehler() {
        String fehler = KlassenInhalt.pruefe(List.of("- a: int", "+ getName()"), List.of());
        assertNotNull(fehler);
        assertTrue(fehler.startsWith("Attribut 2:"));
        assertTrue(fehler.contains("Methode"));
    }

    @Test
    void attributOhneNamenIstFehler() {
        assertNotNull(KlassenInhalt.pruefeAttribut(": String"));
        assertNotNull(KlassenInhalt.pruefeAttribut("   "));
        assertNotNull(KlassenInhalt.pruefeAttribut(null));
    }

    @Test
    void zuLangeZeileIstFehler() {
        String lang = "- " + "a".repeat(KlassenInhalt.MAX_LAENGE);
        assertNotNull(KlassenInhalt.pruefeAttribut(lang));
        assertNotNull(KlassenInhalt.pruefeMethode(lang + "()"));
    }

    @Test
    void attributNameWirdErkannt() {
        assertEquals("name", KlassenInhalt.attributName("- name: String"));
        assertEquals("summe", KlassenInhalt.attributName("+ /summe: double"));
        assertEquals("OFFEN", KlassenInhalt.attributName("OFFEN"));
        assertNull(KlassenInhalt.attributName("123"));
        assertNull(KlassenInhalt.attributName(null));
    }

    @Test
    void doppelteAttributeWerdenGefunden() {
        Node n = klasse();
        KlassenInhalt.setzen(n,
                List.of("- name: String", "+ name: int", "- alter: int"), List.of());
        assertEquals(List.of("name"), KlassenInhalt.doppelteAttribute(n));
    }

    @Test
    void nurKlassenartenWerdenUnterstuetzt() {
        assertTrue(KlassenInhalt.unterstuetzt("class"));
        assertTrue(KlassenInhalt.unterstuetzt("interface"));
        assertTrue(KlassenInhalt.unterstuetzt("enum"));
        assertFalse(KlassenInhalt.unterstuetzt("package"));
        assertFalse(KlassenInhalt.unterstuetzt("note"));
        assertFalse(KlassenInhalt.unterstuetzt((String) null));
        assertFalse(KlassenInhalt.unterstuetzt((Node) null));
    }
}