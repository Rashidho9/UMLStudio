package com.rashid.umlstudio.persistence;

/**
 * Fehler beim Speichern oder Laden einer .umls-Datei.
 * Die Meldung ist auf Deutsch und kann direkt im Programm angezeigt werden.
 */
public class ProjektDateiException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProjektDateiException(String meldung) {
        super(meldung);
    }

    public ProjektDateiException(String meldung, Throwable ursache) {
        super(meldung, ursache);
    }
}