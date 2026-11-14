package com.rashid.umlstudio.model;

/**
 * NEU Schritt 11: Aufteilung eines Klassenkastens in drei Abschnitte
 * (Name, Attribute, Methoden). Wird von Zeichenfläche, SVG-Export und
 * Controller gemeinsam genutzt, damit alles gleich aussieht.
 *
 * Reine Rechenlogik ohne JavaFX. Textbreiten werden geschätzt, weil
 * das Modell keine Schriftarten messen kann.
 *
 * @param kopf          Höhe des Namensabschnitts
 * @param attributHoehe Höhe des Attributabschnitts
 * @param methodenHoehe Höhe des Methodenabschnitts
 */
public record KlassenLayout(double kopf, double attributHoehe, double methodenHoehe) {

    /** Zeilenhöhe für Attribute und Methoden. */
    public static final double ZEILE = 16;
    /** Abstand oben und unten in einem Abschnitt mit Einträgen. */
    public static final double INNENRAND = 4;
    /** Mindesthöhe eines leeren Abschnitts. */
    public static final double LEERER_ABSCHNITT = 12;
    /** Linker und rechter Textabstand zum Rahmen. */
    public static final double TEXT_EINZUG = 6;
    /** Geschätzte Breite eines Zeichens bei Schriftgröße 12. */
    public static final double ZEICHEN_BREITE = 7;
    /** Kopfhöhe ohne Stereotyp (Klasse). */
    public static final double KOPF = 28;
    /** Kopfhöhe mit Stereotyp (Schnittstelle, Aufzählung). */
    public static final double KOPF_MIT_STEREOTYP = 40;

    /** Liefert «interface», «enumeration» oder null. */
    public static String stereotyp(String kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case "interface" -> "«interface»";
            case "enum" -> "«enumeration»";
            default -> null;
        };
    }

    /** Höhe des Namensabschnitts für die Elementart. */
    public static double kopfHoehe(String kind) {
        return stereotyp(kind) == null ? KOPF : KOPF_MIT_STEREOTYP;
    }

    /** Benötigte Höhe eines Abschnitts mit der angegebenen Zeilenzahl. */
    public static double abschnittHoehe(int anzahl) {
        return anzahl <= 0 ? LEERER_ABSCHNITT : anzahl * ZEILE + 2 * INNENRAND;
    }

    /**
     * Berechnet die Abschnitte für ein Element. Freier Platz wird
     * gleichmäßig auf Attribute und Methoden verteilt; eine leere
     * Klasse sieht daher genauso aus wie vor Schritt 11.
     */
    public static KlassenLayout fuer(Node n) {
        double kopf = kopfHoehe(n.getKind());
        double attribute = abschnittHoehe(KlassenInhalt.attribute(n).size());
        double methoden = abschnittHoehe(KlassenInhalt.methoden(n).size());
        double frei = n.getH() - kopf - attribute - methoden;
        if (frei > 0) {
            attribute += frei / 2;
            methoden += frei / 2;
        }
        return new KlassenLayout(kopf, attribute, methoden);
    }

    /** Kleinste Höhe, bei der alle Zeilen in den Kasten passen. */
    public static double mindestHoehe(Node n) {
        return kopfHoehe(n.getKind())
                + abschnittHoehe(KlassenInhalt.attribute(n).size())
                + abschnittHoehe(KlassenInhalt.methoden(n).size());
    }

    /** Geschätzte kleinste Breite für Name, Stereotyp und längste Zeile. */
    public static double mindestBreite(Node n) {
        int zeichen = laenge(n.getName());
        zeichen = Math.max(zeichen, laenge(stereotyp(n.getKind())));
        for (String zeile : KlassenInhalt.attribute(n)) {
            zeichen = Math.max(zeichen, laenge(zeile));
        }
        for (String zeile : KlassenInhalt.methoden(n)) {
            zeichen = Math.max(zeichen, laenge(zeile));
        }
        return Math.ceil(zeichen * ZEICHEN_BREITE + 2 * TEXT_EINZUG);
    }

    /**
     * Vergrößert das Element, wenn der Inhalt nicht hineinpasst.
     * Es wird nie verkleinert.
     *
     * @return true, wenn sich die Größe geändert hat
     */
    public static boolean groesseAnpassen(Node n) {
        double w = Math.max(n.getW(), mindestBreite(n));
        double h = Math.max(n.getH(), mindestHoehe(n));
        if (w == n.getW() && h == n.getH()) {
            return false;
        }
        n.setGroesse(w, h);
        return true;
    }

    /** Senkrechte Mitte der Zeile mit dem Index (0 = erste Zeile). */
    public static double zeileY(double abschnittOben, int index) {
        return abschnittOben + INNENRAND + index * ZEILE + ZEILE / 2;
    }

    /** Obere Kante des Attributabschnitts für ein Element mit Oberkante y. */
    public double attributeOben(double y) {
        return y + kopf;
    }

    /** Obere Kante des Methodenabschnitts für ein Element mit Oberkante y. */
    public double methodenOben(double y) {
        return y + kopf + attributHoehe;
    }

    private static int laenge(String s) {
        return s == null ? 0 : s.length();
    }
}