package com.rashid.umlstudio.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Edge;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.Project;

/**
 * Speichert und lädt Projekte als .umls-Datei (JSON, UTF-8).
 */
public final class ProjectStore {

    /** Dateiendung von UMLStudio-Projekten. */
    public static final String ENDUNG = ".umls";

    /** Aktuelle Version des Dateiformats. */
    public static final int FORMAT_VERSION = 1;

    /** Kennung, an der wir eigene Dateien erkennen. */
    private static final String KENNUNG = "UMLStudio";

    // Gson ist thread-sicher und kann wiederverwendet werden.
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()     // gut lesbar und Git-freundlich
            .serializeNulls()        // parentId = null bleibt sichtbar
            .create();

    private ProjectStore() {
        // Nur statische Methoden.
    }

    /** Hülle um das Projekt mit Kennung und Formatversion. */
    private static final class Dateiinhalt {
        String app;
        int formatVersion;
        Project project;
    }

    /**
     * Speichert ein Projekt sicher: erst in eine Temp-Datei, dann ersetzen.
     *
     * @param projekt das zu speichernde Projekt
     * @param ziel    Zieldatei; fehlt die Endung .umls, wird sie ergänzt
     * @return der tatsächlich verwendete Pfad
     * @throws ProjektDateiException wenn das Schreiben fehlschlägt
     */
    public static Path speichern(Project projekt, Path ziel) throws ProjektDateiException {
        if (projekt == null || ziel == null) {
            throw new IllegalArgumentException("Projekt und Ziel dürfen nicht null sein.");
        }
        Path datei = mitEndung(ziel);

        Dateiinhalt inhalt = new Dateiinhalt();
        inhalt.app = KENNUNG;
        inhalt.formatVersion = FORMAT_VERSION;
        inhalt.project = projekt;

        try {
            Path ordner = datei.toAbsolutePath().getParent();
            Files.createDirectories(ordner);
            Path temp = Files.createTempFile(ordner, "umls-", ".tmp");
            try (Writer w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(inhalt, w);
            }
            Files.move(temp, datei,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return datei;
        } catch (IOException e) {
            throw new ProjektDateiException("Projekt konnte nicht gespeichert werden: "
                    + datei.getFileName(), e);
        }
    }

    /**
     * Lädt ein Projekt und prüft Kennung, Version und Verweise.
     *
     * @throws ProjektDateiException wenn die Datei fehlt, kaputt oder nicht kompatibel ist
     */
    public static Project laden(Path datei) throws ProjektDateiException {
        if (datei == null || !Files.isRegularFile(datei)) {
            throw new ProjektDateiException("Datei nicht gefunden: " + datei);
        }

        Dateiinhalt inhalt;
        try (Reader r = Files.newBufferedReader(datei, StandardCharsets.UTF_8)) {
            inhalt = GSON.fromJson(r, Dateiinhalt.class);
        } catch (IOException | JsonParseException e) {
            throw new ProjektDateiException("Datei ist beschädigt oder kein gültiges JSON: "
                    + datei.getFileName(), e);
        }

        if (inhalt == null || !KENNUNG.equals(inhalt.app) || inhalt.project == null) {
            throw new ProjektDateiException("Keine UMLStudio-Projektdatei: " + datei.getFileName());
        }
        if (inhalt.formatVersion > FORMAT_VERSION) {
            throw new ProjektDateiException("Die Datei stammt aus einer neueren UMLStudio-Version "
                    + "(Format " + inhalt.formatVersion + ").");
        }
        pruefeVerweise(inhalt.project);
        return inhalt.project;
    }

    /** Prüft, ob alle IDs vorhanden sind und alle Verweise auf existierende Elemente zeigen. */
    private static void pruefeVerweise(Project p) throws ProjektDateiException {
        for (Diagram d : p.getDiagrams()) {
            if (d.getId() == null || d.getType() == null) {
                throw new ProjektDateiException("Diagramm ohne ID oder Typ gefunden.");
            }
            for (Node n : d.getNodes()) {
                if (n.getId() == null || n.getKind() == null) {
                    throw new ProjektDateiException("Element ohne ID oder Art in '" + d.getName() + "'.");
                }
                if (n.getParentId() != null && d.node(n.getParentId()).isEmpty()) {
                    throw new ProjektDateiException("Element '" + n.getName()
                            + "' verweist auf ein fehlendes Elternelement.");
                }
            }
            for (Edge e : d.getEdges()) {
                if (d.node(e.getSourceId()).isEmpty() || d.node(e.getTargetId()).isEmpty()) {
                    throw new ProjektDateiException("Verbindung verweist auf ein fehlendes Element in '"
                            + d.getName() + "'.");
                }
            }
        }
    }

    /** Ergänzt die Endung .umls, falls sie fehlt. */
    static Path mitEndung(Path pfad) {
        String name = pfad.getFileName().toString();
        if (name.toLowerCase().endsWith(ENDUNG)) {
            return pfad;
        }
        return pfad.resolveSibling(name + ENDUNG);
    }
}