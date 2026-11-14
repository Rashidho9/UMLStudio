package com.rashid.umlstudio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Ein Element auf der Zeichenfläche, z. B. eine Klasse oder ein Paket.
 * Position und Größe werden in Pixeln gespeichert.
 * Ein Element kann in einem anderen liegen; der Verweis darauf ist die parentId.
 */
public class Node {

    private String id;
    private String kind;
    private double x;
    private double y;
    private double w = 100.0;
    private double h = 60.0;
    private String name = "";
    private String parentId;
    private Map<String, Object> props = new HashMap<>();

    /** Leerer Konstruktor, wird später von Gson zum Laden benötigt. */
    public Node() {
    }

    /**
     * Erzeugt ein neues Element mit zufälliger ID.
     *
     * @param kind Elementart, z. B. "class" (nicht leer)
     * @param name angezeigter Name
     * @param x    linke Kante
     * @param y    obere Kante
     */
    public Node(String kind, String name, double x, double y) {
        this.id = UUID.randomUUID().toString();
        this.kind = pruefeText(kind, "Elementart");
        this.name = name == null ? "" : name.trim();
        this.x = x;
        this.y = y;
    }

    /** Liefert das Rechteck als Feld {x, y, w, h}. */
    public double[] rect() {
        return new double[] { x, y, w, h };
    }

    /** Liefert den Mittelpunkt als Feld {x, y}. */
    public double[] center() {
        return new double[] { x + w / 2, y + h / 2 };
    }

    /** Verschiebt das Element um dx und dy. */
    public void verschieben(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /**
     * Ändert die Größe.
     *
     * @throws IllegalArgumentException wenn Breite oder Höhe nicht größer als 0 ist
     */
    public void setGroesse(double w, double h) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Breite und Höhe müssen größer als 0 sein.");
        }
        this.w = w;
        this.h = h;
    }

    public String getId() { return id; }
    public String getKind() { return kind; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getW() { return w; }
    public double getH() { return h; }
    public String getName() { return name; }
    public String getParentId() { return parentId; }
    
    /**
     * Liefert die freien Eigenschaften, z. B. Attribute und Methoden.
     * GEÄNDERT Schritt 11: Steht in einer Datei "props": null, wird eine
     * leere Tabelle angelegt, damit nie null zurückkommt.
     */
    public Map<String, Object> getProps() {
        if (props == null) {
            props = new HashMap<>();
        }
        return props;
    }

    /** Setzt den Namen; null wird zu einem leeren Text. */
    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    /** Setzt das umgebende Element (null = liegt direkt auf der Fläche). */
    public void setParentId(String parentId) {
        if (id != null && id.equals(parentId)) {
            throw new IllegalArgumentException("Ein Element kann nicht in sich selbst liegen.");
        }
        this.parentId = parentId;
    }

    static String pruefeText(String wert, String feldname) {
        if (wert == null || wert.trim().isEmpty()) {
            throw new IllegalArgumentException(feldname + " darf nicht leer sein.");
        }
        return wert.trim();
    }

    /** Zwei Elemente sind gleich, wenn ihre ID gleich ist. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        return Objects.equals(id, ((Node) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return kind + " \"" + name + "\" @(" + x + ", " + y + ")";
    }
}