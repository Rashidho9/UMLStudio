package com.rashid.umlstudio.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ein Diagramm mit Elementen und Verbindungen, z. B. ein Klassendiagramm.
 */
public class Diagram {

    private String id;
    private String type;
    private String name;
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    /** Leerer Konstruktor für Gson. */
    public Diagram() {
    }

    /**
     * Erzeugt ein leeres Diagramm.
     *
     * @param type Diagrammtyp, z. B. "class"
     * @param name angezeigter Name (nicht leer)
     */
    public Diagram(String type, String name) {
        this.id = UUID.randomUUID().toString();
        this.type = Node.pruefeText(type, "Diagrammtyp");
        this.name = Node.pruefeText(name, "Diagrammname");
    }

    /** Fügt ein Element hinzu. */
    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Element darf nicht null sein.");
        }
        nodes.add(node);
    }

    /**
     * Fügt eine Verbindung hinzu. Quelle und Ziel müssen im Diagramm existieren.
     *
     * @throws IllegalArgumentException wenn Quelle oder Ziel fehlt
     */
    public void addEdge(Edge edge) {
        if (node(edge.getSourceId()).isEmpty() || node(edge.getTargetId()).isEmpty()) {
            throw new IllegalArgumentException("Quelle und Ziel müssen im Diagramm liegen.");
        }
        edges.add(edge);
    }

    /**
     * Entfernt ein Element samt allen enthaltenen Elementen und allen
     * Verbindungen, die eines davon berühren. So bleiben keine kaputten Verweise.
     */
    public void removeNode(String nodeId) {
        List<String> weg = descendants(nodeId);
        weg.add(nodeId);
        edges.removeIf(e -> weg.stream().anyMatch(e::beruehrt));
        nodes.removeIf(n -> weg.contains(n.getId()));
    }

    /** Sucht ein Element über seine ID. */
    public Optional<Node> node(String nodeId) {
        return nodes.stream().filter(n -> n.getId().equals(nodeId)).findFirst();
    }

    /** Liefert die direkten Kinder eines Elements. */
    public List<Node> childrenOf(String nodeId) {
        List<Node> kinder = new ArrayList<>();
        for (Node n : nodes) {
            if (nodeId.equals(n.getParentId())) {
                kinder.add(n);
            }
        }
        return kinder;
    }

    /** Liefert die IDs aller Nachfahren (Kinder, Enkel, ...). */
    public List<String> descendants(String nodeId) {
        List<String> ergebnis = new ArrayList<>();
        for (Node kind : childrenOf(nodeId)) {
            ergebnis.add(kind.getId());
            ergebnis.addAll(descendants(kind.getId()));
        }
        return ergebnis;
    }

    /** Verschachtelungstiefe: 0 = liegt direkt auf der Fläche. */
    public int depth(String nodeId) {
        int tiefe = 0;
        Optional<Node> aktuell = node(nodeId);
        while (aktuell.isPresent() && aktuell.get().getParentId() != null) {
            tiefe++;
            aktuell = node(aktuell.get().getParentId());
        }
        return tiefe;
    }

    /** Liefert alle Verbindungen, die eines der Elemente berühren. */
    public List<Edge> edgesOf(Collection<String> nodeIds) {
        List<Edge> ergebnis = new ArrayList<>();
        for (Edge e : edges) {
            if (nodeIds.stream().anyMatch(e::beruehrt)) {
                ergebnis.add(e);
            }
        }
        return ergebnis;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }

    public void setName(String name) {
        this.name = Node.pruefeText(name, "Diagrammname");
    }

    @Override
    public String toString() {
        return name + " (" + type + "): " + nodes.size() + " Elemente, "
                + edges.size() + " Verbindungen";
    }
}