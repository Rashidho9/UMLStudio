package com.rashid.umlstudio.export;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.Node;

/** NEU Schritt 11: Attribute und Methoden im SVG-Export. */
class SvgExporterKlassenTest {

    /** Klasse bei (0, 0), 160 x 100, mit einem Attribut und einer Methode. */
    private Diagram beispiel(String attribut) {
        Diagram d = new Diagram("class", "Test");
        Node kunde = new Node("class", "Kunde", 0, 0);
        kunde.setGroesse(160, 100);
        KlassenInhalt.setzen(kunde, List.of(attribut), List.of("+ getName(): String"));
        d.addNode(kunde);
        return d;
    }

    @Test
    void zeilenStehenLinksbuendigImSvg() {
        String svg = SvgExporter.alsSvg(beispiel("- name: String"));
        assertTrue(svg.contains(">- name: String</text>"));
        assertTrue(svg.contains(">+ getName(): String</text>"));
        assertTrue(svg.contains("text-anchor=\"start\""));
    }

    @Test
    void zeilenUndTrennlinienAnRichtigerStelle() {
        String svg = SvgExporter.alsSvg(beispiel("- name: String"));
        // Kopf 28, je Abschnitt 24 + 12 freier Platz = 36
        assertTrue(svg.contains("<text x=\"6.0\" y=\"40.0\""));
        assertTrue(svg.contains("<text x=\"6.0\" y=\"76.0\""));
        assertTrue(svg.contains("<line x1=\"0.0\" y1=\"28.0\" x2=\"160.0\" y2=\"28.0\""));
        assertTrue(svg.contains("<line x1=\"0.0\" y1=\"64.0\" x2=\"160.0\" y2=\"64.0\""));
    }

    @Test
    void sonderzeichenInAttributenWerdenMaskiert() {
        String svg = SvgExporter.alsSvg(beispiel("- liste: List<String>"));
        assertTrue(svg.contains("- liste: List&lt;String&gt;"));
        assertFalse(svg.contains("List<String>"));
    }
}