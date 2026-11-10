package com.rashid.umlstudio.view;

import java.util.List;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Zeichenfläche für ein Diagramm.
 * Maus ziehen verschiebt, Shift + ziehen verbindet zwei Elemente.
 */
public class DiagramView extends Pane {

    private static final Color AUSWAHL = Color.web("#1e88e5");
    private static final double PFEIL = 14;   // Länge der Pfeilspitze in Pixel

    private final Canvas canvas = new Canvas();
    private Diagram diagram;

    private Node ausgewaehlt;          // aktuell markiertes Element
    private Node verbindungsStart;     // Start beim Verbinden (Shift)
    private double letzteX, letzteY;   // letzte Mausposition
    private String verbindungsart = "association";
    private boolean verbindenModus = false;   // true = Ziehen erzeugt Verbindung

    public DiagramView(Diagram diagram) {
        this.diagram = diagram;
        getChildren().add(canvas);

        // Canvas wächst mit dem Fenster mit.
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(o -> zeichnen());
        canvas.heightProperty().addListener(o -> zeichnen());

        canvas.setOnMousePressed(this::mausGedrueckt);
        canvas.setOnMouseDragged(this::mausGezogen);
        canvas.setOnMouseReleased(this::mausLosgelassen);
    }

    /** Legt fest, welche Verbindungsart beim Verbinden entsteht. */
    public void setVerbindungsart(String art) {
        this.verbindungsart = art;
    }
    
    /** Schaltet den Verbinden-Modus ein oder aus (Knopf in der Werkzeugleiste). */
    public void setVerbindenModus(boolean an) {
        this.verbindenModus = an;
    }

    public Node getAusgewaehlt() {
        return ausgewaehlt;
    }

    // ------------------------------------------------------------------ Maus

    private void mausGedrueckt(MouseEvent e) {
        Node n = Geometrie.treffer(diagram, e.getX(), e.getY());
        ausgewaehlt = n;
        // Verbinden: Shift, rechte Maustaste oder Verbinden-Modus
        boolean verbinden = e.isShiftDown() || e.isSecondaryButtonDown() || verbindenModus;
        verbindungsStart = (n != null && verbinden) ? n : null;
        letzteX = e.getX();
        letzteY = e.getY();
        zeichnen();
    }

    private void mausGezogen(MouseEvent e) {
        if (verbindungsStart == null && ausgewaehlt != null) {
            double dx = e.getX() - letzteX;
            double dy = e.getY() - letzteY;
            ausgewaehlt.verschieben(dx, dy);
            for (Node kind : Geometrie.nachkommen(diagram, ausgewaehlt)) {
                kind.verschieben(dx, dy);      // Paketinhalt wandert mit
            }
        }
        letzteX = e.getX();
        letzteY = e.getY();
        zeichnen();
    }

    private void mausLosgelassen(MouseEvent e) {
        if (verbindungsStart != null) {
            Node ziel = Geometrie.treffer(diagram, e.getX(), e.getY());
            if (ziel != null && ziel != verbindungsStart) {
                diagram.addEdge(new Edge(verbindungsart, verbindungsStart.getId(), ziel.getId()));
            }
            verbindungsStart = null;
        }
        zeichnen();
    }

    // -------------------------------------------------------------- Zeichnen

    /** Zeichnet das ganze Diagramm neu. */
    public void zeichnen() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Zuerst Elemente nach Tiefe (Pakete hinten), dann die Linien darüber.
        List<Node> sortiert = diagram.getNodes().stream()
                .sorted((a, b) -> Integer.compare(Geometrie.tiefe(diagram, a), Geometrie.tiefe(diagram, b)))
                .toList();
        for (Node n : sortiert) {
            zeichneElement(g, n);
        }
        for (Edge kante : diagram.getEdges()) {
            zeichneVerbindung(g, kante);
        }

