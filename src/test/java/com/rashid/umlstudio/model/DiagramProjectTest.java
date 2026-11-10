package com.rashid.umlstudio.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Prüft Verschachtelung, Verbindungen und Projektverwaltung. */
class DiagramProjectTest {

    @Test
    void verschachtelungUndTiefe() {
        Diagram d = new Diagram("class", "Datenmodell");
        Node paket = new Node("package", "model", 0, 0);
        Node klasse = new Node("class", "Project", 10, 10);
        klasse.setParentId(paket.getId());
        d.addNode(paket);
        d.addNode(klasse);

        assertEquals(1, d.childrenOf(paket.getId()).size());
        assertEquals(1, d.depth(klasse.getId()));
        assertEquals(List.of(klasse.getId()), d.descendants(paket.getId()));
    }

    @Test
    void verbindungOhneZielWirdAbgelehnt() {
        Diagram d = new Diagram("class", "Datenmodell");
        Node a = new Node("class", "A", 0, 0);
        d.addNode(a);
        Edge kaputt = new Edge("association", a.getId(), "gibt-es-nicht");
        assertThrows(IllegalArgumentException.class, () -> d.addEdge(kaputt));
    }

    @Test
    void loeschenEntferntKinderUndVerbindungen() {
        Diagram d = new Diagram("class", "Datenmodell");
        Node paket = new Node("package", "model", 0, 0);
        Node kind = new Node("class", "Node", 10, 10);
        Node aussen = new Node("class", "App", 300, 0);
        kind.setParentId(paket.getId());
        d.addNode(paket);
        d.addNode(kind);
        d.addNode(aussen);
        d.addEdge(new Edge("dependency", aussen.getId(), kind.getId()));

        d.removeNode(paket.getId());

        assertEquals(1, d.getNodes().size());
        assertTrue(d.getEdges().isEmpty());
    }

    @Test
    void projektFindetDiagrammUndPosition() {
        Project p = new Project();
        Diagram d1 = new Diagram("class", "Eins");
        Diagram d2 = new Diagram("class", "Zwei");
        p.addDiagram(d1);
        p.addDiagram(d2);

        assertEquals("Neues Projekt", p.getName());
        assertEquals(1, p.indexOf(d2.getId()));
        assertTrue(p.diagram(d1.getId()).isPresent());
        assertTrue(p.removeDiagram(d1.getId()));
        assertEquals(-1, p.indexOf(d1.getId()));
    }
}