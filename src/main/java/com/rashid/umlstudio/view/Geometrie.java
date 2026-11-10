package com.rashid.umlstudio.view;

import java.util.ArrayList;
import java.util.List;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;

/**
 * Reine Rechenlogik für die Zeichenfläche (ohne JavaFX, daher gut testbar).
 */
public final class Geometrie {

    private Geometrie() {
    }

    /** Liegt der Punkt (px, py) innerhalb des Elements? */
    public static boolean enthaelt(Node n, double px, double py) {
        return px >= n.getX() && px <= n.getX() + n.getW()
            && py >= n.getY() && py <= n.getY() + n.getH();
    }

    /** Verschachtelungstiefe: 0 = oberste Ebene, 1 = in einem Paket usw. */
    public static int tiefe(Diagram d, Node n) {
        int tiefe = 0;
        String parent = n.getParentId();
        while (parent != null && tiefe < 100) {          // 100 = Schutz gegen Schleifen
            Node p = d.node(parent).orElse(null);
            if (p == null) {
                break;
            }
            tiefe++;
            parent = p.getParentId();
        }
        return tiefe;
    }

    /**
     * Findet das Element unter dem Mauszeiger.
     * Bei Überlappung gewinnt das am tiefsten verschachtelte, bei gleicher Tiefe das zuletzt gezeichnete.
     *
     * @return das getroffene Element oder null
     */
    public static Node treffer(Diagram d, double px, double py) {
        Node bester = null;
        int besteTiefe = -1;
        for (Node n : d.getNodes()) {
            if (enthaelt(n, px, py)) {
                int t = tiefe(d, n);
                if (t >= besteTiefe) {
                    bester = n;
                    besteTiefe = t;
                }
            }
        }
        return bester;
    }

    /** Alle Nachkommen eines Elements (Kinder, Enkel, ...). */
    public static List<Node> nachkommen(Diagram d, Node eltern) {
        List<Node> ergebnis = new ArrayList<>();
        List<String> offen = new ArrayList<>(List.of(eltern.getId()));
        while (!offen.isEmpty()) {
            String id = offen.remove(0);
            for (Node n : d.getNodes()) {
                if (id.equals(n.getParentId()) && !ergebnis.contains(n)) {
                    ergebnis.add(n);
                    offen.add(n.getId());
                }
            }
        }
        return ergebnis;
    }

    /**
     * Punkt auf dem Rand des Elements in Richtung (zielX, zielY).
     * Damit beginnen und enden Linien am Rahmen statt in der Mitte.
     *
     * @return Feld {x, y}
     */
    public static double[] randpunkt(Node n, double zielX, double zielY) {
        double cx = n.getX() + n.getW() / 2;
        double cy = n.getY() + n.getH() / 2;
        double dx = zielX - cx;
        double dy = zielY - cy;
        if (dx == 0 && dy == 0) {
            return new double[] { cx, cy };
        }
        double halbB = n.getW() / 2;
        double halbH = n.getH() / 2;
        double t = Math.min(
                dx == 0 ? Double.MAX_VALUE : halbB / Math.abs(dx),
                dy == 0 ? Double.MAX_VALUE : halbH / Math.abs(dy));
        return new double[] { cx + dx * t, cy + dy * t };
    }

    /** Mittelpunkt eines Elements als {x, y}. */
    public static double[] mitte(Node n) {
        return new double[] { n.getX() + n.getW() / 2, n.getY() + n.getH() / 2 };
    }
}