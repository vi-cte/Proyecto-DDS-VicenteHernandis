package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import com.votify.frontend.client.FormValidators;
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
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

// Controlador del formulario de creación y edición de equipos.
public class RegistrationFormController {
    private final VotifyApi apiClient = ApiClient.getInstance();

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
    private Label userNameLabel;

    @FXML
    private Button submitButton;

    private Long editingParticipantId = null;
    private String originalTeamName = null;

    @FXML
    // Inicializa validaciones, lista de miembros y datos existentes.
    private void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText(apiClient.getCurrentUserEmail());
        }

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
        
        loadExistingParticipant();
    }

    // Carga el equipo del usuario si ya tenía uno registrado.
    private void loadExistingParticipant() {
        try {
            ParticipantResponse p = apiClient.getCurrentParticipant();
            if (p == null) {
                return;
            }

            editingParticipantId = p.getId();
            originalTeamName = p.getTeamName();

            teamField.setText(p.getTeamName());
            emailField.setText(p.getEmail());
            if (p.getPhone() != null) phoneField.setText(p.getPhone());
            if (p.getDescription() != null && descriptionArea != null) descriptionArea.setText(p.getDescription());
            if (p.getMembers() != null) membersList.setAll(p.getMembers());

            if (p.getLogo() != null && !p.getLogo().isBlank()) {
                logoBase64 = p.getLogo();
                try {
                    byte[] imgBytes = Base64.getDecoder().decode(logoBase64);
                    if (logoImageView != null) logoImageView.setImage(new Image(new ByteArrayInputStream(imgBytes)));
                    if (logoLabel != null) logoLabel.setText("Logo cargado");
                } catch (Exception ignored) {
                }
            }

            if (submitButton != null) {
                submitButton.setText("Editar equipo");
            }
        } catch (ApiClientException ignored) {}
    }

    @FXML
    // Abre un selector de archivo y carga el logo en Base64.
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
    // Valida el formulario y crea o actualiza el equipo.
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
            if (editingParticipantId != null) {
                apiClient.updateParticipant(editingParticipantId, team, email, phone, description, logoBase64, members);
                AlertHelper.showInfo("Participante actualizado: " + team);
            } else {
                apiClient.createParticipant(team, email, phone, description, logoBase64, members);
                AlertHelper.showInfo("Participante registrado: " + team);
            }
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // Añade un miembro nuevo a la lista del equipo.
    private void addMember() {
        String memberName = memberInputField.getText().trim();
        if (!memberName.isEmpty()) {
            membersList.add(memberName);
            memberInputField.clear();
        }
    }

    @FXML
    // Cierra la sesión local y vuelve a acceso.
    private void exit() {
        ApiClient.getInstance().logout();
        try {
            SceneNavigator.showScene(
                    (Stage) teamField.getScene().getWindow(),
                    "/com/votify/frontend/view/Access.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Acceso"
            );
        } catch (IOException e) {
            showError("Error al cerrar sesión: " + e.getMessage());
        }
    }


    @FXML
    // Vuelve al menú principal.
    private void goBack() {
        try {
            SceneNavigator.showMainMenu((Stage) teamField.getScene().getWindow());
        } catch (IOException e) {
            showError("No se pudo volver al panel principal: " + e.getMessage());
        }
    }

    // Muestra un error en el formulario.
    private void showError(String message) {
        AlertHelper.showError(message);
    }

    // Valida todos los campos obligatorios del formulario.
    private boolean validateForm() {
        boolean teamOk = validateTeamField();
        boolean emailOk = validateEmailField();
        return teamOk && emailOk;
    }

    // Valida el nombre del equipo y comprueba duplicados.
    private boolean validateTeamField() {
        String team = teamField.getText() == null ? "" : teamField.getText().trim();
        if (team.isEmpty()) {
            teamErrorLabel.setText(FormValidators.MSG_TEAM_REQUIRED);
            return false;
        }
        try {
            boolean checkExists = true;
            if (editingParticipantId != null && team.equalsIgnoreCase(originalTeamName)) {
                checkExists = false; // No comprobar si el nombre no ha cambiado
            }
            if (checkExists && apiClient.teamNameExists(team)) {
                teamErrorLabel.setText(FormValidators.MSG_TEAM_EXISTS);
                return false;
            }
        } catch (ApiClientException e) {
            teamErrorLabel.setText("No se pudo validar el nombre en el servidor.");
            return false;
        }
        teamErrorLabel.setText("");
        return true;
    }

    // Valida que el correo tenga formato correcto.
    private boolean validateEmailField() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty()) {
            emailErrorLabel.setText(FormValidators.MSG_EMAIL_REQUIRED);
            return false;
        }
        if (!FormValidators.isValidEmail(email)) {
            emailErrorLabel.setText(FormValidators.MSG_INVALID_EMAIL);
            return false;
        }
        emailErrorLabel.setText("");
        return true;
    }

    private class MemberCell extends ListCell<String> {
        private final HBox hbox = new HBox();
        private final Label label = new Label();
        private final Button editButton = new Button("Editar");
        private final Button removeButton = new Button("Quitar");

        // Configura la celda editable de un miembro.
        public MemberCell() {
            super();
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            hbox.getChildren().addAll(label, spacer, editButton, removeButton);
            hbox.setAlignment(Pos.CENTER_LEFT);
            label.getStyleClass().add("member-cell-label");
            hbox.setSpacing(10);
            editButton.getStyleClass().addAll("member-cell-button", "member-cell-edit-button");
            removeButton.getStyleClass().addAll("member-cell-button", "member-cell-remove-button");
            getStyleClass().add("member-list-cell");

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
        // Refresca la celda cuando cambia el miembro mostrado.
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
