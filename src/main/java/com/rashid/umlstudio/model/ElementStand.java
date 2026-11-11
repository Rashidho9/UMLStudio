package com.rashid.umlstudio.model;

import java.util.Objects;

/**
 * Speichert Name, Position und Größe eines Elements für Undo und Redo.
 * Beim Wiederherstellen werden Kinder nicht automatisch mitverschoben,
 * weil jedes betroffene Element seine eigene Momentaufnahme erhält.
 */
public final class ElementStand {

    private final String id;
    private final String name;
    private final double x;
    private final double y;
    private final double w;
    private final double h;

    public ElementStand(Node n) {
        id = n.getId();
        name = n.getName();
        x = n.getX();
        y = n.getY();
        w = n.getW();
        h = n.getH();
    }

    public String getId() {
        return id;
    }

    /** Stellt die gespeicherten Werte am vorhandenen Element wieder her. */
    public void wiederherstellen(Diagram diagram) {
        diagram.node(id).ifPresent(n -> {
            n.setName(name);
            n.verschieben(x - n.getX(), y - n.getY());
            n.setGroesse(w, h);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ElementStand s)) {
            return false;
        }
        return Objects.equals(id, s.id)
                && Objects.equals(name, s.name)
                && Double.compare(x, s.x) == 0
                && Double.compare(y, s.y) == 0
                && Double.compare(w, s.w) == 0
                && Double.compare(h, s.h) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, x, y, w, h);
    }
}