package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Controlador del formulario de creación de evento.
public class CreateEventDialogController {
    @FXML private TextField nameField;
    @FXML private DatePicker eventDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox registrationsToggle;
    @FXML private CheckBox votingToggle;
    @FXML private CheckBox juryToggle;
    @FXML private TextField maxVotesField;
    @FXML private Label registrationsLabel;
    @FXML private Label votingLabel;
    @FXML private Label juryLabel;

    @FXML
    private void updateLabels() {
        if (registrationsToggle.isSelected()) {
            votingToggle.setSelected(false);
        }
        registrationsLabel.setText(registrationsToggle.isSelected() ? "Abiertas" : "Cerradas");
        votingLabel.setText(votingToggle.isSelected() ? "Abiertas" : "Cerradas");
        juryLabel.setText(juryToggle.isSelected() ? "Activado" : "Desactivado");
    }

    @FXML
    private void createEvent() {
        try {
            int maxVotes = Integer.parseInt(maxVotesField.getText().trim());
            String date = eventDatePicker.getValue() == null ? null : eventDatePicker.getValue().toString();
            ApiClient.getInstance().createAdminEvent(
                    nameField.getText(),
                    date,
                    descriptionArea.getText(),
                    registrationsToggle.isSelected(),
                    votingToggle.isSelected(),
                    false,
                    maxVotes,
                    juryToggle.isSelected()
            );
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            AlertHelper.showError("El número de votos debe ser numérico.");
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
        }
    }
}
