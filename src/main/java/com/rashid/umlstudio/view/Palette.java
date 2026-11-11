package com.rashid.umlstudio.view;

import com.rashid.umlstudio.controller.DocumentController;
import com.rashid.umlstudio.model.Diagram;
import com.rashid.umlstudio.model.catalog.Catalog;
import com.rashid.umlstudio.model.catalog.NodeKind;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Fügt Elemente über den Controller hinzu, damit Undo möglich ist. */
public class Palette extends VBox {

    public Palette(DocumentController controller) {
        Diagram diagram = controller.getDiagram();
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
                int versatz = (diagram.getNodes().size() % 8) * 24;
                controller.elementHinzufuegen(art.id(), art.label(),
                        80 + versatz, 80 + versatz);
            });
            getChildren().add(knopf);
        }
    }
}