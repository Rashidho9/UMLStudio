package com.rashid.umlstudio.model.catalog;

/**
 * Beschreibt eine Elementart, z. B. "Klasse" oder "Paket".
 *
 * @param id         technischer Schlüssel, wird in Node.kind gespeichert
 * @param label      deutscher Anzeigename für die Palette
 * @param breite     Standardbreite in Pixeln
 * @param hoehe      Standardhöhe in Pixeln
 * @param container  true, wenn andere Elemente darin liegen dürfen
 */
public record NodeKind(String id, String label, double breite, double hoehe, boolean container) {

    /** Prüft die Angaben direkt beim Erzeugen. */
    public NodeKind {
        if (id == null || id.isBlank() || label == null || label.isBlank()) {
            throw new IllegalArgumentException("ID und Anzeigename dürfen nicht leer sein.");
        }
        if (breite <= 0 || hoehe <= 0) {
            throw new IllegalArgumentException("Größe muss größer als 0 sein.");
        }
    }
}