        // Gummiband-Linie während des Verbindens
        if (verbindungsStart != null) {
            double[] m = Geometrie.mitte(verbindungsStart);
            g.setStroke(AUSWAHL);
            g.setLineDashes(4);
            g.strokeLine(m[0], m[1], letzteX, letzteY);
            g.setLineDashes(null);
        }
    }

    private void zeichneElement(GraphicsContext g, Node n) {
        double x = n.getX(), y = n.getY(), w = n.getW(), h = n.getH();
        g.setLineWidth(n == ausgewaehlt ? 2.5 : 1.2);
        g.setStroke(n == ausgewaehlt ? AUSWAHL : Color.BLACK);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);

        switch (n.getKind()) {
            case "package" -> {
                // Reiter oben links + Rumpf
                g.setFill(Color.web("#f5f5f5"));
                g.fillRect(x, y, w * 0.4, 20);
                g.strokeRect(x, y, w * 0.4, 20);
                g.fillRect(x, y + 20, w, h - 20);
                g.strokeRect(x, y + 20, w, h - 20);
                text(g, n.getName(), x + w * 0.2, y + 10, true);
            }
            case "note" -> {
                // Rechteck mit umgeknickter Ecke
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
                // class, interface, enum: Kopf + zwei leere Abschnitte (Attribute/Methoden)
                g.setFill(Color.web("#e3f2fd"));
                g.fillRect(x, y, w, h);
                g.strokeRect(x, y, w, h);
                String stereotyp = switch (n.getKind()) {
                    case "interface" -> "«interface»";
                    case "enum" -> "«enumeration»";
                    default -> null;
                };
                double kopf = stereotyp == null ? 28 : 40;
                if (stereotyp != null) {
                    text(g, stereotyp, x + w / 2, y + 12, false);
                    text(g, n.getName(), x + w / 2, y + 28, true);
                } else {
                    text(g, n.getName(), x + w / 2, y + 14, true);
                }
                g.strokeLine(x, y + kopf, x + w, y + kopf);
                double rest = (h - kopf) / 2;
                g.strokeLine(x, y + kopf + rest, x + w, y + kopf + rest);
            }
        }
    }

    private void text(GraphicsContext g, String s, double x, double y, boolean fett) {
        g.setFill(Color.BLACK);
        g.setFont(Font.font("Segoe UI", fett ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        g.fillText(s == null ? "" : s, x, y);
    }

    private void zeichneVerbindung(GraphicsContext g, Edge kante) {
        Node quelle = diagram.node(kante.getSourceId()).orElse(null);
        Node ziel = diagram.node(kante.getTargetId()).orElse(null);
        if (quelle == null || ziel == null) {
            return;
        }
        double[] mq = Geometrie.mitte(quelle);
        double[] mz = Geometrie.mitte(ziel);
        double[] a = Geometrie.randpunkt(quelle, mz[0], mz[1]);   // Start am Rand
        double[] b = Geometrie.randpunkt(ziel, mq[0], mq[1]);     // Ende am Rand

        String art = kante.getKind();
        boolean gestrichelt = art.equals("realization") || art.equals("dependency") || art.equals("note_link");

        g.setStroke(Color.BLACK);
        g.setLineWidth(1.2);
        g.setLineDashes(gestrichelt ? new double[] { 6, 4 } : null);
        g.strokeLine(a[0], a[1], b[0], b[1]);
        g.setLineDashes(null);

        switch (art) {
            case "generalization", "realization" -> dreieck(g, a, b);   // hohles Dreieck am Ziel
            case "dependency" -> offenerPfeil(g, a, b);                 // offener Pfeil am Ziel
            case "aggregation" -> raute(g, b, a, false);                // hohle Raute am Ganzen (Quelle)
            case "composition" -> raute(g, b, a, true);                 // gefüllte Raute an der Quelle
            default -> { /* association, note_link: keine Spitze */ }
        }

        if (kante.getName() != null && !kante.getName().isBlank()) {
            text(g, kante.getName(), (a[0] + b[0]) / 2, (a[1] + b[1]) / 2 - 10, false);
        }
    }

    /** Hohles Dreieck mit Spitze bei b. */
    private void dreieck(GraphicsContext g, double[] a, double[] b) {
        double winkel = Math.atan2(b[1] - a[1], b[0] - a[0]);
        double[] px = { b[0], b[0] - PFEIL * Math.cos(winkel - 0.45), b[0] - PFEIL * Math.cos(winkel + 0.45) };
        double[] py = { b[1], b[1] - PFEIL * Math.sin(winkel - 0.45), b[1] - PFEIL * Math.sin(winkel + 0.45) };
        g.setFill(Color.WHITE);
        g.fillPolygon(px, py, 3);
        g.strokePolygon(px, py, 3);
    }

    /** Offener Pfeil (zwei Striche) mit Spitze bei b. */
    private void offenerPfeil(GraphicsContext g, double[] a, double[] b) {
        double winkel = Math.atan2(b[1] - a[1], b[0] - a[0]);
        g.strokeLine(b[0], b[1], b[0] - PFEIL * Math.cos(winkel - 0.45), b[1] - PFEIL * Math.sin(winkel - 0.45));
        g.strokeLine(b[0], b[1], b[0] - PFEIL * Math.cos(winkel + 0.45), b[1] - PFEIL * Math.sin(winkel + 0.45));
    }

    /** Raute mit Spitze bei spitze, Richtung kommt von von. */
    private void raute(GraphicsContext g, double[] von, double[] spitze, boolean gefuellt) {
        double winkel = Math.atan2(spitze[1] - von[1], spitze[0] - von[0]);
        double l = PFEIL, br = 6;
        double mx = spitze[0] - l * Math.cos(winkel), my = spitze[1] - l * Math.sin(winkel);
        double ex = spitze[0] - 2 * l * Math.cos(winkel), ey = spitze[1] - 2 * l * Math.sin(winkel);
        double nx = -Math.sin(winkel) * br, ny = Math.cos(winkel) * br;
        double[] px = { spitze[0], mx + nx, ex, mx - nx };
        double[] py = { spitze[1], my + ny, ey, my - ny };
        g.setFill(gefuellt ? Color.BLACK : Color.WHITE);
        g.fillPolygon(px, py, 4);
        g.strokePolygon(px, py, 4);
    }
}