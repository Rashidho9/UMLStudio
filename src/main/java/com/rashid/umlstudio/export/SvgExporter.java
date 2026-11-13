package com.rashid.umlstudio.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.view.Geometrie;

/**
 * Wandelt ein Diagramm in eine SVG-Datei (Vektorgrafik) um.
 *
 * Die Formen entsprechen der Zeichenfläche: Klassen, Interfaces, Enums,
 * Pakete, Notizen und alle UML-Pfeilspitzen. Eine Auswahl wird nicht
 * markiert, weil sie nicht zum Diagramm gehört.
 */
public final class SvgExporter {

    /** Dateiendung für SVG-Dateien. */
    public static final String ENDUNG = ".svg";

    private static final double PFEIL = 14;
    private static final String SCHRIFT = "Segoe UI, Arial, sans-serif";

    private SvgExporter() {
    }

    /**
     * Speichert das Diagramm als SVG-Datei in UTF-8.
     *
     * @return der tatsächliche Pfad (mit Endung .svg)
     * @throws IOException wenn die Datei nicht geschrieben werden kann
     */
    public static Path speichern(Diagram diagram, Path ziel) throws IOException {
        Path datei = mitEndung(ziel, ENDUNG);
        Files.writeString(datei, alsSvg(diagram), StandardCharsets.UTF_8);
        return datei;
    }

    /** Liefert das komplette SVG-Dokument als Text. */
    public static String alsSvg(Diagram diagram) {
        ExportBereich b = ExportBereich.berechnen(diagram);
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\"")
          .append(" width=\"").append(zahl(b.breite())).append('"')
          .append(" height=\"").append(zahl(b.hoehe())).append('"')
          .append(" viewBox=\"0 0 ").append(zahl(b.breite())).append(' ')
          .append(zahl(b.hoehe())).append("\">\n");
        sb.append("  <rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");

        // Alles so verschieben, dass der Bereich bei (0, 0) beginnt.
        sb.append("  <g transform=\"translate(").append(zahl(-b.x())).append(' ')
          .append(zahl(-b.y())).append(")\" stroke-width=\"1.2\">\n");

        // Eltern vor Kindern zeichnen, wie in der DiagramView.
        List<Node> sortiert = diagram.getNodes().stream()
                .sorted(Comparator.comparingInt(n -> Geometrie.tiefe(diagram, n)))
                .toList();
        for (Node n : sortiert) {
            element(sb, n);
        }
        for (Edge e : diagram.getEdges()) {
            verbindung(sb, diagram, e);
        }

