package com.rashid.umlstudio.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests für den PNG-Export. */
class PngExporterTest {

    private static final int ROT = 0xFFFF0000;
    private static final int GRUEN = 0xFF00FF00;
    private static final int BLAU = 0xFF0000FF;
    private static final int WEISS = 0xFFFFFFFF;

    @TempDir
    Path ordner;

    /** 2 x 2 Pixel: oben rot/grün, unten blau/weiß. */
    private int[] vierPixel() {
        return new int[] { ROT, GRUEN, BLAU, WEISS };
    }

    @Test
    void pngWirdGeschriebenUndHatRichtigeGroesse() throws IOException {
        Path datei = PngExporter.speichern(2, 2, vierPixel(), ordner.resolve("bild"));

        assertEquals("bild.png", datei.getFileName().toString());
        assertTrue(Files.size(datei) > 0);

        BufferedImage gelesen = ImageIO.read(datei.toFile());
        assertEquals(2, gelesen.getWidth());
        assertEquals(2, gelesen.getHeight());
    }

    @Test
    void farbenBleibenErhalten() throws IOException {
        Path datei = PngExporter.speichern(2, 2, vierPixel(), ordner.resolve("farben.png"));

        BufferedImage gelesen = ImageIO.read(datei.toFile());
        assertEquals(ROT, gelesen.getRGB(0, 0));
        assertEquals(GRUEN, gelesen.getRGB(1, 0));
        assertEquals(BLAU, gelesen.getRGB(0, 1));
        assertEquals(WEISS, gelesen.getRGB(1, 1));
    }

    @Test
    void falschePixelanzahlWirdAbgelehnt() {
        assertThrows(IllegalArgumentException.class,
                () -> PngExporter.speichern(2, 2, new int[3], ordner.resolve("x")));
    }

    @Test
    void nullGroesseWirdAbgelehnt() {
        assertThrows(IllegalArgumentException.class,
                () -> PngExporter.alsBild(0, 5, new int[0]));
    }

    @Test
    void vorhandeneEndungWirdNichtVerdoppelt() throws IOException {
        Path datei = PngExporter.speichern(2, 2, vierPixel(), ordner.resolve("Diagramm.PNG"));
        assertEquals("Diagramm.PNG", datei.getFileName().toString());
    }
}