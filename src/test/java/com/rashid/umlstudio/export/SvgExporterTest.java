package com.rashid.umlstudio.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;

/** Tests für den SVG-Export. */
class SvgExporterTest {

    @TempDir
    Path ordner;

    private Diagram beispiel() {
        Diagram d = new Diagram("class", "Test");
        Node kunde = new Node("class", "Kunde", 300, 50);
        Node person = new Node("class", "Person", 50, 50);
        d.addNode(kunde);
        d.addNode(person);
        d.addEdge(new Edge("generalization", kunde.getId(), person.getId()));
        return d;
    }

    @Test
    void svgHatKopfUndGroesse() {
        String svg = SvgExporter.alsSvg(beispiel());

        assertTrue(svg.startsWith("<?xml"));
        assertTrue(svg.contains("<svg xmlns=\"http://www.w3.org/2000/svg\""));
        // 50..400 breit + 2 * 20 Rand = 390, 50..110 hoch + 40 = 100
        assertTrue(svg.contains("width=\"390.0\""));
        assertTrue(svg.contains("height=\"100.0\""));
        assertTrue(svg.trim().endsWith("</svg>"));
    }

    @Test
    void namenUndVererbungsdreieckSindEnthalten() {
        String svg = SvgExporter.alsSvg(beispiel());

        assertTrue(svg.contains(">Kunde</text>"));
        assertTrue(svg.contains(">Person</text>"));
        assertTrue(svg.contains("<polygon"));
    }

    @Test
    void abhaengigkeitIstGestrichelt() {
        Diagram d = new Diagram("class", "Test");
        Node a = new Node("class", "A", 0, 0);
        Node b = new Node("class", "B", 300, 0);
        d.addNode(a);
        d.addNode(b);
        d.addEdge(new Edge("dependency", a.getId(), b.getId()));

        assertTrue(SvgExporter.alsSvg(d).contains("stroke-dasharray=\"6 4\""));
    }

    @Test
    void sonderzeichenWerdenMaskiert() {
        Diagram d = new Diagram("class", "Test");
        d.addNode(new Node("class", "Liste<T> & Co", 0, 0));

        String svg = SvgExporter.alsSvg(d);

        assertTrue(svg.contains("Liste&lt;T&gt; &amp; Co"));
        assertFalse(svg.contains("Liste<T>"));
    }

    @Test
    void leeresDiagrammErgibtGueltigesSvg() {
        String svg = SvgExporter.alsSvg(new Diagram("class", "Leer"));

        assertTrue(svg.contains("width=\"200.0\""));
        assertTrue(svg.contains("height=\"100.0\""));
    }

    @Test
    void speichernHaengtEndungAnUndSchreibtDatei() throws IOException {
        Path datei = SvgExporter.speichern(beispiel(), ordner.resolve("diagramm"));

        assertEquals("diagramm.svg", datei.getFileName().toString());
        assertTrue(Files.exists(datei));
        String inhalt = Files.readString(datei, StandardCharsets.UTF_8);
        assertTrue(inhalt.contains("Kunde"));
    }

    @Test
    void vorhandeneEndungBleibt() {
        Path p = SvgExporter.mitEndung(ordner.resolve("bild.SVG"), ".svg");
        assertEquals("bild.SVG", p.getFileName().toString());
    }

    @Test
    void zahlenMitPunkt() {
        assertEquals("12.5", SvgExporter.zahl(12.5));
        assertEquals("-3.0", SvgExporter.zahl(-3));
    }
}