package com.rashid.umlstudio.model.validation;

/**
 * Ein Befund der Modellprüfung.
 *
 * @param code stabile Kennung des Problems
 * @param elementId betroffene Element- oder Verbindungs-ID, sonst null
 * @param text verständliche Beschreibung
 */
public record Pruefmeldung(String code, String elementId, String text) {

    public Pruefmeldung {
        if (code == null || code.isBlank()
                || text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Kennung und Text dürfen nicht leer sein.");
        }
    }
}