        sb.append("  </g>\n</svg>\n");
        return sb.toString();
    }

    // ------------------------------------------------------------ Elemente

    private static void element(StringBuilder sb, Node n) {
        double x = n.getX();
        double y = n.getY();
        double w = n.getW();
        double h = n.getH();

        switch (n.getKind()) {
            case "package" -> {
                rechteck(sb, x, y, w * 0.4, 20, "#f5f5f5");
                rechteck(sb, x, y + 20, w, h - 20, "#f5f5f5");
                text(sb, n.getName(), x + w * 0.2, y + 10, true);
            }
            case "note" -> {
                double e = 12;
                double[] px = { x, x + w - e, x + w, x + w, x };
                double[] py = { y, y, y + e, y + h, y + h };
                polygon(sb, px, py, "#fff9c4");
                linie(sb, x + w - e, y, x + w - e, y + e, false);
                linie(sb, x + w - e, y + e, x + w, y + e, false);
                text(sb, n.getName(), x + w / 2, y + h / 2, false);
            }
            default -> {
                rechteck(sb, x, y, w, h, "#e3f2fd");
                String stereotyp = switch (n.getKind()) {
                    case "interface" -> "«interface»";
                    case "enum" -> "«enumeration»";
                    default -> null;
                };
                double kopf = stereotyp == null ? 28 : 40;
                if (stereotyp != null) {
                    text(sb, stereotyp, x + w / 2, y + 12, false);
                    text(sb, n.getName(), x + w / 2, y + 28, true);
                } else {
                    text(sb, n.getName(), x + w / 2, y + 14, true);
                }
                linie(sb, x, y + kopf, x + w, y + kopf, false);
                double rest = (h - kopf) / 2;
                linie(sb, x, y + kopf + rest, x + w, y + kopf + rest, false);
            }
        }
    }

    // ------------------------------------------------------------ Verbindungen

    private static void verbindung(StringBuilder sb, Diagram d, Edge kante) {
        Node quelle = d.node(kante.getSourceId()).orElse(null);
        Node ziel = d.node(kante.getTargetId()).orElse(null);
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
        linie(sb, a[0], a[1], b[0], b[1], gestrichelt);

        switch (art) {
            case "generalization", "realization" -> dreieck(sb, a, b);
            case "dependency" -> offenerPfeil(sb, a, b);
            case "aggregation" -> raute(sb, b, a, false);
            case "composition" -> raute(sb, b, a, true);
            default -> {
                // Assoziation und Notizverbindung ohne Pfeilspitze.
            }
        }

        if (kante.getName() != null && !kante.getName().isBlank()) {
            text(sb, kante.getName(), (a[0] + b[0]) / 2,
                    (a[1] + b[1]) / 2 - 10, false);
        }
    }

    /** Hohles Dreieck mit der Spitze bei b (Vererbung, Realisierung). */
    private static void dreieck(StringBuilder sb, double[] a, double[] b) {
        double w = Math.atan2(b[1] - a[1], b[0] - a[0]);
        double[] px = { b[0], b[0] - PFEIL * Math.cos(w - 0.45),
                b[0] - PFEIL * Math.cos(w + 0.45) };
        double[] py = { b[1], b[1] - PFEIL * Math.sin(w - 0.45),
                b[1] - PFEIL * Math.sin(w + 0.45) };
        polygon(sb, px, py, "#ffffff");
    }

    /** Offene Pfeilspitze bei b (Abhängigkeit). */
    private static void offenerPfeil(StringBuilder sb, double[] a, double[] b) {
        double w = Math.atan2(b[1] - a[1], b[0] - a[0]);
        linie(sb, b[0], b[1], b[0] - PFEIL * Math.cos(w - 0.45),
                b[1] - PFEIL * Math.sin(w - 0.45), false);
        linie(sb, b[0], b[1], b[0] - PFEIL * Math.cos(w + 0.45),
                b[1] - PFEIL * Math.sin(w + 0.45), false);
    }

    /** Hohle (Aggregation) oder gefüllte (Komposition) Raute an der Spitze. */
    private static void raute(StringBuilder sb, double[] von, double[] spitze,
            boolean gefuellt) {
        double w = Math.atan2(spitze[1] - von[1], spitze[0] - von[0]);
        double mx = spitze[0] - PFEIL * Math.cos(w);
        double my = spitze[1] - PFEIL * Math.sin(w);
        double ex = spitze[0] - 2 * PFEIL * Math.cos(w);
        double ey = spitze[1] - 2 * PFEIL * Math.sin(w);
        double nx = -Math.sin(w) * 6;
        double ny = Math.cos(w) * 6;
        double[] px = { spitze[0], mx + nx, ex, mx - nx };
        double[] py = { spitze[1], my + ny, ey, my - ny };
        polygon(sb, px, py, gefuellt ? "#000000" : "#ffffff");
    }

    // ------------------------------------------------------------ SVG-Bausteine

    private static void rechteck(StringBuilder sb, double x, double y,
            double w, double h, String fuellung) {
        sb.append("    <rect x=\"").append(zahl(x)).append("\" y=\"").append(zahl(y))
          .append("\" width=\"").append(zahl(w)).append("\" height=\"").append(zahl(h))
          .append("\" fill=\"").append(fuellung).append("\" stroke=\"#000000\"/>\n");
    }

    private static void linie(StringBuilder sb, double x1, double y1,
            double x2, double y2, boolean gestrichelt) {
        sb.append("    <line x1=\"").append(zahl(x1)).append("\" y1=\"").append(zahl(y1))
          .append("\" x2=\"").append(zahl(x2)).append("\" y2=\"").append(zahl(y2))
          .append("\" stroke=\"#000000\"");
        if (gestrichelt) {
            sb.append(" stroke-dasharray=\"6 4\"");
        }
        sb.append("/>\n");
    }

    private static void polygon(StringBuilder sb, double[] px, double[] py,
            String fuellung) {
        sb.append("    <polygon points=\"");
        for (int i = 0; i < px.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(zahl(px[i])).append(',').append(zahl(py[i]));
        }
        sb.append("\" fill=\"").append(fuellung).append("\" stroke=\"#000000\"/>\n");
    }

    /** Zentrierter Text; der Mittelpunkt liegt bei (x, y). */
    private static void text(StringBuilder sb, String s, double x, double y,
            boolean fett) {
        sb.append("    <text x=\"").append(zahl(x)).append("\" y=\"").append(zahl(y))
          .append("\" font-family=\"").append(SCHRIFT).append("\" font-size=\"12\"")
          .append(" text-anchor=\"middle\" dominant-baseline=\"central\"")
          .append(" fill=\"#000000\" stroke=\"none\"");
        if (fett) {
            sb.append(" font-weight=\"bold\"");
        }
        sb.append('>').append(escape(s)).append("</text>\n");
    }

    // ------------------------------------------------------------ Hilfen

    /** Zahl mit Punkt als Dezimaltrenner (SVG verlangt "12.5", nicht "12,5"). */
    static String zahl(double wert) {
        return String.format(Locale.ROOT, "%.1f", wert);
    }

    /** Ersetzt XML-Sonderzeichen, damit Namen wie "A<B>" die Datei nicht zerstören. */
    static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /** Hängt die Endung an, wenn sie fehlt (Groß-/Kleinschreibung egal). */
    static Path mitEndung(Path ziel, String endung) {
        String name = ziel.getFileName().toString();
        if (name.toLowerCase(Locale.ROOT).endsWith(endung)) {
            return ziel;
        }
        return ziel.resolveSibling(name + endung);
    }
}