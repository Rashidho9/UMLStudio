package com.rashid.umlstudio.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rashid.umlstudio.model.Project;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Testet die automatische Sicherung in einem temporären Ordner. */
class AutosaveTest {

    @TempDir
    Path ordner;

    private Project projekt() {
        Project p = new Project("Test");
        p.addDiagram(Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D"));
        return p;
    }

    @Test
    void amAnfangKeineSicherung() {
        assertFalse(new Autosave(ordner).vorhanden());
    }

    @Test
    void speichernUndLaden() throws Exception {
        Autosave a = new Autosave(ordner);
        a.speichern(projekt());
        assertTrue(a.vorhanden());
        Project geladen = a.laden();
        assertEquals("Test", geladen.getName());
        assertEquals(1, geladen.getDiagrams().size());
    }

    @Test
    void legtFehlendenOrdnerAn() throws Exception {
        Autosave a = new Autosave(ordner.resolve("neu").resolve("unter"));
        a.speichern(projekt());
        assertTrue(a.vorhanden());
    }

    @Test
    void loeschenEntferntSicherung() throws Exception {
        Autosave a = new Autosave(ordner);
        a.speichern(projekt());
        a.loeschen();
        assertFalse(a.vorhanden());
    }

    @Test
    void loeschenOhneDateiWirftNicht() {
        assertDoesNotThrow(() -> new Autosave(ordner).loeschen());
    }

    @Test
    void ladenOhneDateiMeldetFehler() {
        assertThrows(ProjektDateiException.class, () -> new Autosave(ordner).laden());
    }
}