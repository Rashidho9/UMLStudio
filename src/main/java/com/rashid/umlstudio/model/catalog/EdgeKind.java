package com.rashid.umlstudio.model.catalog;

/**
 * Beschreibt eine Verbindungsart, z. B. "Vererbung".
 *
 * @param id    technischer Schlüssel, wird in Edge.kind gespeichert
 * @param label deutscher Anzeigename für die Palette
 */
public record EdgeKind(String id, String label) {

    public EdgeKind {
        if (id == null || id.isBlank() || label == null || label.isBlank()) {
            throw new IllegalArgumentException("ID und Anzeigename dürfen nicht leer sein.");
        }
    }
}