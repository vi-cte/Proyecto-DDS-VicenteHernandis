package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClientProxy;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.dto.EventSettingsResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Optional;

public class SettingsFormController {

    private final VotifyApi apiClient = ApiClientProxy.getInstance();

    @FXML private CheckBox registrationToggle;
    @FXML private CheckBox votingToggle;
    @FXML private Label registrationStatusLabel;
    @FXML private Label votingStatusLabel;
    @FXML private TextField maxVotesField;
    @FXML private Button resetButton;

    @FXML
    private void initialize() {
        try {
            EventSettingsResponse settings = apiClient.getAdminSettings();
            registrationToggle.setSelected(settings.isRegistrationsOpen());
            votingToggle.setSelected(settings.isVotingOpen());
            maxVotesField.setText(String.valueOf(settings.getMaxTeamsToVote()));
            updateLabels();
        } catch (ApiClientException e) {
            AlertHelper.showWarning("Aviso: " + e.getMessage());
        }

        registrationToggle.selectedProperty().addListener((obs, oldV, newV) -> saveSettings());
        votingToggle.selectedProperty().addListener((obs, oldV, newV) -> saveSettings());
        maxVotesField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) saveSettings();
        });
    }

    @FXML
    private void onRegistrationToggle() {
        if (registrationToggle.isSelected()) {
            votingToggle.setSelected(false);
        }
        updateLabels();
        saveSettings();
    }

    @FXML
    private void onVotingToggle() {
        if (votingToggle.isSelected()) {
            registrationToggle.setSelected(false);
        }
        updateLabels();
        saveSettings();
    }

    private void updateLabels() {
        registrationStatusLabel.setText(registrationToggle.isSelected() ? "Abiertas" : "Cerradas");
        votingStatusLabel.setText(votingToggle.isSelected() ? "Abiertas" : "Cerradas");
    }

    private void saveSettings() {
        try {
            int maxVotes = Integer.parseInt(maxVotesField.getText());
            apiClient.updateAdminSettings(registrationToggle.isSelected(), votingToggle.isSelected(), maxVotes);
        } catch (NumberFormatException e) {
            AlertHelper.showError("El número de votos debe ser numérico.");
        } catch (ApiClientException e) {
            AlertHelper.showError("Error al guardar configuración: " + e.getMessage());
        }
    }

    @FXML
    private void resetEvent() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Reinicio");
        alert.setHeaderText("¿Estás seguro de que deseas reiniciar el evento?");
        alert.setContentText("Esta acción eliminará de forma permanente todos los equipos participantes y los votos registrados. No se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                apiClient.resetEvent();
                AlertHelper.showInfo("El evento ha sido reiniciado. Todos los datos han sido eliminados.");
            } catch (ApiClientException e) {
                AlertHelper.showError("Error al reiniciar evento: " + e.getMessage());
            }
        }
    }

    @FXML
    private void close() {
        ((Stage) registrationToggle.getScene().getWindow()).close();
    }
}