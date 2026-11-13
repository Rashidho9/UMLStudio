package com.rashid.umlstudio.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.rashid.umlstudio.model.Project;

/**
 * Automatische Sicherung des aktuellen Projekts.
 * Die Datei liegt standardmäßig unter Benutzerordner/.umlstudio/autosave.umls.
 * Nutzt intern den sicheren ProjectStore (Temp-Datei, dann ersetzen).
 */
public final class Autosave {

    /** Dateiname der automatischen Sicherung. */
    public static final String DATEINAME = "autosave" + ProjectStore.ENDUNG;

    private final Path datei;

    /**
     * @param ordner Ordner für die Sicherung; wird beim Speichern bei Bedarf angelegt
     */
    public Autosave(Path ordner) {
        if (ordner == null) {
            throw new IllegalArgumentException("Ordner darf nicht null sein.");
        }
        this.datei = ordner.resolve(DATEINAME);
    }

    /** Sicherung im Benutzerordner, z. B. C:\Users\rashi\.umlstudio. */
    public static Autosave standard() {
        return new Autosave(Path.of(System.getProperty("user.home"), ".umlstudio"));
    }

    /** Pfad der Sicherungsdatei. */
    public Path getDatei() {
        return datei;
    }

    /** Schreibt das Projekt in die Sicherungsdatei. */
    public void speichern(Project projekt) throws ProjektDateiException {
        ProjectStore.speichern(projekt, datei);
    }

    /** true, wenn eine Sicherung vom letzten Mal existiert. */
    public boolean vorhanden() {
        return Files.isRegularFile(datei);
    }

    /** Lädt die Sicherung und prüft sie wie eine normale Projektdatei. */
    public Project laden() throws ProjektDateiException {
        return ProjectStore.laden(datei);
    }

    /** Entfernt die Sicherung; ein Fehler dabei ist nicht kritisch. */
    public void loeschen() {
        try {
            Files.deleteIfExists(datei);
        } catch (IOException e) {
            System.err.println("Autosave konnte nicht gelöscht werden: " + e.getMessage());
        }
    }
}