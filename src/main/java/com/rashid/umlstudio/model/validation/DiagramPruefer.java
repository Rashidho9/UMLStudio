package com.rashid.umlstudio.model.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.model.catalog.DiagramType;
import com.rashid.umlstudio.model.catalog.NodeKind;

/**
 * Prüft die Struktur eines Diagramms, ohne dessen Inhalt zu verändern.
 * Auch beschädigte Daten mit Nullwerten oder Elternzyklen werden berücksichtigt.
 */
public final class DiagramPruefer {

    private DiagramPruefer() {
    }

    /**
     * Liefert eine unveränderliche Befundliste.
     * Eine leere Liste bedeutet: keine Probleme innerhalb des Prüfumfangs.
     */
    public static List<Pruefmeldung> pruefe(Diagram diagram) {
        List<Pruefmeldung> funde = new ArrayList<>();
        if (diagram == null) {
            funde.add(meldung("DIAGRAMM_FEHLT", null,
                    "Es ist kein Diagramm vorhanden."));
            return List.copyOf(funde);
        }

        if (istLeer(diagram.getId())) {
            funde.add(meldung("DIAGRAMM_ID_FEHLT", diagram.getId(),
                    "Das Diagramm hat keine ID."));
        }

        DiagramType typ = Catalog.typ(diagram.getType()).orElse(null);
        if (typ == null) {
            funde.add(meldung("DIAGRAMM_TYP_UNBEKANNT", null,
                    "Unbekannter oder fehlender Diagrammtyp: "
                            + diagram.getType()));
        }

        Map<String, Node> elemente = sammleElemente(diagram.getNodes(), funde);
        pruefeElemente(elemente, typ, funde);
        pruefeKanten(diagram.getEdges(), elemente, typ, funde);
        return List.copyOf(funde);
    }

    /** Sammelt eindeutige IDs und meldet fehlende oder doppelte Einträge. */
    private static Map<String, Node> sammleElemente(
            List<Node> roh, List<Pruefmeldung> funde) {
        Map<String, Node> nachId = new LinkedHashMap<>();
        if (roh == null) {
            funde.add(meldung("ELEMENTLISTE_FEHLT", null,
                    "Die Elementliste fehlt."));
            return nachId;
        }
        for (Node n : roh) {
            if (n == null) {
                funde.add(meldung("ELEMENT_NULL", null,
                        "Ein Element-Eintrag ist leer."));
                continue;
            }
            String id = n.getId();
            if (istLeer(id)) {
                funde.add(meldung("ELEMENT_ID_FEHLT", id,
                        "Ein Element hat keine ID."));
                continue;
            }
            if (nachId.containsKey(id)) {
                funde.add(meldung("ELEMENT_ID_DOPPELT", id,
                        "Die Element-ID kommt mehrfach vor: " + id));
                continue;
            }
            nachId.put(id, n);
        }
        return nachId;
    }

    /** Prüft Art, Position, Größe und Elternzuordnung. */
    private static void pruefeElemente(Map<String, Node> elemente,
            DiagramType typ, List<Pruefmeldung> funde) {
        for (Node n : elemente.values()) {
            String id = n.getId();
            if (typ != null && (n.getKind() == null
                    || !typ.erlaubtElement(n.getKind()))) {
                funde.add(meldung("ELEMENT_ART_UNBEKANNT", id,
                        "Unbekannte Elementart: " + n.getKind()));
            }
            if (!Double.isFinite(n.getX()) || !Double.isFinite(n.getY())) {
                funde.add(meldung("ELEMENT_POSITION_UNGUELTIG", id,
                        "Die Position muss aus endlichen Zahlen bestehen."));
            }
            if (!Double.isFinite(n.getW()) || !Double.isFinite(n.getH())
                    || n.getW() <= 0 || n.getH() <= 0) {
                funde.add(meldung("ELEMENT_GROESSE_UNGUELTIG", id,
                        "Breite und Höhe müssen endlich und größer als 0 sein."));
            }
            pruefeHierarchie(n, elemente, typ, funde);
        }
    }

