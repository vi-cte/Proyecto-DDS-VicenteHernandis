package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.exception.ApiClientException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VotingFormController {
    private static final String BLANK_OPTION = "";

    private final ApiClient apiClient = new ApiClient();

    @FXML
    private ComboBox<String> firstChoice;

    @FXML
    private ComboBox<String> secondChoice;

    @FXML
    private ComboBox<String> thirdChoice;

    @FXML
    private Label hintLabel;

    @FXML
    private Button submitButton;

    @FXML
    private void initialize() {
        try {
            List<String> participants = apiClient.getParticipants();
            List<String> votingOptions = new java.util.ArrayList<>();
            votingOptions.add(BLANK_OPTION);
            votingOptions.addAll(participants);

            firstChoice.setItems(FXCollections.observableArrayList(votingOptions));
            secondChoice.setItems(FXCollections.observableArrayList(votingOptions));
            thirdChoice.setItems(FXCollections.observableArrayList(votingOptions));

            firstChoice.setValue(BLANK_OPTION);
            secondChoice.setValue(BLANK_OPTION);
            thirdChoice.setValue(BLANK_OPTION);

            if (participants.isEmpty()) {
                hintLabel.setText("Todavia no hay participantes registrados.");
                submitButton.setDisable(true);
            }
        } catch (ApiClientException e) {
            hintLabel.setText("No se pudo cargar la lista de participantes.");
            submitButton.setDisable(true);
            showError(e.getMessage());
        }
    }

    // Mantiene el orden de seleccion y detecta duplicados antes de enviar el voto.
    @FXML
    private void submitVote() {
        Set<String> choiceSelections = new LinkedHashSet<>();
        if (isValidChoice(firstChoice.getValue())) {
            choiceSelections.add(firstChoice.getValue());
        }
        if (isValidChoice(secondChoice.getValue())) {
            choiceSelections.add(secondChoice.getValue());
        }
        if (isValidChoice(thirdChoice.getValue())) {
            choiceSelections.add(thirdChoice.getValue());
        }

        if (choiceSelections.isEmpty()) {
            showError("Selecciona al menos un participante.");
            return;
        }

        int totalSelected = countSelectedValues();
        if (choiceSelections.size() != totalSelected) {
            showError("No se permiten opciones duplicadas.");
            return;
        }

        try {
            VoteResponse response = apiClient.createVotes(List.copyOf(choiceSelections));
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setContentText("Votos registrados: " + response.getRecordedVotes());
            success.showAndWait();
            closeWindow();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    private int countSelectedValues() {
        int count = 0;
        if (isValidChoice(firstChoice.getValue())) {
            count++;
        }
        if (isValidChoice(secondChoice.getValue())) {
            count++;
        }
        if (isValidChoice(thirdChoice.getValue())) {
            count++;
        }
        return count;
    }

    private boolean isValidChoice(String value) {
        return value != null && !value.isBlank() && !BLANK_OPTION.equals(value);
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
