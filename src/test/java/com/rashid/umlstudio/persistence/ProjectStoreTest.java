package com.rashid.umlstudio.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.Project;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Prüft Speichern, Laden und die Fehlerfälle von ProjectStore. */
class ProjectStoreTest {

    /** JUnit legt hier für jeden Test einen leeren Ordner an und löscht ihn danach. */
    @TempDir
    Path ordner;

    /** Baut ein kleines Projekt: Paket mit Klasse, dazu eine zweite Klasse mit Verbindung. */
    private Project beispielProjekt() {
        Project p = new Project("Testprojekt");
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Datenmodell");
        Node paket = Catalog.neuesElement("class", "package", "model", 0, 0);
        Node kunde = Catalog.neuesElement("class", "class", "Kunde", 20, 40);
        Node auftrag = Catalog.neuesElement("class", "class", "Auftrag", 400, 40);
        kunde.setParentId(paket.getId());
        d.addNode(paket);
        d.addNode(kunde);
        d.addNode(auftrag);
        Edge e = new Edge("association", kunde.getId(), auftrag.getId());
        e.setName("erteilt");
        d.addEdge(e);
        p.addDiagram(d);
        return p;
    }

    @Test
    void speichernUndLadenErgibtGleichenInhalt() throws Exception {
        Project original = beispielProjekt();
        Path datei = ProjectStore.speichern(original, ordner.resolve("test.umls"));

        Project geladen = ProjectStore.laden(datei);
        Diagram d = geladen.getDiagrams().get(0);

        assertEquals("Testprojekt", geladen.getName());
        assertEquals(3, d.getNodes().size());
        assertEquals(1, d.getEdges().size());
        assertEquals("erteilt", d.getEdges().get(0).getName());
        assertEquals(original.getDiagrams().get(0).getId(), d.getId());
    }

    @Test
    void verschachtelungBleibtErhalten() throws Exception {
        Path datei = ProjectStore.speichern(beispielProjekt(), ordner.resolve("test.umls"));
        Diagram d = ProjectStore.laden(datei).getDiagrams().get(0);
        Node kunde = d.getNodes().stream()
                .filter(n -> n.getName().equals("Kunde")).findFirst().orElseThrow();
        assertEquals(1, d.depth(kunde.getId()));
    }

    @Test
    void endungWirdErgaenzt() throws Exception {
        Path datei = ProjectStore.speichern(beispielProjekt(), ordner.resolve("ohneEndung"));
        assertTrue(datei.getFileName().toString().endsWith(".umls"));
        assertTrue(Files.exists(datei));
    }

    @Test
    void fehlendeDateiGibtFehlermeldung() {
        assertThrows(ProjektDateiException.class,
                () -> ProjectStore.laden(ordner.resolve("gibt-es-nicht.umls")));
    }

    @Test
    void kaputtesJsonGibtFehlermeldung() throws Exception {
        Path datei = ordner.resolve("kaputt.umls");
        Files.writeString(datei, "{ das ist kein json", StandardCharsets.UTF_8);
        assertThrows(ProjektDateiException.class, () -> ProjectStore.laden(datei));
    }

    @Test
    void fremdeDateiWirdErkannt() throws Exception {
        Path datei = ordner.resolve("fremd.umls");
        Files.writeString(datei, "{\"app\":\"AnderesProgramm\"}", StandardCharsets.UTF_8);
        assertThrows(ProjektDateiException.class, () -> ProjectStore.laden(datei));
    }

    @Test
    void neueresFormatWirdAbgelehnt() throws Exception {
        Path datei = ordner.resolve("neu.umls");
        Files.writeString(datei,
                "{\"app\":\"UMLStudio\",\"formatVersion\":99,\"project\":{\"name\":\"X\",\"diagrams\":[]}}",
                StandardCharsets.UTF_8);
        assertThrows(ProjektDateiException.class, () -> ProjectStore.laden(datei));
    }
}