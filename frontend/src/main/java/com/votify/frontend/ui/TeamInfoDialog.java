package com.votify.frontend.ui;

import com.votify.frontend.dto.ParticipantResponse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class TeamInfoDialog {

    // Muestra un pop-up con la información detallada del equipo
    public static void show(ParticipantResponse participant) {
        if (participant == null) {
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Información del equipo");

        Label title = new Label("Información del equipo");
        title.getStyleClass().add("team-dialog-title");

        Button closeIcon = new Button("×");
        closeIcon.getStyleClass().add("team-dialog-icon-button");
        closeIcon.setOnAction(event -> stage.close());

        HBox header = new HBox(16, title, closeIcon);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, javafx.scene.layout.Priority.ALWAYS);

        StackPane avatar = buildAvatar(participant);
        Label name = new Label(nullToDefault(participant.getTeamName(), "Equipo sin nombre"));
        name.getStyleClass().add("team-dialog-name");

        Label descLabel = new Label(nullToDefault(participant.getDescription(), "Sin descripción disponible."));
        descLabel.getStyleClass().add("team-dialog-description");
        descLabel.setWrapText(true);

        VBox details = new VBox(12, name, descLabel);
        HBox teamHeader = new HBox(18, avatar, details);
        teamHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        VBox card = new VBox(22, teamHeader);
        card.getStyleClass().add("team-dialog-card");
        card.setPadding(new Insets(24));

        if (participant.getMembers() != null && !participant.getMembers().isEmpty()) {
            Label membersTitle = new Label("Integrantes");
            membersTitle.getStyleClass().add("team-dialog-members-title");
            Label membersLabel = new Label(String.join("\n", participant.getMembers()));
            membersLabel.getStyleClass().add("team-dialog-members-list");
            VBox membersBox = new VBox(6, membersTitle, membersLabel);
            card.getChildren().add(membersBox);
        }

        Button closeButton = new Button("Cerrar");
        closeButton.getStyleClass().add("team-dialog-close-button");
        closeButton.setOnAction(event -> stage.close());
        HBox actions = new HBox(closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(20, header, card, actions);
        content.getStyleClass().add("team-dialog-root");
        content.setPadding(new Insets(24));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("screen-scroll");

        Scene scene = new Scene(scrollPane, 560, 440);
        scene.getStylesheets().add(TeamInfoDialog.class.getResource("/com/votify/frontend/view/MainMenu.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static StackPane buildAvatar(ParticipantResponse participant) {
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("team-dialog-avatar");
        avatar.setMinSize(90, 90);
        avatar.setPrefSize(90, 90);
        avatar.setMaxSize(90, 90);

        String logo = participant.getLogo();
        if (logo != null && !logo.isBlank()) {
            try {
                byte[] bytes = Base64.getDecoder().decode(logo);
                ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(bytes)));
                imageView.setFitWidth(82);
                imageView.setFitHeight(82);
                imageView.setPreserveRatio(true);
                avatar.getChildren().add(imageView);
                return avatar;
            } catch (IllegalArgumentException ignored) {
            }
        }

        SVGPath icon = new SVGPath();
        icon.setContent("M12 12.1C13.7 12.1 15.05 10.72 15.05 9.03C15.05 7.34 13.7 5.96 12 5.96C10.3 5.96 8.95 7.34 8.95 9.03C8.95 10.72 10.3 12.1 12 12.1ZM12 13.63C9.23 13.63 6.95 15.91 6.95 18.68V19.45C6.95 19.87 7.28 20.2 7.7 20.2H16.3C16.72 20.2 17.05 19.87 17.05 19.45V18.68C17.05 15.91 14.77 13.63 12 13.63Z");
        icon.getStyleClass().add("team-dialog-default-icon");
        avatar.getChildren().add(icon);
        return avatar;
    }

    private static String nullToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
