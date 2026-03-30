package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.exception.ApiClientException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RegistrationFormController {
    private final ApiClient apiClient = new ApiClient();

    @FXML
    private TextField teamField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea membersArea;

    @FXML
    private Label teamErrorLabel;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private void initialize() {
        teamField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateTeamField();
            }
        });

        emailField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateEmailField();
            }
        });
    }

    @FXML
    private void registerParticipant() {
        String team = teamField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        List<String> members = parseMembers(membersArea.getText());

        if (!validateForm()) {
            return;
        }

        try {
            apiClient.createParticipant(team, email, address, phone, members);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setContentText("Participante registrado: " + team);
            success.showAndWait();
            closeWindow();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void cancelRegistration() {
        closeWindow();
    }

    private List<String> parseMembers(String rawMembers) {
        List<String> members = new ArrayList<>();
        if (rawMembers == null || rawMembers.trim().isEmpty()) {
            return members;
        }

        String normalized = rawMembers.replace("\r", "\n");
        String[] tokens = normalized.split("[,\n]");
        for (String token : tokens) {
            String member = token.trim();
            if (!member.isEmpty()) {
                members.add(member);
            }
        }
        return members;
    }

    private void closeWindow() {
        Stage stage = (Stage) teamField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setContentText(message);
        error.showAndWait();
    }

    private boolean validateForm() {
        boolean teamOk = validateTeamField();
        boolean emailOk = validateEmailField();
        return teamOk && emailOk;
    }

    private boolean validateTeamField() {
        String team = teamField.getText() == null ? "" : teamField.getText().trim();
        if (team.isEmpty()) {
            teamErrorLabel.setText("El nombre del equipo es obligatorio.");
            return false;
        }
        try {
            if (apiClient.teamNameExists(team)) {
                teamErrorLabel.setText("El nombre del equipo ya esta registrado.");
                return false;
            }
        } catch (ApiClientException e) {
            teamErrorLabel.setText("No se pudo validar el nombre en el servidor.");
            return false;
        }
        teamErrorLabel.setText("");
        return true;
    }

    private boolean validateEmailField() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty()) {
            emailErrorLabel.setText("El correo es obligatorio.");
            return false;
        }
        if (!validEmail(email)) {
            emailErrorLabel.setText("Formato de correo no valido.");
            return false;
        }
        emailErrorLabel.setText("");
        return true;
    }

    private boolean validEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}
