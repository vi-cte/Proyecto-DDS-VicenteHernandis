package com.votify.frontend.ui;

import com.votify.frontend.dto.ParticipantResponse;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TeamInfoDialog {

    // Muestra un pop-up con la información detallada del equipo
    public static void show(ParticipantResponse participant) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información del Equipo");
        alert.setHeaderText(participant.getTeamName());

        VBox content = new VBox(10);
        
        Label descTitle = new Label("Descripción:");
        descTitle.setStyle("-fx-font-weight: bold;");
        
        Label descLabel = new Label(
            (participant.getDescription() != null && !participant.getDescription().isBlank()) 
            ? participant.getDescription() 
            : "Sin descripción disponible."
        );
        descLabel.setWrapText(true);

        content.getChildren().addAll(descTitle, descLabel);

        if (participant.getMembers() != null && !participant.getMembers().isEmpty()) {
            Label membersTitle = new Label("Integrantes:");
            membersTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            Label membersLabel = new Label("- " + String.join("\n- ", participant.getMembers()));
            content.getChildren().addAll(membersTitle, membersLabel);
        }

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}