package com.votify.frontend.ui;

import com.votify.frontend.dto.ParticipantResponse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

public class TeamInfoDialog {

    public static void show(ParticipantResponse participant) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e2e8f2; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        root.setPrefWidth(550);

        // Encabezado
        HBox topHeader = new HBox();
        topHeader.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Información del equipo");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #162642;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeIconBtn = new Button("✕");
        closeIconBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #162642; -fx-font-size: 16px; -fx-cursor: hand;");
        closeIconBtn.setOnAction(e -> stage.close());
        topHeader.getChildren().addAll(titleLabel, spacer, closeIconBtn);

        // Tarjeta Principal
        VBox card = new VBox(20);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #e2e8f2; -fx-border-radius: 12; -fx-border-width: 1;");

        // Sección del Perfil (Logo, Nombre, Descripción)
        HBox profileSection = new HBox(20);
        profileSection.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(90, 90);
        avatarPane.setMinSize(90, 90);
        avatarPane.setMaxSize(90, 90);
        avatarPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #4a90e2, #9013fe); -fx-background-radius: 45;");

        SVGPath defaultIcon = new SVGPath();
        defaultIcon.setContent("M12 12.1C13.7 12.1 15.05 10.72 15.05 9.03C15.05 7.34 13.7 5.96 12 5.96C10.3 5.96 8.95 7.34 8.95 9.03C8.95 10.72 10.3 12.1 12 12.1ZM12 13.63C9.23 13.63 6.95 15.91 6.95 18.68V19.45C6.95 19.87 7.28 20.2 7.7 20.2H8.47C8.89 20.2 9.22 19.87 9.22 19.45V18.68C9.22 17.16 10.48 15.9 12 15.9C13.52 15.9 14.78 17.16 14.78 18.68V19.45C14.78 19.87 15.11 20.2 15.53 20.2H16.3C16.72 20.2 17.05 19.87 17.05 19.45V18.68C17.05 15.91 14.77 13.63 12 13.63Z");
        defaultIcon.setStyle("-fx-fill: white; -fx-scale-x: 1.5; -fx-scale-y: 1.5;");
        avatarPane.getChildren().add(defaultIcon);

        if (participant.getLogo() != null && !participant.getLogo().isBlank()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(participant.getLogo());
                Image img = new Image(new ByteArrayInputStream(imageBytes));
                ImageView logoView = new ImageView(img);
                logoView.setFitWidth(90);
                logoView.setFitHeight(90);
                Circle clip = new Circle(45, 45, 45);
                logoView.setClip(clip);
                avatarPane.getChildren().clear();
                avatarPane.getChildren().add(logoView);
                avatarPane.setStyle("-fx-background-color: transparent;");
            } catch (Exception ignored) {}
        }

        VBox textInfo = new VBox(8);
        textInfo.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(participant.getTeamName() != null ? participant.getTeamName() : "Sin nombre");
        nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        nameLabel.setWrapText(true);

        Label descLabel = new Label(participant.getDescription() != null && !participant.getDescription().isBlank() ? participant.getDescription() : "Sin descripción");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        descLabel.setWrapText(true);

        textInfo.getChildren().addAll(nameLabel, descLabel);
        profileSection.getChildren().addAll(avatarPane, textInfo);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #e2e8f2 transparent; -fx-border-width: 0 0 1 0;");

        // Sección de Integrantes
        HBox membersSection = new HBox(15);
        membersSection.setAlignment(Pos.CENTER_LEFT);

        StackPane membersIconPane = new StackPane();
        membersIconPane.setPrefSize(42, 42);
        membersIconPane.setStyle("-fx-background-color: #f3e8ff; -fx-background-radius: 8;");
        SVGPath membersIcon = new SVGPath();
        membersIcon.setContent("M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z");
        membersIcon.setStyle("-fx-fill: #9013fe; -fx-scale-x: 0.9; -fx-scale-y: 0.9;");
        membersIconPane.getChildren().add(membersIcon);

        VBox membersTextInfo = new VBox(4);
        List<String> members = participant.getMembers();
        int memberCount = members != null ? members.size() : 0;
        Label membersTitle = new Label("Integrantes (" + memberCount + ")");
        membersTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        Label membersList = new Label(memberCount > 0 ? String.join(", ", members) : "Sin integrantes");
        membersList.setStyle("-fx-font-size: 14px; -fx-text-fill: #1f2937;");
        membersList.setWrapText(true);

        membersTextInfo.getChildren().addAll(membersTitle, membersList);
        membersSection.getChildren().addAll(membersIconPane, membersTextInfo);

        card.getChildren().addAll(profileSection, separator, membersSection);

        // Botones inferiores
        HBox bottomControls = new HBox();
        bottomControls.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("Cerrar información");
        closeBtn.setStyle("-fx-background-color: #2962ff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());
        bottomControls.getChildren().add(closeBtn);

        root.getChildren().addAll(topHeader, card, bottomControls);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }
}