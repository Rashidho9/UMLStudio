package com.rashid.umlstudio.export;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * Schreibt ein Bild als PNG-Datei.
 *
 * Das Bild wird als Pixelfeld übergeben: Jeder Eintrag ist eine Farbe
 * im ARGB-Format (Alpha, Rot, Grün, Blau), Zeile für Zeile von oben
 * links nach unten rechts. Die Klasse braucht kein JavaFX.
 */
public final class PngExporter {

    /** Dateiendung für PNG-Dateien. */
    public static final String ENDUNG = ".png";

    private PngExporter() {
    }

    /**
     * Speichert die Pixel als PNG-Datei.
     *
     * @param breite Bildbreite in Pixeln (größer als 0)
     * @param hoehe  Bildhöhe in Pixeln (größer als 0)
     * @param argb   Pixelfarben, Länge genau breite * hoehe
     * @param ziel   gewünschte Datei; ".png" wird ergänzt, wenn es fehlt
     * @return der tatsächliche Pfad (mit Endung .png)
     * @throws IllegalArgumentException wenn Größe oder Pixelanzahl nicht passen
     * @throws IOException              wenn die Datei nicht geschrieben werden kann
     */
    public static Path speichern(int breite, int hoehe, int[] argb, Path ziel)
            throws IOException {
        BufferedImage bild = alsBild(breite, hoehe, argb);
        Path datei = SvgExporter.mitEndung(ziel, ENDUNG);

        // ImageIO gibt false zurück, wenn kein PNG-Schreiber gefunden wird.
        try (var aus = Files.newOutputStream(datei)) {
            if (!ImageIO.write(bild, "png", aus)) {
                throw new IOException("Kein PNG-Schreiber verfügbar.");
            }
        }
        return datei;
    }

    /**
     * Baut aus dem Pixelfeld ein BufferedImage.
     *
     * @throws IllegalArgumentException wenn Größe oder Pixelanzahl nicht passen
     */
    static BufferedImage alsBild(int breite, int hoehe, int[] argb) {
        if (breite <= 0 || hoehe <= 0) {
            throw new IllegalArgumentException(
                    "Breite und Höhe müssen größer als 0 sein.");
        }
        if (argb == null || argb.length != breite * hoehe) {
            throw new IllegalArgumentException(
                    "Die Pixelanzahl passt nicht zur Bildgröße.");
        }
        BufferedImage bild = new BufferedImage(
                breite, hoehe, BufferedImage.TYPE_INT_ARGB);
        // Alle Pixel auf einmal setzen (schneller als einzeln).
        bild.setRGB(0, 0, breite, hoehe, argb, 0, breite);
        return bild;
    }
}