    /** Prüft direkte Eltern und zyklische Elternketten. */
    private static void pruefeHierarchie(Node n, Map<String, Node> elemente,
            DiagramType typ, List<Pruefmeldung> funde) {
        String parentId = n.getParentId();
        if (istLeer(parentId)) {
            return;
        }
        if (parentId.equals(n.getId())) {
            funde.add(meldung("ELTERN_SELBST", n.getId(),
                    "Ein Element verweist auf sich selbst als Elternteil."));
            return;
        }
        Node eltern = elemente.get(parentId);
        if (eltern == null) {
            funde.add(meldung("ELTERN_FEHLT", n.getId(),
                    "Das Elternelement " + parentId + " fehlt."));
            return;
        }
        if (hatZyklus(n, elemente)) {
            funde.add(meldung("ELTERN_ZYKLUS", n.getId(),
                    "Die Elternkette enthält einen Zyklus."));
        }
        if (typ != null) {
            NodeKind art = typ.nodeKinds().stream()
                    .filter(k -> k.id().equals(eltern.getKind()))
                    .findFirst().orElse(null);
            if (art != null && !art.container()) {
                funde.add(meldung("ELTERN_KEIN_BEHAELTER", n.getId(),
                        "Das Elternelement darf keine Inhalte aufnehmen."));
            }
        }
    }

    /**
     * Verfolgt die Elternkette mit einer Menge bereits besuchter IDs.
     * Verwendet bewusst weder descendants noch depth des Modells.
     */
    private static boolean hatZyklus(Node start, Map<String, Node> elemente) {
        Set<String> pfad = new HashSet<>();
        String aktuell = start.getId();
        int schutz = elemente.size() + 1;
        while (aktuell != null && schutz-- > 0) {
            if (!pfad.add(aktuell)) {
                return true;
            }
            Node n = elemente.get(aktuell);
            if (n == null || istLeer(n.getParentId())) {
                return false;
            }
            aktuell = n.getParentId();
        }
        return schutz <= 0;
    }

    /** Prüft Verbindungs-IDs, Verbindungsarten und Endpunkte. */
    private static void pruefeKanten(List<Edge> roh, Map<String, Node> elemente,
            DiagramType typ, List<Pruefmeldung> funde) {
        if (roh == null) {
            funde.add(meldung("KANTENLISTE_FEHLT", null,
                    "Die Verbindungsliste fehlt."));
            return;
        }
        Set<String> bekannteIds = new HashSet<>();
        for (Edge k : roh) {
            if (k == null) {
                funde.add(meldung("KANTE_NULL", null,
                        "Ein Verbindungs-Eintrag ist leer."));
                continue;
            }
            String id = k.getId();
            if (istLeer(id)) {
                funde.add(meldung("KANTE_ID_FEHLT", id,
                        "Eine Verbindung hat keine ID."));
            } else if (!bekannteIds.add(id)) {
                funde.add(meldung("KANTE_ID_DOPPELT", id,
                        "Die Verbindungs-ID kommt mehrfach vor: " + id));
            }
            if (typ != null && (k.getKind() == null
                    || !typ.erlaubtVerbindung(k.getKind()))) {
                funde.add(meldung("KANTE_ART_UNBEKANNT", id,
                        "Unbekannte Verbindungsart: " + k.getKind()));
            }
            if (istLeer(k.getSourceId())
                    || !elemente.containsKey(k.getSourceId())) {
                funde.add(meldung("KANTE_QUELLE_FEHLT", id,
                        "Die Quelle der Verbindung fehlt oder ist unbekannt."));
            }
            if (istLeer(k.getTargetId())
                    || !elemente.containsKey(k.getTargetId())) {
                funde.add(meldung("KANTE_ZIEL_FEHLT", id,
                        "Das Ziel der Verbindung fehlt oder ist unbekannt."));
            }
        }
    }

    private static boolean istLeer(String wert) {
        return wert == null || wert.isBlank();
    }

    private static Pruefmeldung meldung(String code, String id, String text) {
        return new Pruefmeldung(code, id, text);
    }
}