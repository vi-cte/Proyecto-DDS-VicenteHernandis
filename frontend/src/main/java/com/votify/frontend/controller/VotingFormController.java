package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.exception.ApiClientException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.util.List;

public class VotingFormController {
    private static final int MAX_TEAMS_TO_VOTE = 3;

    private final ApiClient apiClient = new ApiClient();

    @FXML
    private ListView<String> participantList;

    @FXML
    private Label hintLabel;

    @FXML
    private Label selectionCountLabel;

    @FXML
    private Button submitButton;

    @FXML
    private void initialize() {
        try {
            List<String> participants = apiClient.getParticipants();
            participantList.setItems(FXCollections.observableArrayList(participants));
            participantList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            participantList.getSelectionModel().getSelectedItems().addListener(
                    (javafx.collections.ListChangeListener<String>) change -> enforceSelectionLimit()
            );
            hintLabel.setText("Selecciona hasta " + MAX_TEAMS_TO_VOTE + " equipos diferentes de la lista.");
            updateSelectionCountLabel();

            if (participants.isEmpty()) {
                hintLabel.setText("Todavia no hay participantes registrados.");
                submitButton.setDisable(true);
                participantList.setDisable(true);
            }
        } catch (ApiClientException e) {
            hintLabel.setText("No se pudo cargar la lista de participantes.");
            submitButton.setDisable(true);
            participantList.setDisable(true);
            showError(e.getMessage());
        }
    }

    @FXML
    private void submitVote() {
        List<String> selectedTeams = List.copyOf(participantList.getSelectionModel().getSelectedItems());

        if (selectedTeams.isEmpty()) {
            showError("Selecciona al menos un equipo.");
            return;
        }

        if (selectedTeams.size() > MAX_TEAMS_TO_VOTE) {
            showError("Solo puedes votar a " + MAX_TEAMS_TO_VOTE + " equipos.");
            return;
        }

        try {
            VoteResponse response = apiClient.createVotes(selectedTeams);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setContentText("Votos registrados: " + response.getRecordedVotes());
            success.showAndWait();
            closeWindow();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    private void enforceSelectionLimit() {
        var selectionModel = participantList.getSelectionModel();
        while (selectionModel.getSelectedItems().size() > MAX_TEAMS_TO_VOTE) {
            int lastIndex = selectionModel.getSelectedIndices().get(selectionModel.getSelectedIndices().size() - 1);
            selectionModel.clearSelection(lastIndex);
        }
        updateSelectionCountLabel();
    }

    private void updateSelectionCountLabel() {
        int selectedCount = participantList.getSelectionModel().getSelectedItems().size();
        selectionCountLabel.setText(selectedCount + " seleccionados de " + MAX_TEAMS_TO_VOTE);
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setContentText(message);
        error.showAndWait();
    }
}
