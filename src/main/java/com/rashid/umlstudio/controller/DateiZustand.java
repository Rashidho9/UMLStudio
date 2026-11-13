package com.rashid.umlstudio.controller;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Merkt sich die aktuelle Datei und die Version beim letzten Speichern.
 * Ohne JavaFX, damit es mit JUnit testbar ist.
 */
public final class DateiZustand {

    /** Wert, der garantiert keiner echten Version entspricht. */
    private static final long UNGESPEICHERT = -1;

    private Path datei;
    private long gespeicherteVersion;

    /** Startzustand: neues Projekt ohne Datei und ohne Änderungen. */
    public DateiZustand() {
        neu(0);
    }

    /** Neues, noch nie gespeichertes Projekt ohne Änderungen. */
    public void neu(long version) {
        datei = null;
        gespeicherteVersion = version;
    }

    /** Nach erfolgreichem Speichern oder Öffnen. */
    public void gespeichert(Path datei, long version) {
        this.datei = datei;
        this.gespeicherteVersion = version;
    }

    /** Z. B. nach dem Wiederherstellen: Inhalt ist noch in keiner Datei gesichert. */
    public void alsUngespeichertMarkieren() {
        gespeicherteVersion = UNGESPEICHERT;
    }

    /** true, wenn sich seit dem letzten Speichern etwas geändert hat. */
    public boolean istGeaendert(long aktuelleVersion) {
        return aktuelleVersion != gespeicherteVersion;
    }

    /** Aktuelle Datei oder leer, wenn noch nie gespeichert wurde. */
    public Optional<Path> getDatei() {
        return Optional.ofNullable(datei);
    }

    /** Fenstertitel, z. B. "UMLStudio – projekt.umls *". */
    public String titel(String appName, long aktuelleVersion) {
        String name = datei == null ? "Unbenannt" : datei.getFileName().toString();
        return appName + " – " + name + (istGeaendert(aktuelleVersion) ? " *" : "");
    }
}