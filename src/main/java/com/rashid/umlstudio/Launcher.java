package com.rashid.umlstudio;

import javafx.application.Application;

/**
 * Startklasse von UMLStudio.
 * Sie erbt bewusst NICHT von Application. So startet JavaFX auch direkt
 * aus Eclipse (Run As → Java Application) ohne Modul-Einstellungen.
 */
public final class Launcher {

    private Launcher() {
        // Keine Objekte nötig – nur main().
    }

    /**
     * Einstiegspunkt des Programms.
     *
     * @param args Kommandozeilenargumente (werden aktuell nicht genutzt)
     */
    public static void main(String[] args) {
        Application.launch(UmlStudioApp.class, args);
    }
}