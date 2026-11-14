package com.rashid.umlstudio.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NEU Schritt 11: Attribute und Methoden einer Klasse.
 *
 * Die Einträge liegen als Liste von UML-Zeilen in {@link Node#getProps()},
 * z. B. "- name: String" unter "attributes" und "+ getName(): String"
 * unter "operations". Alte Dateien ohne diese Einträge ergeben einfach
 * leere Listen. Die Klasse kennt kein JavaFX und ist daher gut testbar.
 */
public final class KlassenInhalt {

    /** Schlüssel der Attributliste in den Eigenschaften (props). */
    public static final String ATTRIBUTE = "attributes";

    /** Schlüssel der Methodenliste in den Eigenschaften (props). */
    public static final String METHODEN = "operations";

    /** Maximale Länge einer einzelnen Zeile. */
    public static final int MAX_LAENGE = 120;

    /** Elementarten, die Attribute und Methoden haben dürfen. */
    private static final Set<String> ARTEN = Set.of("class", "interface", "enum");

    /** Optionale Sichtbarkeit (+ - # ~), optional "/" (abgeleitet), dann der Name. */
    private static final Pattern NAME = Pattern.compile(
            "^\\s*[+\\-#~]?\\s*/?\\s*([\\p{L}_$][\\p{L}\\p{N}_$]*)");

    /** Name direkt gefolgt von runden Klammern, z. B. "+ getName(): String". */
    private static final Pattern METHODE = Pattern.compile(
            "^\\s*[+\\-#~]?\\s*([\\p{L}_$][\\p{L}\\p{N}_$]*)\\s*\\(.*\\).*$");

    private KlassenInhalt() {
    }

    /** Liefert true für Klasse, Schnittstelle und Aufzählung. */
    public static boolean unterstuetzt(String kind) {
        return kind != null && ARTEN.contains(kind);
    }

    /** Liefert true, wenn das Element Attribute und Methoden haben darf. */
    public static boolean unterstuetzt(Node n) {
        return n != null && unterstuetzt(n.getKind());
    }

    /** Liefert die Attribute als unveränderliche Liste (nie null). */
    public static List<String> attribute(Node n) {
        return lesen(n, ATTRIBUTE);
    }

    /** Liefert die Methoden als unveränderliche Liste (nie null). */
    public static List<String> methoden(Node n) {
        return lesen(n, METHODEN);
    }

    /**
     * Speichert Attribute und Methoden im Element.
     * Leere Listen entfernen den Eintrag, damit die Datei schlank bleibt.
     */
    public static void setzen(Node n, List<String> attribute, List<String> methoden) {
        if (n == null) {
            throw new IllegalArgumentException("Element darf nicht null sein.");
        }
        schreiben(n, ATTRIBUTE, attribute);
        schreiben(n, METHODEN, methoden);
    }

    /**
     * Zerlegt einen mehrzeiligen Text in einzelne Einträge.
     * Leerzeichen am Rand werden entfernt, leere Zeilen übersprungen.
     */
    public static List<String> zeilen(String text) {
        if (text == null) {
            return List.of();
        }
        List<String> ergebnis = new ArrayList<>();
        for (String zeile : text.split("\\R")) {
            String sauber = zeile.trim();
            if (!sauber.isEmpty()) {
                ergebnis.add(sauber);
            }
        }
        return List.copyOf(ergebnis);
    }

    /** Verbindet Einträge zu einem Text mit einer Zeile pro Eintrag. */
    public static String alsText(List<String> zeilen) {
        return zeilen == null ? "" : String.join("\n", zeilen);
    }

    /**
     * Prüft alle Zeilen, bevor etwas geändert wird.
     *
     * @return Fehlermeldung mit Zeilennummer oder null, wenn alles passt
     */
    public static String pruefe(List<String> attribute, List<String> methoden) {
        if (attribute != null) {
            for (int i = 0; i < attribute.size(); i++) {
                String fehler = pruefeAttribut(attribute.get(i));
                if (fehler != null) {
                    return "Attribut " + (i + 1) + ": " + fehler;
                }
            }
        }
        if (methoden != null) {
            for (int i = 0; i < methoden.size(); i++) {
                String fehler = pruefeMethode(methoden.get(i));
                if (fehler != null) {
                    return "Methode " + (i + 1) + ": " + fehler;
                }
            }
        }
        return null;
    }

    /**
     * Prüft eine Attributzeile, z. B. "- name: String".
     *
     * @return Fehlermeldung oder null
     */
    public static String pruefeAttribut(String zeile) {
        String fehler = pruefeGrundlagen(zeile);
        if (fehler != null) {
            return fehler;
        }
        if (METHODE.matcher(zeile).matches()) {
            return "'" + zeile.trim() + "' sieht wie eine Methode aus. "
                    + "Bitte unter Methoden eintragen.";
        }
        if (attributName(zeile) == null) {
            return "'" + zeile.trim() + "' braucht einen Namen, z. B. - name: String";
        }
        return null;
    }

    /**
     * Prüft eine Methodenzeile, z. B. "+ getName(): String".
     *
     * @return Fehlermeldung oder null
     */
    public static String pruefeMethode(String zeile) {
        String fehler = pruefeGrundlagen(zeile);
        if (fehler != null) {
            return fehler;
        }
        if (!METHODE.matcher(zeile).matches()) {
            return "'" + zeile.trim() + "' braucht einen Namen mit Klammern, "
                    + "z. B. + getName(): String";
        }
        return null;
    }

    /**
     * Liest den Namen aus einer Attributzeile: "- name: String" ergibt "name".
     *
     * @return der Name oder null, wenn keiner erkennbar ist
     */
    public static String attributName(String zeile) {
        if (zeile == null) {
            return null;
        }
        Matcher treffer = NAME.matcher(zeile);
        return treffer.find() ? treffer.group(1) : null;
    }

    /** Liefert alle Attributnamen, die im Element mehrfach vorkommen. */
    public static List<String> doppelteAttribute(Node n) {
        Set<String> gesehen = new HashSet<>();
        Set<String> doppelt = new LinkedHashSet<>();
        for (String zeile : attribute(n)) {
            String name = attributName(zeile);
            if (name != null && !gesehen.add(name)) {
                doppelt.add(name);
            }
        }
        return List.copyOf(doppelt);
    }
    
    /**
     * NEU: Liest die Signatur einer Methodenzeile, also Name und Parametertypen.
     * "+ bezahlen(betrag: double): void" ergibt "bezahlen(double)".
     * Rückgabetyp und Parameternamen zählen nicht, wie in Java.
     *
     * @return die Signatur oder null, wenn die Zeile keine Methode ist
     */
    public static String methodenSignatur(String zeile) {
        if (zeile == null) {
            return null;
        }
        Matcher treffer = METHODE.matcher(zeile);
        if (!treffer.matches()) {
            return null;
        }
        String name = treffer.group(1);
        int auf = zeile.indexOf('(');
        int zu = zeile.lastIndexOf(')');
        String innen = zeile.substring(auf + 1, zu).trim();

        List<String> typen = new ArrayList<>();
        if (!innen.isEmpty()) {
            for (String parameter : innen.split(",")) {
                // "betrag: double" -> "double"; ohne Doppelpunkt zählt der ganze Text.
                int doppelpunkt = parameter.indexOf(':');
                String typ = doppelpunkt >= 0
                        ? parameter.substring(doppelpunkt + 1)
                        : parameter;
                typen.add(typ.trim().replaceAll("\\s+", " "));
            }
        }
        return name + "(" + String.join(", ", typen) + ")";
    }

    /** NEU: Liefert alle Methodensignaturen, die im Element mehrfach vorkommen. */
    public static List<String> doppelteMethoden(Node n) {
        Set<String> gesehen = new HashSet<>();
        Set<String> doppelt = new LinkedHashSet<>();
        for (String zeile : methoden(n)) {
            String signatur = methodenSignatur(zeile);
            if (signatur != null && !gesehen.add(signatur)) {
                doppelt.add(signatur);
            }
        }
        return List.copyOf(doppelt);
    }

    // ------------------------------------------------------------ intern

    private static String pruefeGrundlagen(String zeile) {
        if (zeile == null || zeile.isBlank()) {
            return "Die Zeile ist leer.";
        }
        if (zeile.trim().length() > MAX_LAENGE) {
            return "Die Zeile ist länger als " + MAX_LAENGE + " Zeichen.";
        }
        return null;
    }

    /**
     * Liest eine Liste aus props. Nach dem Laden mit Gson ist der Wert eine
     * Liste von Objekten; ein einzelner Text wird zeilenweise aufgeteilt.
     */
    private static List<String> lesen(Node n, String schluessel) {
        if (n == null) {
            return List.of();
        }
        Object wert = n.getProps().get(schluessel);
        if (wert instanceof List<?> liste) {
            List<String> ergebnis = new ArrayList<>();
            for (Object eintrag : liste) {
                if (eintrag != null) {
                    String sauber = String.valueOf(eintrag).trim();
                    if (!sauber.isEmpty()) {
                        ergebnis.add(sauber);
                    }
                }
            }
            return List.copyOf(ergebnis);
        }
        if (wert instanceof String text) {
            return zeilen(text);
        }
        return List.of();
    }

    private static void schreiben(Node n, String schluessel, List<String> zeilen) {
        List<String> sauber = new ArrayList<>();
        if (zeilen != null) {
            for (String zeile : zeilen) {
                if (zeile != null && !zeile.isBlank()) {
                    sauber.add(zeile.trim());
                }
            }
        }
        if (sauber.isEmpty()) {
            n.getProps().remove(schluessel);
        } else {
            n.getProps().put(schluessel, sauber);
        }
    }
}