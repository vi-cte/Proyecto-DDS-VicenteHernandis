package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.exception.ApiClientException;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class VotingFormController {
    private static final int MAX_TEAMS_TO_VOTE = 3;

    private final ApiClient apiClient = new ApiClient();

    @FXML
    private ListView<VoteCandidateItem> participantList;

    @FXML
    private Label hintLabel;

    @FXML
    private Label selectionCountLabel;

    @FXML
    private Button submitButton;

    @FXML
    private void initialize() {
        participantList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        participantList.setCellFactory(listView -> new VoteCandidateCell());
        participantList.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<VoteCandidateItem>) change -> updateSelectionState()
        );

        try {
            List<ParticipantResponse> participants = apiClient.getParticipantResponses();
            List<VoteCandidateItem> items = participants.stream()
                    .filter(participant -> participant.getTeamName() != null && !participant.getTeamName().isBlank())
                    .map(participant -> new VoteCandidateItem(
                            participant.getTeamName(),
                            buildSubtitle(participant)
                    ))
                    .toList();

            participantList.setItems(FXCollections.observableArrayList(items));
            hintLabel.setText("Selecciona hasta " + MAX_TEAMS_TO_VOTE + " equipos para votar");

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
        List<String> selectedTeams = participantList.getSelectionModel().getSelectedItems().stream()
                .map(VoteCandidateItem::teamName)
                .toList();

        if (selectedTeams.isEmpty()) {
            showError("Selecciona al menos un equipo.");
            return;
        }

        try {
            VoteResponse response = apiClient.createVotes(selectedTeams);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setContentText("Votos registrados: " + response.getRecordedVotes());
            success.showAndWait();
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) submitButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(VotingFormController.class.getResource("/com/votify/frontend/view/MainMenu.fxml"));
            Scene currentScene = stage.getScene();
            Scene scene = new Scene(loader.load(), currentScene.getWidth(), currentScene.getHeight());
            scene.getStylesheets().add(Objects.requireNonNull(
                    VotingFormController.class.getResource("/com/votify/frontend/view/MainMenu.css")
            ).toExternalForm());
            stage.setTitle("Votify");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("No se pudo volver al panel principal: " + e.getMessage());
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    private void updateSelectionState() {
        int selectedCount = participantList.getSelectionModel().getSelectedItems().size();
        selectionCountLabel.setText(selectedCount + " / " + MAX_TEAMS_TO_VOTE);
        submitButton.setText("Enviar Votos (" + selectedCount + ")");
        submitButton.setDisable(selectedCount == 0);
    }

    private String buildSubtitle(ParticipantResponse participant) {
        if (participant.getMembers() != null && !participant.getMembers().isEmpty()) {
            return "Integrantes: " + String.join(", ", participant.getMembers());
        }
        if (participant.getDescription() != null && !participant.getDescription().isBlank()) {
            return participant.getDescription();
        }
        if (participant.getEmail() != null && !participant.getEmail().isBlank()) {
            return participant.getEmail();
        }
        return "Equipo participante registrado en Votify";
    }

    private void showError(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setContentText(message);
        error.showAndWait();
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

            titleLabel.getStyleClass().add("vote-team-name");
            subtitleLabel.getStyleClass().add("vote-team-subtitle");
            subtitleLabel.setWrapText(true);

            textBox.getChildren().addAll(titleLabel, subtitleLabel);
            HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().add("vote-row");
            root.getChildren().addAll(checkBox, textBox);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(root);

            this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && getItem() != null) {
                    event.consume(); // Bloquea la selección por defecto de JavaFX
                    toggleSelection();
                }
            });
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
            checkBox.setSelected(getListView().getSelectionModel().isSelected(getIndex()));
            setGraphic(root);
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            checkBox.setSelected(selected);
        }

        private void toggleSelection() {
            if (getItem() == null) {
                return;
            }

            var selectionModel = getListView().getSelectionModel();
            int index = getIndex();
            if (selectionModel.isSelected(index)) {
                selectionModel.clearSelection(index);
                return;
            }

            if (selectionModel.getSelectedIndices().size() >= MAX_TEAMS_TO_VOTE) {
                showError("Solo puedes seleccionar hasta " + MAX_TEAMS_TO_VOTE + " equipos.");
                checkBox.setSelected(false);
                return;
            }

            selectionModel.select(index);
        }
    }

    private record VoteCandidateItem(String teamName, String subtitle) {
    }
}
