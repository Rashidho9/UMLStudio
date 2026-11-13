package com.rashid.umlstudio.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;

/** Tests für die Berechnung des Exportbereichs. */
class ExportBereichTest {

    private static final double GENAU = 0.0001;

    @Test
    void leeresDiagrammLiefertStandardgroesse() {
        Diagram d = new Diagram("class", "Leer");

        ExportBereich b = ExportBereich.berechnen(d);

        assertEquals(0, b.x(), GENAU);
        assertEquals(0, b.y(), GENAU);
        assertEquals(ExportBereich.LEER_BREITE, b.breite(), GENAU);
        assertEquals(ExportBereich.LEER_HOEHE, b.hoehe(), GENAU);
    }

    @Test
    void einElementMitRand() {
        Diagram d = new Diagram("class", "Test");
        d.addNode(new Node("class", "A", 100, 50)); // Standardgröße 100 x 60

        ExportBereich b = ExportBereich.berechnen(d, 20);

        assertEquals(80, b.x(), GENAU);
        assertEquals(30, b.y(), GENAU);
        assertEquals(140, b.breite(), GENAU);
        assertEquals(100, b.hoehe(), GENAU);
    }

    @Test
    void mehrereElementeWerdenUmfasst() {
        Diagram d = new Diagram("class", "Test");
        d.addNode(new Node("class", "A", 100, 50));  // bis 200 / 110
        d.addNode(new Node("class", "B", 400, 300)); // bis 500 / 360

        ExportBereich b = ExportBereich.berechnen(d, 10);

        assertEquals(90, b.x(), GENAU);
        assertEquals(40, b.y(), GENAU);
        assertEquals(420, b.breite(), GENAU);  // 500 - 100 + 2 * 10
        assertEquals(330, b.hoehe(), GENAU);   // 360 - 50 + 2 * 10
    }

    @Test
    void standardRandIstZwanzig() {
        Diagram d = new Diagram("class", "Test");
        d.addNode(new Node("class", "A", 0, 0));

        ExportBereich b = ExportBereich.berechnen(d);

        assertEquals(-20, b.x(), GENAU);
        assertEquals(-20, b.y(), GENAU);
        assertEquals(140, b.breite(), GENAU);
        assertEquals(100, b.hoehe(), GENAU);
    }

    @Test
    void negativerRandWirdAbgelehnt() {
        Diagram d = new Diagram("class", "Test");
        assertThrows(IllegalArgumentException.class,
                () -> ExportBereich.berechnen(d, -1));
    }
}