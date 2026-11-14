package com.rashid.umlstudio.view;

import java.util.List;

import com.rashid.umlstudio.controller.DocumentController;
import com.rashid.umlstudio.export.ExportBereich;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.KlassenInhalt;
import com.rashid.umlstudio.model.KlassenLayout;
import com.rashid.umlstudio.model.Node;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Zeichenfläche für ein Diagramm.
 *
 * Verschieben und Verbinden laufen über den Controller, damit Undo und Redo
 * möglich sind. Ein vollständiger Mauszug zählt als ein Änderungsschritt.
 *
 * Verbinden funktioniert per Shift, rechter Maustaste oder Verbinden-Modus.
 *
 * GEÄNDERT Schritt 11: Klassen zeigen Attribute und Methoden in drei Abschnitten.
 */
public class DiagramView extends Pane {

    private static final Color AUSWAHL = Color.web("#1e88e5");
    private static final double PFEIL = 14;

    private final Canvas canvas = new Canvas();
    private final DocumentController controller;
    private final Diagram diagram;

    private Node ausgewaehlt;
    private Node verbindungsStart;
    private double letzteX;
    private double letzteY;
    private double startX;
    private double startY;
    private String verbindungsart = "association";
    private boolean verbindenModus = false;

    /** Erstellt die Zeichenfläche für das Diagramm des Controllers. */
    public DiagramView(DocumentController controller) {
        this.controller = controller;
        this.diagram = controller.getDiagram();
        getChildren().add(canvas);

        // Die Canvas-Größe folgt der Größe dieses Pane.
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(o -> zeichnen());
        canvas.heightProperty().addListener(o -> zeichnen());

        canvas.setOnMousePressed(this::mausGedrueckt);
        canvas.setOnMouseDragged(this::mausGezogen);
        canvas.setOnMouseReleased(this::mausLosgelassen);
    }

    /** Bestimmt die Art neu erstellter Verbindungen. */
    public void setVerbindungsart(String art) {
        this.verbindungsart = art;
    }

    /** Aktiviert oder deaktiviert das Verbinden ohne Tastatur. */
    public void setVerbindenModus(boolean an) {
        this.verbindenModus = an;
    }

    public Node getAusgewaehlt() {
        return ausgewaehlt;
    }

    /**
     * Übernimmt die Auswahl aus dem Controller und zeichnet neu.
     * Ruft den Controller nicht erneut auf, damit keine Rückkopplung entsteht.
     */
    public void aktualisieren() {
        ausgewaehlt = controller.getAusgewaehlt().orElse(null);
        zeichnen();
    }

    // ------------------------------------------------------------------ Maus

    private void mausGedrueckt(MouseEvent e) {
        Node n = Geometrie.treffer(diagram, e.getX(), e.getY());
        boolean verbinden = e.isShiftDown()
                || e.isSecondaryButtonDown() || verbindenModus;
        verbindungsStart = (n != null && verbinden) ? n : null;

        startX = e.getX();
        startY = e.getY();
        letzteX = e.getX();
        letzteY = e.getY();

        controller.auswaehlen(n);
        if (n != null && !verbinden) {
            controller.zugBeginnen(n);
        }
        zeichnen();
    }

    private void mausGezogen(MouseEvent e) {
        letzteX = e.getX();
        letzteY = e.getY();

        if (verbindungsStart == null && controller.getAusgewaehlt().isPresent()) {
            // Der Abstand bezieht sich immer auf den Anfang des gesamten Zugs.
            controller.zugAktualisieren(e.getX() - startX, e.getY() - startY);
        }
        zeichnen();
    }

    private void mausLosgelassen(MouseEvent e) {
        if (verbindungsStart != null) {
            Node ziel = Geometrie.treffer(diagram, e.getX(), e.getY());
            controller.verbindungHinzufuegen(
                    verbindungsart, verbindungsStart, ziel);
            verbindungsStart = null;
        } else {
            controller.zugAbschliessen();
        }

        letzteX = e.getX();
        letzteY = e.getY();
        zeichnen();
    }

    // -------------------------------------------------------------- Zeichnen

    /** Zeichnet alle Elemente, Verbindungen und die Verbindungsvorschau. */
    public void zeichnen() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        inhaltZeichnen(g);

