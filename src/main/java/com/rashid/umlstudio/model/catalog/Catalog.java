package com.rashid.umlstudio.model.catalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;

/**
 * Zentraler Katalog aller Diagrammtypen.
 * Neue Typen werden nur hier in registriereStandardtypen() eingetragen.
 */
public final class Catalog {

    /** Schlüssel des Klassendiagramms. */
    public static final String KLASSENDIAGRAMM = "class";

    // LinkedHashMap behält die Eintragsreihenfolge (wichtig für Menüs).
    private static final Map<String, DiagramType> TYPEN = new LinkedHashMap<>();

    static {
        registriereStandardtypen();
    }

    private Catalog() {
        // Nur statische Methoden.
    }

    /** Trägt alle mitgelieferten Diagrammtypen ein. */
    private static void registriereStandardtypen() {
        registriere(new DiagramType(KLASSENDIAGRAMM, "Klassendiagramm",
                List.of(
                        new NodeKind("class", "Klasse", 160, 100, false),
                        new NodeKind("interface", "Schnittstelle", 160, 80, false),
                        new NodeKind("enum", "Aufzählung", 140, 80, false),
                        new NodeKind("package", "Paket", 300, 200, true),
                        new NodeKind("note", "Notiz", 140, 60, false)),
                List.of(
                        new EdgeKind("association", "Assoziation"),
                        new EdgeKind("aggregation", "Aggregation"),
                        new EdgeKind("composition", "Komposition"),
                        new EdgeKind("generalization", "Vererbung"),
                        new EdgeKind("realization", "Realisierung"),
                        new EdgeKind("dependency", "Abhängigkeit"),
                        new EdgeKind("note_link", "Notizverbindung"))));
    }

    private static void registriere(DiagramType typ) {
        if (TYPEN.containsKey(typ.id())) {
            throw new IllegalStateException("Diagrammtyp doppelt: " + typ.id());
        }
        TYPEN.put(typ.id(), typ);
    }

    /** Liefert alle Diagrammtypen in fester Reihenfolge (nur lesbar). */
    public static List<DiagramType> alleTypen() {
        return Collections.unmodifiableList(List.copyOf(TYPEN.values()));
    }

    /** Sucht einen Diagrammtyp über seine ID. */
    public static Optional<DiagramType> typ(String id) {
        return Optional.ofNullable(TYPEN.get(id));
    }

    /** Sucht eine Elementart innerhalb eines Diagrammtyps. */
    public static Optional<NodeKind> elementart(String diagrammTyp, String nodeKindId) {
        return typ(diagrammTyp).flatMap(t -> t.nodeKinds().stream()
                .filter(k -> k.id().equals(nodeKindId))
                .findFirst());
    }

    /**
     * Erzeugt ein leeres Diagramm eines bekannten Typs.
     *
     * @throws IllegalArgumentException wenn der Typ unbekannt ist
     */
    public static Diagram neuesDiagramm(String diagrammTyp, String name) {
        pruefeTyp(diagrammTyp);
        return new Diagram(diagrammTyp, name);
    }

    /**
     * Erzeugt ein Element mit der Standardgröße aus dem Katalog.
     *
     * @throws IllegalArgumentException wenn die Elementart im Diagrammtyp nicht erlaubt ist
     */
    public static Node neuesElement(String diagrammTyp, String nodeKindId, String name, double x, double y) {
        NodeKind art = elementart(diagrammTyp, nodeKindId).orElseThrow(() ->
                new IllegalArgumentException("Element '" + nodeKindId
                        + "' ist im Diagrammtyp '" + diagrammTyp + "' nicht erlaubt."));
        Node node = new Node(art.id(), name, x, y);
        node.setGroesse(art.breite(), art.hoehe());
        return node;
    }

    private static void pruefeTyp(String diagrammTyp) {
        if (!TYPEN.containsKey(diagrammTyp)) {
            throw new IllegalArgumentException("Unbekannter Diagrammtyp: " + diagrammTyp);
        }
    }
}