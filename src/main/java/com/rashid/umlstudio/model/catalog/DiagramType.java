package com.rashid.umlstudio.model.catalog;

import java.util.List;

/**
 * Beschreibt einen Diagrammtyp mit seinen erlaubten Elementen und Verbindungen.
 *
 * @param id        technischer Schlüssel, wird in Diagram.type gespeichert
 * @param label     deutscher Anzeigename
 * @param nodeKinds erlaubte Elementarten in Palettenreihenfolge
 * @param edgeKinds erlaubte Verbindungsarten in Palettenreihenfolge
 */
public record DiagramType(String id, String label, List<NodeKind> nodeKinds, List<EdgeKind> edgeKinds) {

    /** Macht die Listen unveränderlich, damit niemand den Katalog von außen ändert. */
    public DiagramType {
        nodeKinds = List.copyOf(nodeKinds);
        edgeKinds = List.copyOf(edgeKinds);
    }

    /** Liefert true, wenn die Elementart in diesem Diagrammtyp erlaubt ist. */
    public boolean erlaubtElement(String nodeKindId) {
        return nodeKinds.stream().anyMatch(k -> k.id().equals(nodeKindId));
    }

    /** Liefert true, wenn die Verbindungsart in diesem Diagrammtyp erlaubt ist. */
    public boolean erlaubtVerbindung(String edgeKindId) {
        return edgeKinds.stream().anyMatch(k -> k.id().equals(edgeKindId));
    }
}