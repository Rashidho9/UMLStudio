package com.rashid.umlstudio.export;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;

/**
 * Rechteck, das alle Elemente eines Diagramms plus einen Rand umfasst.
 *
 * Wird beim PNG- und SVG-Export genutzt, damit nur der wirklich
 * benutzte Bereich exportiert wird und keine große weiße Fläche.
 *
 * @param x      linke Kante des Bereichs
 * @param y      obere Kante des Bereichs
 * @param breite Breite des Bereichs (größer als 0)
 * @param hoehe  Höhe des Bereichs (größer als 0)
 */
public record ExportBereich(double x, double y, double breite, double hoehe) {

    /** Standardrand um die Elemente in Pixeln. */
    public static final double STANDARD_RAND = 20;

    /** Größe für ein leeres Diagramm, damit trotzdem ein Bild entsteht. */
    public static final double LEER_BREITE = 200;
    public static final double LEER_HOEHE = 100;

    /** Prüft die Angaben direkt beim Erzeugen. */
    public ExportBereich {
        if (breite <= 0 || hoehe <= 0) {
            throw new IllegalArgumentException(
                    "Breite und Höhe müssen größer als 0 sein.");
        }
    }

    /** Berechnet den Bereich mit dem Standardrand von 20 Pixeln. */
    public static ExportBereich berechnen(Diagram diagram) {
        return berechnen(diagram, STANDARD_RAND);
    }

    /**
     * Berechnet das kleinste Rechteck um alle Elemente und
     * vergrößert es an jeder Seite um den Rand.
     *
     * @param diagram das zu exportierende Diagramm
     * @param rand    Abstand an jeder Seite (nicht negativ)
     * @return der Exportbereich; bei leerem Diagramm 200 x 100 ab (0, 0)
     * @throws IllegalArgumentException wenn der Rand negativ ist
     */
    public static ExportBereich berechnen(Diagram diagram, double rand) {
        if (rand < 0) {
            throw new IllegalArgumentException("Der Rand darf nicht negativ sein.");
        }
        if (diagram.getNodes().isEmpty()) {
            return new ExportBereich(0, 0, LEER_BREITE, LEER_HOEHE);
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Node n : diagram.getNodes()) {
            minX = Math.min(minX, n.getX());
            minY = Math.min(minY, n.getY());
            maxX = Math.max(maxX, n.getX() + n.getW());
            maxY = Math.max(maxY, n.getY() + n.getH());
        }

        return new ExportBereich(
                minX - rand,
                minY - rand,
                (maxX - minX) + 2 * rand,
                (maxY - minY) + 2 * rand);
    }
}