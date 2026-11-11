package com.rashid.umlstudio.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Verwaltet die Undo- und Redo-Stapel.
 * Eine neue Änderung verwirft den bisherigen Redo-Zweig.
 */
public final class CommandHistory {

    private final Deque<Command> rueckgaengig = new ArrayDeque<>();
    private final Deque<Command> wiederholen = new ArrayDeque<>();

    /** Führt einen neuen Befehl aus und merkt ihn für Undo. */
    public void ausfuehren(Command befehl) {
        befehl.ausfuehren();
        rueckgaengig.push(befehl);
        wiederholen.clear();
    }

    /**
     * Merkt einen bereits ausgeführten Befehl, ohne ihn erneut auszuführen.
     * Das benötigen wir beispielsweise nach einer sichtbaren Mausbewegung.
     */
    public void aufnehmen(Command befehl) {
        rueckgaengig.push(befehl);
        wiederholen.clear();
    }

    public boolean kannRueckgaengig() {
        return !rueckgaengig.isEmpty();
    }

    public boolean kannWiederholen() {
        return !wiederholen.isEmpty();
    }

    public int undoAnzahl() {
        return rueckgaengig.size();
    }

    public int redoAnzahl() {
        return wiederholen.size();
    }

    /** Macht den letzten Befehl rückgängig, sofern vorhanden. */
    public void rueckgaengig() {
        if (rueckgaengig.isEmpty()) {
            return;
        }
        Command befehl = rueckgaengig.pop();
        befehl.rueckgaengig();
        wiederholen.push(befehl);
    }

    /** Führt den zuletzt zurückgenommenen Befehl erneut aus. */
    public void wiederholen() {
        if (wiederholen.isEmpty()) {
            return;
        }
        Command befehl = wiederholen.pop();
        befehl.ausfuehren();
        rueckgaengig.push(befehl);
    }
}