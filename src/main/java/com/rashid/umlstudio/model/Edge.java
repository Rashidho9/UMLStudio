package com.rashid.umlstudio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Eine Verbindung zwischen zwei Elementen, z. B. Assoziation oder Vererbung.
 * Quelle und Ziel werden als ID-Text gespeichert, nicht als Objektverweis.
 */
public class Edge {

    private String id;
    private String kind;
    private String sourceId;
    private String targetId;
    private String name = "";
    private Map<String, Object> props = new HashMap<>();

    /** Leerer Konstruktor für Gson. */
    public Edge() {
    }

    /**
     * Erzeugt eine neue Verbindung mit zufälliger ID.
     *
     * @param kind     Verbindungsart, z. B. "association"
     * @param sourceId ID des Startelements
     * @param targetId ID des Zielelements
     */
    public Edge(String kind, String sourceId, String targetId) {
        this.id = UUID.randomUUID().toString();
        this.kind = Node.pruefeText(kind, "Verbindungsart");
        this.sourceId = Node.pruefeText(sourceId, "Quelle");
        this.targetId = Node.pruefeText(targetId, "Ziel");
    }

    /** Liefert true, wenn die Verbindung das Element mit dieser ID berührt. */
    public boolean beruehrt(String nodeId) {
        return sourceId.equals(nodeId) || targetId.equals(nodeId);
    }

    public String getId() { return id; }
    public String getKind() { return kind; }
    public String getSourceId() { return sourceId; }
    public String getTargetId() { return targetId; }
    public String getName() { return name; }
    public Map<String, Object> getProps() { return props; }

    /** Setzt die Beschriftung; null wird zu einem leeren Text. */
    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        return Objects.equals(id, ((Edge) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return kind + ": " + sourceId + " -> " + targetId;
    }
}