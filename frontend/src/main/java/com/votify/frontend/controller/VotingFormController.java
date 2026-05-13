package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.dto.EventResponse;
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
import javafx.scene.control.ComboBox;
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

// Controlador de la pantalla donde el usuario selecciona equipos para votar.
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
    private ComboBox<EventResponse> eventComboBox;

    @FXML
    private VBox juryVotingBox;

    @FXML
    private ComboBox<String> juryWinnerComboBox;

    @FXML
    private ComboBox<String> juryTechnicalComboBox;

    private boolean juryMode;

    @FXML
    // Carga participantes, límite de votos y estado inicial de selección.
    private void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText(apiClient.getCurrentUserEmail()
                    + (apiClient.isCurrentUserJury() ? " · Jurado" : ""));
        }
        juryMode = apiClient.isCurrentUserJury();

        participantList.setCellFactory(listView -> new VoteCandidateCell());
        if (eventComboBox != null) {
            eventComboBox.valueProperty().addListener((observable, oldValue, newValue) -> loadVotingData());
            loadVotingEvents();
        } else {
            loadVotingData();
        }
    }

    // Carga eventos con votacion abierta para que el usuario elija donde votar.
    private void loadVotingEvents() {
        try {
            List<EventResponse> events = apiClient.getEvents().stream()
                    .filter(EventResponse::isVotingOpen)
                    .toList();
            eventComboBox.setItems(FXCollections.observableArrayList(events));
            if (!events.isEmpty()) {
                eventComboBox.getSelectionModel().selectFirst();
            } else {
                hintLabel.setText("No hay eventos con votación abierta.");
                submitButton.setDisable(true);
                participantList.setDisable(true);
            }
        } catch (ApiClientException e) {
            hintLabel.setText("No se pudo cargar la lista de eventos.");
            submitButton.setDisable(true);
            participantList.setDisable(true);
            showError(e.getMessage());
        }
    }

    // Recarga limite, candidatos y estado de voto para el evento seleccionado.
    private void loadVotingData() {
        Long eventId = selectedEventId();
        selectedTeamNames.clear();
        participantList.setDisable(false);
        submitButton.setDisable(false);
        if (juryVotingBox != null) {
            juryVotingBox.setVisible(false);
            juryVotingBox.setManaged(false);
        }
        participantList.setVisible(true);
        participantList.setManaged(true);

        try {
            if (apiClient.hasVoted(eventId)) {
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
            maxTeamsToVote = apiClient.getVotingLimit(eventId);
        } catch (ApiClientException ignored) {
            maxTeamsToVote = FALLBACK_MAX_TEAMS_TO_VOTE;
        }

        try {
            List<VoteCandidateItem> items = apiClient.getParticipantResponses(eventId).stream()
                    .filter(participant -> participant.getTeamName() != null && !participant.getTeamName().isBlank())
                    .map(participant -> new VoteCandidateItem(
                            participant.getTeamName(),
                            buildSubtitle(participant),
                            participant
                    ))
                    .toList();

            participantList.setItems(FXCollections.observableArrayList(items));
            configureVotingMode(items);

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
    // Envía las selecciones actuales como voto.
    private void submitVote() {
        if (juryMode) {
            submitJuryVote();
            return;
        }

        List<String> selectedTeams = participantList.getItems().stream()
                .map(VoteCandidateItem::teamName)
                .filter(selectedTeamNames::contains)
                .toList();

        if (selectedTeams.isEmpty()) {
            showError("Selecciona al menos un equipo.");
            return;
        }

        try {
            VoteResponse response = apiClient.createVotes(selectedEventId(), selectedTeams);
            AlertHelper.showInfo("Votos registrados: " + response.getRecordedVotes());
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    // Envía las dos categorías del voto de jurado.
    private void submitJuryVote() {
        String winnerSelection = juryWinnerComboBox.getValue();
        String technicalSelection = juryTechnicalComboBox.getValue();

        if (winnerSelection == null || winnerSelection.isBlank()
                || technicalSelection == null || technicalSelection.isBlank()) {
            showError("Selecciona un equipo en cada categoría del jurado.");
            return;
        }

        try {
            VoteResponse response = apiClient.createJuryVotes(selectedEventId(), winnerSelection, technicalSelection);
            AlertHelper.showInfo("Votos del jurado registrados: " + response.getRecordedVotes());
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // Vuelve al menú principal.
    private void goBack() {
        try {
            SceneNavigator.showMainMenu(currentStage());
        } catch (IOException e) {
            showError("No se pudo volver al panel principal: " + e.getMessage());
        }
    }

    @FXML
    // Cierra la sesión local y vuelve a acceso.
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

    // Añade o quita un equipo de la selección actual.
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

    // Actualiza contador, botón y refresco visual de selección.
    private void updateSelectionState() {
        if (juryMode) {
            boolean ready = juryWinnerComboBox.getValue() != null && juryTechnicalComboBox.getValue() != null;
            selectionCountLabel.setText(ready ? "2 / 2" : "0 / 2");
            submitButton.setText("Enviar valoración del jurado");
            submitButton.setDisable(!ready);
            return;
        }
        int selectedCount = selectedTeamNames.size();
        selectionCountLabel.setText(selectedCount + " / " + maxTeamsToVote);
        submitButton.setText("Enviar Votos (" + selectedCount + ")");
        submitButton.setDisable(selectedCount == 0);
    }

    // Construye el subtítulo mostrado debajo del nombre del equipo.
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

    // Prepara la pantalla pública o la interfaz especial del jurado.
    private void configureVotingMode(List<VoteCandidateItem> items) {
        if (!juryMode) {
            hintLabel.setText("Selecciona hasta " + maxTeamsToVote + " equipos para votar");
            return;
        }

        hintLabel.setText("Selecciona el ganador del jurado y la mención técnica");
        participantList.setVisible(false);
        participantList.setManaged(false);
        juryVotingBox.setVisible(true);
        juryVotingBox.setManaged(true);
        selectionCountLabel.setText("0 / 2");
        submitButton.setText("Enviar valoración del jurado");

        List<String> teamNames = items.stream().map(VoteCandidateItem::teamName).toList();
        juryWinnerComboBox.setItems(FXCollections.observableArrayList(teamNames));
        juryTechnicalComboBox.setItems(FXCollections.observableArrayList(teamNames));
        juryWinnerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        juryTechnicalComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
    }

    // Obtiene el Stage actual desde un nodo de la pantalla.
    private Stage currentStage() {
        return (Stage) submitButton.getScene().getWindow();
    }

    // Muestra un error en la zona superior del formulario.
    private void showError(String message) {
        AlertHelper.showError(message);
    }

    // Devuelve el evento seleccionado en la pantalla.
    private Long selectedEventId() {
        EventResponse event = eventComboBox == null ? null : eventComboBox.getValue();
        return event == null ? null : event.getId();
    }

    private final class VoteCandidateCell extends ListCell<VoteCandidateItem> {
        private final CheckBox checkBox = new CheckBox();
        private final Label titleLabel = new Label();
        private final Label subtitleLabel = new Label();
        private final VBox textBox = new VBox(4.0);
        private final HBox root = new HBox(12.0);

        // Configura la celda visual de un candidato votable.
        private VoteCandidateCell() {
            checkBox.getStyleClass().add("vote-checkbox");
            checkBox.setFocusTraversable(false);
            checkBox.setMouseTransparent(true);

            titleLabel.getStyleClass().add("vote-team-name");
            titleLabel.getStyleClass().add("team-link");
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
        // Refresca la celda cuando cambia el candidato mostrado.
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

    // Datos compactos que alimentan una fila de votación.
    private record VoteCandidateItem(String teamName, String subtitle, ParticipantResponse participant) {
    }
}
