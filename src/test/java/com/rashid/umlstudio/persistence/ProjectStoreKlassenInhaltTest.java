package com.rashid.umlstudio.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.Project;
import com.rashid.umlstudio.model.catalog.Catalog;

/** NEU Schritt 11: Attribute und Methoden in .umls-Dateien. */
class ProjectStoreKlassenInhaltTest {

    @TempDir
    Path ordner;

    @Test
    void attributeUndMethodenBleibenErhalten() throws Exception {
        Project p = new Project("P");
        Diagram d = Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "D");
        Node kunde = Catalog.neuesElement("class", "class", "Kunde", 0, 0);
        KlassenInhalt.setzen(kunde,
                List.of("- name: String", "- alter: int"),
                List.of("+ getName(): String"));
        d.addNode(kunde);
        p.addDiagram(d);

        Path datei = ProjectStore.speichern(p, ordner.resolve("klassen.umls"));
        Node geladen = ProjectStore.laden(datei).getDiagrams().get(0).getNodes().get(0);

        assertEquals(List.of("- name: String", "- alter: int"),
                KlassenInhalt.attribute(geladen));
        assertEquals(List.of("+ getName(): String"), KlassenInhalt.methoden(geladen));
    }

    @Test
    void alteDateiOhneInhaltLaedt() throws Exception {
        String json = """
                {
                  "app": "UMLStudio",
                  "formatVersion": 1,
                  "project": {
                    "name": "Alt",
                    "diagrams": [ {
                      "id": "d1", "type": "class", "name": "D",
                      "nodes": [ {
                        "id": "n1", "kind": "class", "x": 0, "y": 0,
                        "w": 160, "h": 100, "name": "Kunde", "parentId": null
                      } ],
                      "edges": []
                    } ]
                  }
                }
                """;
        Path datei = ordner.resolve("alt.umls");
        Files.writeString(datei, json, StandardCharsets.UTF_8);

        Node n = ProjectStore.laden(datei).getDiagrams().get(0).getNodes().get(0);
        assertNotNull(n.getProps());
        assertEquals(List.of(), KlassenInhalt.attribute(n));
        assertEquals(List.of(), KlassenInhalt.methoden(n));
    }
}