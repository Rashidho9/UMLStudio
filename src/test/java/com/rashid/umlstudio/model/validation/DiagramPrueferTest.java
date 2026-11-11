package com.rashid.umlstudio.model.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;

/** Prüft gültige und absichtlich beschädigte Diagramme. */
class DiagramPrueferTest {

    private static final Gson GSON = new Gson();

    private static Diagram diagram() {
        return Catalog.neuesDiagramm(Catalog.KLASSENDIAGRAMM, "Test");
    }

    private static boolean hat(List<Pruefmeldung> funde, String code) {
        return funde.stream().anyMatch(m -> code.equals(m.code()));
    }

    /** Ausschließlich für beschädigte Testdaten verwenden. */
    private static void feld(Object ziel, String name, Object wert) throws Exception {
        Field f = ziel.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(ziel, wert);
    }

    @Test
    void leeresGueltigesDiagrammIstOhneBefund() {
        assertEquals(List.of(), DiagramPruefer.pruefe(diagram()));
    }

    @Test
    void gueltigesDiagrammMitPaketUndVerbindung() {
        Diagram d = diagram();
        Node paket = Catalog.neuesElement("class", "package", "model", 0, 0);
        Node a = Catalog.neuesElement("class", "class", "A", 20, 40);
        Node b = Catalog.neuesElement("class", "class", "B", 400, 40);
        a.setParentId(paket.getId());
        d.addNode(paket);
        d.addNode(a);
        d.addNode(b);
        d.addEdge(new Edge("association", a.getId(), b.getId()));
        assertEquals(List.of(), DiagramPruefer.pruefe(d));
    }

    @Test
    void fehlendesDiagrammErzeugtMeldung() {
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(null);
        assertEquals(1, funde.size());
        assertEquals("DIAGRAMM_FEHLT", funde.get(0).code());
    }

    @Test
    void fehlendeListenUndLeereEintraegeWerdenGemeldet() {
        Diagram leer = GSON.fromJson(
                "{\"id\":\"d1\",\"type\":\"class\",\"nodes\":null,\"edges\":null}",
                Diagram.class);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(leer);
        assertTrue(hat(funde, "ELEMENTLISTE_FEHLT"));
        assertTrue(hat(funde, "KANTENLISTE_FEHLT"));

        Diagram luecken = GSON.fromJson(
                "{\"id\":\"d2\",\"type\":\"class\",\"nodes\":[null],\"edges\":[null]}",
                Diagram.class);
        funde = DiagramPruefer.pruefe(luecken);
        assertTrue(hat(funde, "ELEMENT_NULL"));
        assertTrue(hat(funde, "KANTE_NULL"));
    }

    @Test
    void fehlendeUndDoppelteElementIds() throws Exception {
        Diagram d = diagram();
        Node a = new Node("class", "A", 0, 0);
        Node b = new Node("class", "B", 10, 0);
        Node ohneId = new Node("class", "C", 20, 0);
        feld(b, "id", a.getId());
        feld(ohneId, "id", "");
        d.getNodes().add(a);
        d.getNodes().add(b);
        d.getNodes().add(ohneId);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertTrue(hat(funde, "ELEMENT_ID_DOPPELT"));
        assertTrue(hat(funde, "ELEMENT_ID_FEHLT"));
    }

    @Test
    void verbindungOhneEndpunkteUndId() throws Exception {
        Diagram d = diagram();
        Edge k = new Edge("association", "fehlt1", "fehlt2");
        feld(k, "id", null);
        d.getEdges().add(k);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertTrue(hat(funde, "KANTE_QUELLE_FEHLT"));
        assertTrue(hat(funde, "KANTE_ZIEL_FEHLT"));
        assertTrue(hat(funde, "KANTE_ID_FEHLT"));
    }

