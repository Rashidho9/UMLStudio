package com.rashid.umlstudio.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ein UMLStudio-Projekt. Es enthält beliebig viele Diagramme in fester Reihenfolge.
 * Später wird es als .umls-Datei gespeichert.
 */
public class Project {

    private String name = "Neues Projekt";
    private List<Diagram> diagrams = new ArrayList<>();

    /** Erzeugt ein leeres Projekt mit dem Standardnamen. */
    public Project() {
    }

    /** Erzeugt ein leeres Projekt mit eigenem Namen. */
    public Project(String name) {
        setName(name);
    }

    /** Hängt ein Diagramm am Ende an. */
    public void addDiagram(Diagram diagram) {
        if (diagram == null) {
            throw new IllegalArgumentException("Diagramm darf nicht null sein.");
        }
        diagrams.add(diagram);
    }

    /** Entfernt ein Diagramm über seine ID; liefert true, wenn es existierte. */
    public boolean removeDiagram(String diagramId) {
        return diagrams.removeIf(d -> d.getId().equals(diagramId));
    }

    /** Sucht ein Diagramm über seine ID. */
    public Optional<Diagram> diagram(String diagramId) {
        return diagrams.stream().filter(d -> d.getId().equals(diagramId)).findFirst();
    }

    /** Position des Diagramms in der Liste, -1 wenn es fehlt. */
    public int indexOf(String diagramId) {
        for (int i = 0; i < diagrams.size(); i++) {
            if (diagrams.get(i).getId().equals(diagramId)) {
                return i;
            }
        }
        return -1;
    }

    public String getName() { return name; }
    public List<Diagram> getDiagrams() { return diagrams; }

    public void setName(String name) {
        this.name = Node.pruefeText(name, "Projektname");
    }

    @Override
    public String toString() {
        return name + " (" + diagrams.size() + " Diagramme)";
    }
}