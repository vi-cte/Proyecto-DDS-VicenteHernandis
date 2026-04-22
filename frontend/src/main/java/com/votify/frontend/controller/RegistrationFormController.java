package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RegistrationFormController {
    private final ApiClient apiClient = new ApiClient();

    @FXML
    private TextField teamField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField memberInputField;

    @FXML
    private ListView<String> membersListView;

    private final ObservableList<String> membersList = FXCollections.observableArrayList();

    @FXML
    private Label teamErrorLabel;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private Label logoLabel;

    @FXML
    private ImageView logoImageView;

    private String logoBase64;

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

        membersListView.setItems(membersList);
        membersListView.setCellFactory(lv -> new MemberCell());
    }

    @FXML
    private void uploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Logo del Equipo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(teamField.getScene().getWindow());
        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                logoBase64 = Base64.getEncoder().encodeToString(bytes);
                if (logoLabel != null) {
                    logoLabel.setText(file.getName());
                }
                if (logoImageView != null) {
                    logoImageView.setImage(new Image(file.toURI().toString()));
                }
            } catch (IOException e) {
                showError("No se pudo leer la imagen: " + e.getMessage());
            }
        }
    }

    @FXML
    private void registerParticipant() {
        String team = teamField.getText().trim();
        String email = emailField.getText().trim();
        String description = descriptionArea != null && descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";
        String phone = phoneField.getText().trim();
        List<String> members = new ArrayList<>(membersList);

        if (!validateForm()) {
            return;
        }

        try {
            apiClient.createParticipant(team, email, phone, description, logoBase64, members);
            AlertHelper.showInfo("Participante registrado: " + team);
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void addMember() {
        String memberName = memberInputField.getText().trim();
        if (!memberName.isEmpty()) {
            membersList.add(memberName);
            memberInputField.clear();
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }


    @FXML
    private void goBack() {
        try {
            SceneNavigator.showMainMenu((Stage) teamField.getScene().getWindow());
        } catch (IOException e) {
            showError("No se pudo volver al panel principal: " + e.getMessage());
        }
    }

    private void showError(String message) {
        AlertHelper.showError(message);
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

    private class MemberCell extends ListCell<String> {
        private final HBox hbox = new HBox();
        private final Label label = new Label();
        private final Button editButton = new Button("Editar");
        private final Button removeButton = new Button("Quitar");

        public MemberCell() {
            super();
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            hbox.getChildren().addAll(label, spacer, editButton, removeButton);
            hbox.setAlignment(Pos.CENTER_LEFT);
            label.setStyle("-fx-text-fill: #162642; -fx-font-size: 14px;");
            hbox.setSpacing(10);
            editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b88ff; -fx-font-weight: bold; -fx-cursor: hand;");
            removeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e53935; -fx-font-weight: bold; -fx-cursor: hand;");
            setStyle("-fx-padding: 5; -fx-background-color: transparent; -fx-border-color: transparent transparent #e2e8f2 transparent; -fx-border-width: 0 0 1 0;");

            editButton.setOnAction(event -> {
                String item = getItem();
                if (item != null) {
                    memberInputField.setText(item);
                    membersList.remove(item);
                    memberInputField.requestFocus();
                }
            });

            removeButton.setOnAction(event -> {
                String item = getItem();
                if (item != null) {
                    membersList.remove(item);
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                label.setText(item);
                setGraphic(hbox);
            }
        }
    }
}
