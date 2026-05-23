package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Controlador del formulario de creación de evento.
public class CreateEventDialogController {
    @FXML private Label dialogTitle;
    @FXML private Label dialogSubtitle;
    @FXML private TextField nameField;
    @FXML private DatePicker eventDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox registrationsToggle;
    @FXML private CheckBox votingToggle;
    @FXML private CheckBox juryToggle;
    @FXML private ComboBox<String> juryVotingModeComboBox;
    @FXML private ComboBox<PhaseOption> phaseComboBox;
    @FXML private TextField maxVotesField;
    @FXML private CheckBox resultsToggle;
    @FXML private Label resultsLabel;
    @FXML private Label registrationsLabel;
    @FXML private Label votingLabel;
    @FXML private Label juryLabel;
    @FXML private Button actionButton;
    @FXML private Button deleteButton;
    @FXML private Button archiveButton;

    private Long editEventId = null;
    private boolean editResultsVisible = false;

    private static final java.util.List<PhaseOption> PHASES = java.util.List.of(
            new PhaseOption("REGISTRATION_OPEN", "Registro de equipos abierto"),
            new PhaseOption("REGISTRATION_CLOSED", "Registro cerrado"),
            new PhaseOption("PUBLIC_VOTING_OPEN", "Votación pública abierta"),
            new PhaseOption("JURY_VOTING_OPEN", "Votación del jurado abierta"),
            new PhaseOption("PUBLIC_AND_JURY_VOTING_OPEN", "Votación pública y jurado abierta"),
            new PhaseOption("VOTING_CLOSED", "Votaciones cerradas"),
            new PhaseOption("RESULTS_VISIBLE", "Resultados visibles"),
            new PhaseOption("ARCHIVED", "Archivado")
    );

    @FXML
    private void onRegistrationToggle() {
        if (registrationsToggle.isSelected()) {
            votingToggle.setSelected(false);
        }
        updateLabels();
    }

    @FXML
    private void onVotingToggle() {
        if (votingToggle.isSelected()) {
            registrationsToggle.setSelected(false);
        }
        updateLabels();
    }

    @FXML
    private void updateLabels() {
        registrationsLabel.setText(registrationsToggle.isSelected() ? "Abiertas" : "Cerradas");
        votingLabel.setText(votingToggle.isSelected() ? "Abiertas" : "Cerradas");
        if (resultsLabel != null) {
            resultsLabel.setText(resultsToggle != null && resultsToggle.isSelected() ? "Visibles" : "Ocultos");
        }
        juryLabel.setText(juryToggle.isSelected() ? "Activado" : "Desactivado");
    }

