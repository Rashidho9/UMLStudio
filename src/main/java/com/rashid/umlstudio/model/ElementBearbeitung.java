package com.rashid.umlstudio.model;

/**
 * Prüft Eingaben und übernimmt Änderungen ohne Abhängigkeit von JavaFX.
 * Beim Verschieben eines Pakets werden dessen Nachkommen mitgenommen.
 */
public final class ElementBearbeitung {

    private ElementBearbeitung() {
    }

    /**
     * Übernimmt Name, Position und Größe nach erfolgreicher Eingabeprüfung.
     *
     * @return Fehlermeldung oder null bei Erfolg
     */
    public static String anwenden(Diagram diagram, Node element,
            String name, String xText, String yText,
            String breiteText, String hoeheText) {
        if (diagram == null || element == null) {
            return "Kein Element ausgewählt.";
        }
        if (name == null || name.trim().isEmpty()) {
            return "Name darf nicht leer sein.";
        }

        String fehler = pruefeZahl(xText, "X");
        if (fehler != null) {
            return fehler;
        }
        fehler = pruefeZahl(yText, "Y");
        if (fehler != null) {
            return fehler;
        }
        fehler = pruefeZahl(breiteText, "Breite");
        if (fehler != null) {
            return fehler;
        }
        fehler = pruefeZahl(hoeheText, "Höhe");
        if (fehler != null) {
            return fehler;
        }

        double x = liesZahl(xText);
        double y = liesZahl(yText);
        double breite = liesZahl(breiteText);
        double hoehe = liesZahl(hoeheText);
        if (breite <= 0 || hoehe <= 0) {
            return "Breite und Höhe müssen größer als 0 sein.";
        }

        element.setName(name);
        verschiebenMitNachkommen(diagram, element,
                x - element.getX(), y - element.getY());
        element.setGroesse(breite, hoehe);
        return null;
    }

    /** Verschiebt ein Element und alle Kinder, Enkel und weiteren Nachkommen. */
    public static void verschiebenMitNachkommen(
            Diagram diagram, Node element, double dx, double dy) {
        element.verschieben(dx, dy);
        for (String id : diagram.descendants(element.getId())) {
            diagram.node(id).ifPresent(kind -> kind.verschieben(dx, dy));
        }
    }

    /** Prüft eine endliche Zahl; Punkt und Komma werden akzeptiert. */
    public static String pruefeZahl(String text, String feldname) {
        if (text == null || text.trim().isEmpty()) {
            return feldname + " darf nicht leer sein.";
        }
        try {
            if (!Double.isFinite(liesZahl(text))) {
                return feldname + " muss eine endliche Zahl sein.";
            }
            return null;
        } catch (NumberFormatException e) {
            return feldname + " ist keine gültige Zahl.";
        }
    }

    private static double liesZahl(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }
}