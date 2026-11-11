package com.rashid.umlstudio.command;

/** Ein Befehl mit einer Vorwärtsaktion und der zugehörigen Rückwärtsaktion. */
public final class Command {

    private final String beschreibung;
    private final Runnable vorwaerts;
    private final Runnable rueckwaerts;

    /**
     * @param beschreibung Bezeichnung des Befehls
     * @param vorwaerts Aktion für Ausführen und Wiederholen
     * @param rueckwaerts Aktion für Rückgängig
     */
    public Command(String beschreibung, Runnable vorwaerts, Runnable rueckwaerts) {
        this.beschreibung = beschreibung;
        this.vorwaerts = vorwaerts;
        this.rueckwaerts = rueckwaerts;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void ausfuehren() {
        vorwaerts.run();
    }

    public void rueckgaengig() {
        rueckwaerts.run();
    }
}