package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VotingFormController {
    private static final int FALLBACK_MAX_TEAMS_TO_VOTE = 3;

    private final VotifyApi apiClient = ApiClient.getInstance();
    private final Set<String> selectedTeamNames = new LinkedHashSet<>();

    private int maxTeamsToVote = FALLBACK_MAX_TEAMS_TO_VOTE;

    @FXML
    private ListView<VoteCandidateItem> participantList;

    @FXML
    private Label hintLabel;

    @FXML
    private Label selectionCountLabel;

    @FXML
    private Button submitButton;

    @FXML
    private Label userNameLabel;

    @FXML
    private void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText(apiClient.getCurrentUserEmail());
        }

        participantList.setCellFactory(listView -> new VoteCandidateCell());

        try {
            if (apiClient.hasVoted()) {
                hintLabel.setText("Ya has emitido tu voto para este evento.");
                submitButton.setDisable(true);
                participantList.setDisable(true);
                selectionCountLabel.setText("Voto emitido");
                return; // Salimos para no cargar la lista de participantes si ya ha votado.
            }
        } catch (ApiClientException e) {
            // Permitir continuar si la comprobación falla, pero registrar el error.
        }

        try {
            maxTeamsToVote = apiClient.getVotingLimit();
        } catch (ApiClientException ignored) {
            maxTeamsToVote = FALLBACK_MAX_TEAMS_TO_VOTE;
        }

        try {
            List<VoteCandidateItem> items = apiClient.getParticipantResponses().stream()
                    .filter(participant -> participant.getTeamName() != null && !participant.getTeamName().isBlank())
                    .map(participant -> new VoteCandidateItem(
                            participant.getTeamName(),
                            buildSubtitle(participant),
                            participant
                    ))
                    .toList();

            participantList.setItems(FXCollections.observableArrayList(items));
            hintLabel.setText("Selecciona hasta " + maxTeamsToVote + " equipos para votar");

            if (items.isEmpty()) {
                hintLabel.setText("Todavía no hay equipos registrados para votar.");
                submitButton.setDisable(true);
                participantList.setDisable(true);
            }
        } catch (ApiClientException e) {
            hintLabel.setText("No se pudo cargar la lista de equipos.");
            submitButton.setDisable(true);
            participantList.setDisable(true);
            showError(e.getMessage());
        }

        updateSelectionState();
    }

    @FXML
    private void submitVote() {
        List<String> selectedTeams = participantList.getItems().stream()
                .map(VoteCandidateItem::teamName)
                .filter(selectedTeamNames::contains)
                .toList();

        if (selectedTeams.isEmpty()) {
            showError("Selecciona al menos un equipo.");
            return;
        }

        try {
            VoteResponse response = apiClient.createVotes(selectedTeams);
            AlertHelper.showInfo("Votos registrados: " + response.getRecordedVotes());
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            SceneNavigator.showMainMenu(currentStage());
        } catch (IOException e) {
            showError("No se pudo volver al panel principal: " + e.getMessage());
        }
    }

    @FXML
    private void exit() {
        ApiClient.getInstance().logout();
        try {
            SceneNavigator.showScene(
                    currentStage(),
                    "/com/votify/frontend/view/Access.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Acceso"
            );
        } catch (IOException e) {
            showError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    private void toggleSelection(VoteCandidateItem item) {
        if (item == null) {
            return;
        }

        if (selectedTeamNames.contains(item.teamName())) {
            selectedTeamNames.remove(item.teamName());
            updateSelectionState();
            participantList.refresh();
            return;
        }

        if (selectedTeamNames.size() >= maxTeamsToVote) {
            showError("Solo puedes seleccionar hasta " + maxTeamsToVote + " equipos.");
            participantList.refresh();
            return;
        }

        selectedTeamNames.add(item.teamName());
        updateSelectionState();
        participantList.refresh();
    }

    private void updateSelectionState() {
        int selectedCount = selectedTeamNames.size();
        selectionCountLabel.setText(selectedCount + " / " + maxTeamsToVote);
        submitButton.setText("Enviar Votos (" + selectedCount + ")");
        submitButton.setDisable(selectedCount == 0);
    }

    private String buildSubtitle(ParticipantResponse participant) {
        if (participant.getDescription() != null && !participant.getDescription().isBlank()) {
            return participant.getDescription();
        }
        if (participant.getMembers() != null && !participant.getMembers().isEmpty()) {
            return "Integrantes: " + String.join(", ", participant.getMembers());
        }
        if (participant.getEmail() != null && !participant.getEmail().isBlank()) {
            return participant.getEmail();
        }
        return "Equipo participante registrado en Votify";
    }

    private Stage currentStage() {
        return (Stage) submitButton.getScene().getWindow();
    }

    private void showError(String message) {
        AlertHelper.showError(message);
    }

    private final class VoteCandidateCell extends ListCell<VoteCandidateItem> {
        private final CheckBox checkBox = new CheckBox();
        private final Label titleLabel = new Label();
        private final Label subtitleLabel = new Label();
        private final VBox textBox = new VBox(4.0);
        private final HBox root = new HBox(12.0);

        private VoteCandidateCell() {
            checkBox.getStyleClass().add("vote-checkbox");
            checkBox.setFocusTraversable(false);
            checkBox.setMouseTransparent(true);

            titleLabel.getStyleClass().add("vote-team-name");
            // Estilo extra para simular un enlace
            titleLabel.setStyle("-fx-text-fill: #2962ff; -fx-cursor: hand;");
            titleLabel.setOnMouseClicked(event -> {
                event.consume(); // Previene que el CheckBox reaccione
                com.votify.frontend.ui.TeamInfoDialog.show(getItem().participant());
            });

            subtitleLabel.getStyleClass().add("vote-team-subtitle");
            subtitleLabel.setWrapText(true);

            textBox.getChildren().addAll(titleLabel, subtitleLabel);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().add("vote-row");
            root.getChildren().addAll(checkBox, textBox);
            root.setOnMouseClicked(event -> toggleSelection(getItem()));

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(VoteCandidateItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            titleLabel.setText(item.teamName());
            subtitleLabel.setText(item.subtitle());
            checkBox.setSelected(selectedTeamNames.contains(item.teamName()));
            setGraphic(root);
        }
    }

    private record VoteCandidateItem(String teamName, String subtitle, ParticipantResponse participant) {
    }
}