        // Vorschau während des Verbindens (nur auf dem Bildschirm).
        if (verbindungsStart != null) {
            double[] m = Geometrie.mitte(verbindungsStart);
            g.setStroke(AUSWAHL);
            g.setLineDashes(4);
            g.strokeLine(m[0], m[1], letzteX, letzteY);
            g.setLineDashes();
        }
    }

    /**
     * Zeichnet nur den Diagramminhalt: erst die Elemente (Eltern vor
     * Kindern), danach die Verbindungen. Wird für Bildschirm und Export genutzt.
     */
    private void inhaltZeichnen(GraphicsContext g) {
        List<Node> sortiert = diagram.getNodes().stream()
                .sorted((a, b) -> Integer.compare(
                        Geometrie.tiefe(diagram, a),
                        Geometrie.tiefe(diagram, b)))
                .toList();

        for (Node n : sortiert) {
            zeichneElement(g, n);
        }
        for (Edge kante : diagram.getEdges()) {
            zeichneVerbindung(g, kante);
        }
    }

    /**
     * Zeichnet den Exportbereich auf eine unsichtbare Canvas und liefert
     * das Ergebnis als Bild. Die Auswahl wird dabei nicht markiert.
     *
     * Muss im JavaFX-Thread aufgerufen werden (z. B. aus einem Menü).
     *
     * @param bereich Ausschnitt des Diagramms, siehe {@link ExportBereich}
     * @param skala   Vergrößerung, z. B. 2 für ein schärferes Bild
     * @return das fertige Bild
     */
    public WritableImage alsBild(ExportBereich bereich, double skala) {
        if (skala <= 0) {
            throw new IllegalArgumentException("Die Skala muss größer als 0 sein.");
        }
        int breite = (int) Math.ceil(bereich.breite() * skala);
        int hoehe = (int) Math.ceil(bereich.hoehe() * skala);

        Canvas export = new Canvas(breite, hoehe);
        GraphicsContext g = export.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, breite, hoehe);

        // Vergrößern und so verschieben, dass der Bereich bei (0, 0) beginnt.
        g.save();
        g.scale(skala, skala);
        g.translate(-bereich.x(), -bereich.y());

        // Auswahl kurz ausblenden, damit sie nicht blau im Bild erscheint.
        Node gemerkt = ausgewaehlt;
        ausgewaehlt = null;
        try {
            inhaltZeichnen(g);
        } finally {
            ausgewaehlt = gemerkt;
            g.restore();
        }

        SnapshotParameters parameter = new SnapshotParameters();
        parameter.setFill(Color.WHITE);
        return export.snapshot(parameter, null);
    }

    /**
     * Liest alle Pixel eines Bildes als ARGB-Zahlen aus,
     * Zeile für Zeile. Passt direkt zu PngExporter.speichern(...).
     */
    public static int[] pixel(Image bild) {
        int breite = (int) bild.getWidth();
        int hoehe = (int) bild.getHeight();
        int[] argb = new int[breite * hoehe];
        bild.getPixelReader().getPixels(0, 0, breite, hoehe,
                WritablePixelFormat.getIntArgbInstance(), argb, 0, breite);
        return argb;
    }

    /** Zeichnet ein Element passend zu seiner Art. */
    private void zeichneElement(GraphicsContext g, Node n) {
        double x = n.getX();
        double y = n.getY();
        double w = n.getW();
        double h = n.getH();

        g.setLineWidth(n == ausgewaehlt ? 2.5 : 1.2);
        g.setStroke(n == ausgewaehlt ? AUSWAHL : Color.BLACK);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);

        switch (n.getKind()) {
            case "package" -> {
                // Paket mit Reiter und Rumpf.
                g.setFill(Color.web("#f5f5f5"));
                g.fillRect(x, y, w * 0.4, 20);
                g.strokeRect(x, y, w * 0.4, 20);
                g.fillRect(x, y + 20, w, h - 20);
                g.strokeRect(x, y + 20, w, h - 20);
                text(g, n.getName(), x + w * 0.2, y + 10, true);
            }
            case "note" -> {
                // Notiz mit umgeknickter Ecke.
                double e = 12;
                double[] px = { x, x + w - e, x + w, x + w, x };
                double[] py = { y, y, y + e, y + h, y + h };
                g.setFill(Color.web("#fff9c4"));
                g.fillPolygon(px, py, 5);
                g.strokePolygon(px, py, 5);
                g.strokeLine(x + w - e, y, x + w - e, y + e);
                g.strokeLine(x + w - e, y + e, x + w, y + e);
                text(g, n.getName(), x + w / 2, y + h / 2, false);
            }
            default -> {
                // Klasse, Interface oder Enum mit drei Abschnitten (Schritt 11).
                g.setFill(Color.web("#e3f2fd"));
                g.fillRect(x, y, w, h);
                g.strokeRect(x, y, w, h);

                String stereotyp = KlassenLayout.stereotyp(n.getKind());
                if (stereotyp != null) {
                    text(g, stereotyp, x + w / 2, y + 12, false);
                    text(g, n.getName(), x + w / 2, y + 28, true);
                } else {
                    text(g, n.getName(), x + w / 2, y + 14, true);
                }

                KlassenLayout layout = KlassenLayout.fuer(n);
                double attributeOben = layout.attributeOben(y);
                double methodenOben = layout.methodenOben(y);
                g.strokeLine(x, attributeOben, x + w, attributeOben);
                g.strokeLine(x, methodenOben, x + w, methodenOben);

                zeilen(g, KlassenInhalt.attribute(n), x, attributeOben);
                zeilen(g, KlassenInhalt.methoden(n), x, methodenOben);
            }
        }
    }

    /** NEU Schritt 11: Zeichnet Einträge linksbündig untereinander. */
    private void zeilen(GraphicsContext g, List<String> eintraege,
            double x, double abschnittOben) {
        g.setTextAlign(TextAlignment.LEFT);
        for (int i = 0; i < eintraege.size(); i++) {
            text(g, eintraege.get(i), x + KlassenLayout.TEXT_EINZUG,
                    KlassenLayout.zeileY(abschnittOben, i), false);
        }
        g.setTextAlign(TextAlignment.CENTER);
    }

    /** Zeichnet eine Beschriftung (zentriert, außer bei Klasseneinträgen). */
    private void text(GraphicsContext g, String s,
            double x, double y, boolean fett) {
        g.setFill(Color.BLACK);
        g.setFont(Font.font("Segoe UI",
                fett ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        g.fillText(s == null ? "" : s, x, y);
    }

    /** Zeichnet eine Verbindung mit der zugehörigen UML-Pfeilspitze. */
    private void zeichneVerbindung(GraphicsContext g, Edge kante) {
        Node quelle = diagram.node(kante.getSourceId()).orElse(null);
        Node ziel = diagram.node(kante.getTargetId()).orElse(null);
        if (quelle == null || ziel == null) {
            return;
        }

        double[] mq = Geometrie.mitte(quelle);
        double[] mz = Geometrie.mitte(ziel);
        double[] a = Geometrie.randpunkt(quelle, mz[0], mz[1]);
        double[] b = Geometrie.randpunkt(ziel, mq[0], mq[1]);

        String art = kante.getKind();
        boolean gestrichelt = art.equals("realization")
                || art.equals("dependency") || art.equals("note_link");

        g.setStroke(Color.BLACK);
        g.setLineWidth(1.2);
        if (gestrichelt) {
            g.setLineDashes(6, 4);
        } else {
            g.setLineDashes();
        }
        g.strokeLine(a[0], a[1], b[0], b[1]);
        g.setLineDashes();

        switch (art) {
            case "generalization", "realization" -> dreieck(g, a, b);
            case "dependency" -> offenerPfeil(g, a, b);
            case "aggregation" -> raute(g, b, a, false);
            case "composition" -> raute(g, b, a, true);
            default -> {
                // Assoziationen und Notizverbindungen ohne Pfeilspitze.
            }
        }

        if (kante.getName() != null && !kante.getName().isBlank()) {
            text(g, kante.getName(),
                    (a[0] + b[0]) / 2,
                    (a[1] + b[1]) / 2 - 10, false);
        }
    }

    /** Zeichnet ein hohles Dreieck mit der Spitze bei b. */
    private void dreieck(GraphicsContext g, double[] a, double[] b) {
        double winkel = Math.atan2(b[1] - a[1], b[0] - a[0]);
        double[] px = {
                b[0],
                b[0] - PFEIL * Math.cos(winkel - 0.45),
                b[0] - PFEIL * Math.cos(winkel + 0.45)
        };
        double[] py = {
                b[1],
                b[1] - PFEIL * Math.sin(winkel - 0.45),
                b[1] - PFEIL * Math.sin(winkel + 0.45)
        };
        g.setFill(Color.WHITE);
        g.fillPolygon(px, py, 3);
        g.strokePolygon(px, py, 3);
    }

    /** Zeichnet eine offene Pfeilspitze bei b. */
    private void offenerPfeil(GraphicsContext g, double[] a, double[] b) {
        double winkel = Math.atan2(b[1] - a[1], b[0] - a[0]);
        g.strokeLine(b[0], b[1],
                b[0] - PFEIL * Math.cos(winkel - 0.45),
                b[1] - PFEIL * Math.sin(winkel - 0.45));
        g.strokeLine(b[0], b[1],
                b[0] - PFEIL * Math.cos(winkel + 0.45),
                b[1] - PFEIL * Math.sin(winkel + 0.45));
    }

    /** Zeichnet eine hohle oder gefüllte Raute an der angegebenen Spitze. */
    private void raute(GraphicsContext g,
            double[] von, double[] spitze, boolean gefuellt) {
        double winkel = Math.atan2(
                spitze[1] - von[1], spitze[0] - von[0]);
        double laenge = PFEIL;
        double breite = 6;

        double mx = spitze[0] - laenge * Math.cos(winkel);
        double my = spitze[1] - laenge * Math.sin(winkel);
        double ex = spitze[0] - 2 * laenge * Math.cos(winkel);
        double ey = spitze[1] - 2 * laenge * Math.sin(winkel);
        double nx = -Math.sin(winkel) * breite;
        double ny = Math.cos(winkel) * breite;

        double[] px = { spitze[0], mx + nx, ex, mx - nx };
        double[] py = { spitze[1], my + ny, ey, my - ny };
        g.setFill(gefuellt ? Color.BLACK : Color.WHITE);
        g.fillPolygon(px, py, 4);
        g.strokePolygon(px, py, 4);
    }
}