    @FXML
    private void initialize() {
        juryVotingModeComboBox.getItems().setAll("SIMPLE", "MULTICRITERIA");
        juryVotingModeComboBox.setValue("SIMPLE");
        if (phaseComboBox != null) {
            phaseComboBox.getItems().setAll(PHASES);
            phaseComboBox.setValue(findPhase("REGISTRATION_OPEN"));
            phaseComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyPhaseToLegacyToggles());
        }
    }

    // Carga los datos de un evento existente para editarlo.
    public void loadEvent(com.votify.frontend.dto.AdminEventResponse event) {
        editEventId = event.getId();
        editResultsVisible = event.isResultsVisible();
        
        if (dialogTitle != null) dialogTitle.setText("Ajustes del evento");
        if (dialogSubtitle != null) dialogSubtitle.setText("Modifica la configuración de este evento");
        if (actionButton != null) actionButton.setText("Guardar cambios");
        
        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }
        
        if (archiveButton != null) {
            archiveButton.setVisible(event.isActive());
            archiveButton.setManaged(event.isActive());
        }
        
        nameField.setText(event.getName());
        if (event.getEventDate() != null) {
            try {
                eventDatePicker.setValue(java.time.LocalDate.parse(event.getEventDate()));
            } catch (Exception ignored) {}
        }
        descriptionArea.setText(event.getDescription() != null ? event.getDescription() : "");
        registrationsToggle.setSelected(event.isRegistrationsOpen());
        votingToggle.setSelected(event.isVotingOpen());
        if (resultsToggle != null) {
            resultsToggle.setSelected(event.isResultsVisible());
        }
        maxVotesField.setText(String.valueOf(event.getMaxTeamsToVote()));
        juryToggle.setSelected(event.isJuryEnabled());
        juryVotingModeComboBox.setValue(normalizeMode(event.getJuryVotingMode()));
        if (phaseComboBox != null) {
            phaseComboBox.setValue(findPhase(event.getPhase()));
        }
        
        updateLabels();
    }

    @FXML
    private void createEvent() {
        try {
            int maxVotes = Integer.parseInt(maxVotesField.getText().trim());
            String date = eventDatePicker.getValue() == null ? null : eventDatePicker.getValue().toString();
            
            if (editEventId != null) {
                ApiClient.getInstance().updateAdminEvent(
                        editEventId,
                        nameField.getText(),
                        date,
                        descriptionArea.getText(),
                        registrationsToggle.isSelected(),
                        votingToggle.isSelected(),
                        resultsToggle != null ? resultsToggle.isSelected() : editResultsVisible,
                        maxVotes,
                        juryToggle.isSelected(),
                        normalizeMode(juryVotingModeComboBox.getValue()),
                        selectedPhase()
                );
            } else {
                ApiClient.getInstance().createAdminEvent(
                        nameField.getText(),
                        date,
                        descriptionArea.getText(),
                        registrationsToggle.isSelected(),
                        votingToggle.isSelected(),
                        resultsToggle != null ? resultsToggle.isSelected() : false,
                        maxVotes,
                        juryToggle.isSelected(),
                        normalizeMode(juryVotingModeComboBox.getValue()),
                        selectedPhase()
                );
            }
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            AlertHelper.showError("El número de votos debe ser numérico.");
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
        }
    }

    @FXML
    private void deleteEvent() {
        if (editEventId == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar evento");
        alert.setContentText("¿Estás seguro de que deseas eliminar este evento? Se perderán todos los datos, participantes y votos. Esta acción no se puede deshacer.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ApiClient.getInstance().deleteAdminEvent(editEventId);
                    ((Stage) nameField.getScene().getWindow()).close();
                } catch (ApiClientException e) {
                    AlertHelper.showError(e.getMessage());
                }
            }
        });
    }

    @FXML
    private void archiveEvent() {
        if (editEventId == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar archivado");
        alert.setHeaderText("Archivar evento");
        alert.setContentText("¿Estás seguro de que deseas archivar este evento? Dejará de estar activo y pasará al histórico.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ApiClient.getInstance().archiveAdminEvent(editEventId);
                    ((Stage) nameField.getScene().getWindow()).close();
                } catch (ApiClientException e) {
                    AlertHelper.showError(e.getMessage());
                }
            }
        });
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "SIMPLE";
        }
        return mode.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String selectedPhase() {
        PhaseOption phase = phaseComboBox == null ? null : phaseComboBox.getValue();
        return phase == null ? "REGISTRATION_OPEN" : phase.key();
    }

    private PhaseOption findPhase(String phase) {
        String normalized = phase == null || phase.isBlank() ? "REGISTRATION_OPEN" : phase.trim().toUpperCase(java.util.Locale.ROOT);
        return PHASES.stream()
                .filter(option -> option.key().equals(normalized))
                .findFirst()
                .orElse(PHASES.getFirst());
    }

    private void applyPhaseToLegacyToggles() {
        String phase = selectedPhase();
        registrationsToggle.setSelected("REGISTRATION_OPEN".equals(phase));
        votingToggle.setSelected("PUBLIC_VOTING_OPEN".equals(phase)
                || "JURY_VOTING_OPEN".equals(phase)
                || "PUBLIC_AND_JURY_VOTING_OPEN".equals(phase));
        if ("JURY_VOTING_OPEN".equals(phase) || "PUBLIC_AND_JURY_VOTING_OPEN".equals(phase)) {
            juryToggle.setSelected(true);
        }
        if (resultsToggle != null) {
            resultsToggle.setSelected("RESULTS_VISIBLE".equals(phase) || "ARCHIVED".equals(phase));
        }
        updateLabels();
    }

    private record PhaseOption(String key, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