    @Test
    void doppelteVerbindungsId() throws Exception {
        Diagram d = diagram();
        Node a = new Node("class", "A", 0, 0);
        Node b = new Node("class", "B", 200, 0);
        d.addNode(a);
        d.addNode(b);
        Edge k1 = new Edge("association", a.getId(), b.getId());
        Edge k2 = new Edge("association", b.getId(), a.getId());
        feld(k2, "id", k1.getId());
        d.getEdges().add(k1);
        d.getEdges().add(k2);
        assertTrue(hat(DiagramPruefer.pruefe(d), "KANTE_ID_DOPPELT"));
    }

    @Test
    void unbekannterDiagrammtypWirdGemeldet() {
        Diagram d = new Diagram("gibt-es-nicht", "X");
        assertTrue(hat(DiagramPruefer.pruefe(d), "DIAGRAMM_TYP_UNBEKANNT"));
    }

    @Test
    void unbekannteElementUndVerbindungsarten() {
        Diagram d = diagram();
        Node a = new Node("actor", "A", 0, 0);
        d.addNode(a);
        d.getEdges().add(new Edge("includes", a.getId(), a.getId()));
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertTrue(hat(funde, "ELEMENT_ART_UNBEKANNT"));
        assertTrue(hat(funde, "KANTE_ART_UNBEKANNT"));
    }

    @Test
    void fehlendeUndSelbstbezogeneEltern() throws Exception {
        Diagram d = diagram();
        Node a = new Node("class", "A", 0, 0);
        Node b = new Node("class", "B", 10, 0);
        a.setParentId("fehlt");
        feld(b, "parentId", b.getId());
        d.addNode(a);
        d.addNode(b);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertTrue(hat(funde, "ELTERN_FEHLT"));
        assertTrue(hat(funde, "ELTERN_SELBST"));
    }

    @Test
    void zyklusWirdErkannt() {
        Diagram d = diagram();
        Node a = Catalog.neuesElement("class", "package", "A", 0, 0);
        Node b = Catalog.neuesElement("class", "package", "B", 10, 0);
        a.setParentId(b.getId());
        b.setParentId(a.getId());
        d.addNode(a);
        d.addNode(b);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertEquals(2L, funde.stream()
                .filter(m -> "ELTERN_ZYKLUS".equals(m.code())).count());
    }

    @Test
    void klasseIstKeinBehaelter() {
        Diagram d = diagram();
        Node klasse = new Node("class", "K", 0, 0);
        Node kind = new Node("class", "Kind", 10, 10);
        kind.setParentId(klasse.getId());
        d.addNode(klasse);
        d.addNode(kind);
        assertTrue(hat(DiagramPruefer.pruefe(d), "ELTERN_KEIN_BEHAELTER"));
    }

    @Test
    void ungueltigePositionUndGroesse() throws Exception {
        Diagram d = diagram();
        Node a = new Node("class", "A", 0, 0);
        feld(a, "x", Double.NaN);
        feld(a, "y", Double.POSITIVE_INFINITY);
        feld(a, "w", 0.0);
        feld(a, "h", -10.0);
        d.addNode(a);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertTrue(hat(funde, "ELEMENT_POSITION_UNGUELTIG"));
        assertTrue(hat(funde, "ELEMENT_GROESSE_UNGUELTIG"));
    }

    @Test
    void negativeKoordinatenSindErlaubt() {
        Diagram d = diagram();
        d.addNode(new Node("class", "A", -40, -10));
        assertEquals(List.of(), DiagramPruefer.pruefe(d));
    }

    @Test
    void ergebnisIstUnveraenderlichUndModellBleibtGleich() {
        Diagram d = diagram();
        d.addNode(new Node("class", "A", 0, 0));
        String vorher = GSON.toJson(d);
        List<Pruefmeldung> funde = DiagramPruefer.pruefe(d);
        assertEquals(List.of(), funde);
        assertThrows(UnsupportedOperationException.class,
                () -> funde.add(new Pruefmeldung("X", null, "Test")));
        assertEquals(vorher, GSON.toJson(d));
    }
}