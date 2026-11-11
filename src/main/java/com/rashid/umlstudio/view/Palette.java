package com.rashid.umlstudio.view;

import java.util.function.Consumer;

import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.Node;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.model.catalog.NodeKind;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Palette zum Hinzufügen der im Katalog erlaubten Elementarten. */
public class Palette extends VBox {

    /**
     * @param diagram das bearbeitete Diagramm
     * @param hinzugefuegt wird nach dem Hinzufügen mit dem neuen Element aufgerufen
     */
    public Palette(Diagram diagram, Consumer<Node> hinzugefuegt) {
        setSpacing(8);
        setPadding(new Insets(10));
        setPrefWidth(170);

        Label titel = new Label("Elemente");
        titel.setStyle("-fx-font-weight: bold;");
        getChildren().add(titel);

        for (NodeKind art : Catalog.typ(diagram.getType()).orElseThrow().nodeKinds()) {
            Button knopf = new Button(art.label());
            knopf.setMaxWidth(Double.MAX_VALUE);
            knopf.setOnAction(e -> {
                // Wiederholter Versatz verhindert eine immer weiter entfernte Startposition.
                int versatz = (diagram.getNodes().size() % 8) * 24;
                Node neu = Catalog.neuesElement(
                        diagram.getType(), art.id(), art.label(),
                        80 + versatz, 80 + versatz);
                diagram.addNode(neu);
                if (hinzugefuegt != null) {
                    hinzugefuegt.accept(neu);
                }
            });
            getChildren().add(knopf);
        }
    }
}