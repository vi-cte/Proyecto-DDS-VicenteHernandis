package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.dto.EventResponse;
import com.votify.frontend.dto.JuryTeamEvaluationRequest;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Controlador de la pantalla donde el usuario selecciona equipos para votar.
public class VotingFormController {
    private static final int FALLBACK_MAX_TEAMS_TO_VOTE = 3;
    private static final String SIMPLE_MODE = "SIMPLE";
    private static final String MULTICRITERIA_MODE = "MULTICRITERIA";
    private static final List<JuryCriterionDefinition> JURY_CRITERIA = List.of(
            new JuryCriterionDefinition("innovacion", "Innovación", "Originalidad y creatividad de la propuesta"),
            new JuryCriterionDefinition("viabilidad", "Viabilidad", "Factibilidad técnica y económica"),
            new JuryCriterionDefinition("impacto", "Impacto", "Potencial de transformación social"),
            new JuryCriterionDefinition("presentacion", "Presentación", "Claridad y calidad de la exposición")
    );

    private final VotifyApi apiClient = ApiClient.getInstance();
    private final Set<String> selectedTeamNames = new LinkedHashSet<>();
    private final Set<String> evaluatedJuryTeams = new LinkedHashSet<>();
    private final Map<String, String> teamComments = new LinkedHashMap<>();
    private final Map<String, Map<String, TextField>> teamJuryCriteriaFields = new LinkedHashMap<>();
    private String selectedJuryTeamName;

    private int maxTeamsToVote = FALLBACK_MAX_TEAMS_TO_VOTE;
    private String juryVotingMode = SIMPLE_MODE;

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

    @FXML
    private VBox jurySimpleBox;

    @FXML
    private VBox juryMulticriteriaBox;

    @FXML
    private ComboBox<String> juryTeamComboBox;

    @FXML
    private VBox juryCriteriaContainer;

    @FXML
    private TextArea juryWinnerCommentArea;

    @FXML
    private TextArea juryTechnicalCommentArea;

    @FXML
    private TextArea juryMulticriteriaCommentArea;

    private boolean juryMode;

    @FXML
    // Carga participantes, límite de votos y estado inicial de selección.
    private void initialize() {
        if (userNameLabel != null) {
            userNameLabel.setText(apiClient.getCurrentUserEmail()
                    + (apiClient.isCurrentUserJury() ? " · Jurado" : ""));
        }
        juryMode = apiClient.isCurrentUserJury();
        configureJuryListeners();

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
                    .filter(e -> e.isActive() && canVoteInSelectedRole(e))
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
        evaluatedJuryTeams.clear();
        teamComments.clear();
        selectedJuryTeamName = null;
        participantList.setDisable(false);
        submitButton.setDisable(false);
        if (juryVotingBox != null) {
            juryVotingBox.setVisible(false);
            juryVotingBox.setManaged(false);
        }
        if (jurySimpleBox != null) {
            jurySimpleBox.setVisible(false);
            jurySimpleBox.setManaged(false);
        }
        if (juryMulticriteriaBox != null) {
            juryMulticriteriaBox.setVisible(false);
            juryMulticriteriaBox.setManaged(false);
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
            var settings = apiClient.getVoteSettings(eventId);
            maxTeamsToVote = settings.getMaxTeamsToVote();
        } catch (ApiClientException ignored) {
            maxTeamsToVote = FALLBACK_MAX_TEAMS_TO_VOTE;
        }

        try {
            // Lee directamente del JSON para prevenir fallos si el DTO está desactualizado
            juryVotingMode = normalizeVotingMode(apiClient.getJuryVotingModeRaw(eventId));
        } catch (Exception ignored) {
            juryVotingMode = SIMPLE_MODE;
        }

        evaluatedJuryTeams.clear();
        if (juryMode && MULTICRITERIA_MODE.equals(juryVotingMode)) {
            try {
                evaluatedJuryTeams.addAll(apiClient.getEvaluatedJuryTeams(eventId));
            } catch (ApiClientException e) {
                System.err.println("Aviso: No se pudieron cargar los equipos ya evaluados: " + e.getMessage());
            }
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
        if (juryMode && MULTICRITERIA_MODE.equals(juryVotingMode)) {
            submitJuryMulticriteriaVote();
            return;
        }

        List<com.votify.frontend.dto.VoteSelectionRequest> selectedTeams = participantList.getItems().stream()
                .filter(item -> selectedTeamNames.contains(item.teamName()))
                .map(item -> new com.votify.frontend.dto.VoteSelectionRequest(
                        item.teamName(),
                        trimToNull(teamComments.get(item.teamName()))
                ))
                .toList();

        if (selectedTeams.isEmpty()) {
            showError("Selecciona al menos un equipo.");
            return;
        }

        try {
            apiClient.createVotes(selectedEventId(), selectedTeams, true);
            AlertHelper.showInfo("Votos registrados exitosamente.");
            goBack();
        } catch (ApiClientException e) {
            showError(e.getMessage());
        }
    }

    // Envía la evaluación multicriterio del jurado para todos los equipos.
    private void submitJuryMulticriteriaVote() {
        if (teamJuryCriteriaFields.isEmpty()) {
            showError("No hay equipos para evaluar.");
            return;
        }

        for (Map.Entry<String, Map<String, TextField>> entry : teamJuryCriteriaFields.entrySet()) {
            for (JuryCriterionDefinition criterion : JURY_CRITERIA) {
                TextField field = entry.getValue().get(criterion.key());
                String rawValue = field == null ? "" : field.getText();
                if (rawValue == null || rawValue.isBlank()) {
                    showError("Debes puntuar todos los criterios para todos los equipos.");
                    return;
                }
                try {
                    int score = Integer.parseInt(rawValue.trim());
                    if (score < 0 || score > 10) {
                        showError("Las puntuaciones deben estar entre 0 y 10.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showError("Las puntuaciones del jurado deben ser numéricas.");
                    return;
                }
            }
        }

        try {
            List<JuryTeamEvaluationRequest> evaluations = new ArrayList<>();
            for (Map.Entry<String, Map<String, TextField>> entry : teamJuryCriteriaFields.entrySet()) {
                String teamName = entry.getKey();
                List<com.votify.frontend.dto.JuryCriterionScoreRequest> scores = new ArrayList<>();

                for (JuryCriterionDefinition criterion : JURY_CRITERIA) {
                    TextField field = entry.getValue().get(criterion.key());
                    int score = Integer.parseInt(field.getText().trim());
                    scores.add(new com.votify.frontend.dto.JuryCriterionScoreRequest(criterion.key(), score));
                }
                evaluations.add(new JuryTeamEvaluationRequest(teamName, scores, null));
            }

            VoteResponse response = apiClient.createJuryMulticriteriaVotes(selectedEventId(), evaluations);
            evaluatedJuryTeams.addAll(response.getSelections());
            AlertHelper.showInfo("Evaluaciones del jurado registradas exitosamente.");
            clearMulticriteriaInputs();
            loadVotingData();
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
        if (juryMode && MULTICRITERIA_MODE.equals(juryVotingMode)) {
            boolean isValid = true;
            for (Map<String, TextField> fields : teamJuryCriteriaFields.values()) {
                for (TextField field : fields.values()) {
                    String text = field.getText();
                    if (text == null || text.isBlank()) {
                        isValid = false;
                        break;
                    }
                    try {
                        int score = Integer.parseInt(text.trim());
                        if (score < 0 || score > 10) {
                            isValid = false;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        isValid = false;
                        break;
                    }
                }
                if (!isValid) break;
            }
            selectionCountLabel.setText(selectedJuryTeamName == null
                    ? "Evaluación multicriterio"
                    : "Equipo seleccionado: " + selectedJuryTeamName);
            submitButton.setText("Guardar evaluación del jurado");
            submitButton.setDisable(!isValid || teamJuryCriteriaFields.isEmpty());
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
        if (!juryMode || SIMPLE_MODE.equals(juryVotingMode)) {
            hintLabel.setText("Selecciona hasta " + maxTeamsToVote + " equipos para votar");
            participantList.setVisible(true);
            participantList.setManaged(true);
            if (juryVotingBox != null) {
                juryVotingBox.setVisible(false);
                juryVotingBox.setManaged(false);
            }
            updateSelectionState();
            return;
        }

        participantList.setVisible(false);
        participantList.setManaged(false);
        juryVotingBox.setVisible(true);
        juryVotingBox.setManaged(true);
        
        if (MULTICRITERIA_MODE.equals(juryVotingMode)) {
            configureJuryMulticriteriaMode(items);
        }
    }

    // Configura la interfaz del jurado en modo multicriterio.
    private void configureJuryMulticriteriaMode(List<VoteCandidateItem> items) {
        hintLabel.setText("Evalúa todos los equipos. Haz clic en el nombre de un equipo para ver sus detalles.");
        
        // Autogenera los contenedores si no existen en el FXML
        if (juryCriteriaContainer == null) {
            juryCriteriaContainer = new VBox(16);
            if (juryMulticriteriaBox == null) {
                juryMulticriteriaBox = new VBox(12);
                juryMulticriteriaBox.getChildren().add(juryCriteriaContainer);
                if (juryVotingBox != null) {
                    juryVotingBox.getChildren().add(juryMulticriteriaBox);
                } else {
                    VBox parent = (VBox) participantList.getParent();
                    parent.getChildren().add(juryMulticriteriaBox);
                }
            } else {
                juryMulticriteriaBox.getChildren().add(juryCriteriaContainer);
            }
        }

        juryMulticriteriaBox.setVisible(true);
        juryMulticriteriaBox.setManaged(true);

        if (juryTeamComboBox != null) {
            juryTeamComboBox.setVisible(false);
            juryTeamComboBox.setManaged(false);
        }
        if (juryMulticriteriaCommentArea != null) {
            juryMulticriteriaCommentArea.setVisible(false);
            juryMulticriteriaCommentArea.setManaged(false);
        }

        buildJuryMulticriteriaAllTeams(items);
        updateSelectionState();
    }

    // Inicializa los listeners del modo jurado una única vez.
    private void configureJuryListeners() {
        if (juryWinnerComboBox != null) {
            juryWinnerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        }
        if (juryTechnicalComboBox != null) {
            juryTechnicalComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        }
        if (juryTeamComboBox != null) {
            juryTeamComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        }
        if (juryWinnerCommentArea != null) {
            juryWinnerCommentArea.textProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        }
        if (juryTechnicalCommentArea != null) {
            juryTechnicalCommentArea.textProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        }
        if (juryMulticriteriaCommentArea != null) {
            juryMulticriteriaCommentArea.textProperty().addListener((observable, oldValue, newValue) -> updateSelectionState());
        }
    }

    // Crea los campos de puntuación multicriterio para todos los equipos.
    private void buildJuryMulticriteriaAllTeams(List<VoteCandidateItem> items) {
        teamJuryCriteriaFields.clear();
        juryCriteriaContainer.getChildren().clear();

        for (VoteCandidateItem item : items) {
            if (evaluatedJuryTeams.contains(item.teamName())) {
                continue; // Evita mostrar equipos ya evaluados
            }

            VBox teamBox = new VBox(12);
            teamBox.getStyleClass().add("vote-row");
            teamBox.getStyleClass().add("jury-team-card");

            Label title = new Label(item.teamName());
            title.getStyleClass().add("vote-team-name");
            title.getStyleClass().add("team-link");
            title.setOnMouseClicked(event -> {
                event.consume();
                com.votify.frontend.ui.TeamInfoDialog.show(item.participant());
            });

            Label subtitle = new Label(item.subtitle());
            subtitle.getStyleClass().add("vote-team-subtitle");
            subtitle.setWrapText(true);

            VBox criteriaBox = new VBox(8);
            criteriaBox.setVisible(false);
            criteriaBox.setManaged(false);
            Map<String, TextField> criteriaFields = new java.util.LinkedHashMap<>();
            
            for (JuryCriterionDefinition criterion : JURY_CRITERIA) {
                Label critTitle = new Label(criterion.title());
                critTitle.getStyleClass().add("ranking-team-name");
                Label critDesc = new Label(criterion.description());
                critDesc.getStyleClass().add("vote-team-subtitle");
                critDesc.setWrapText(true);

                TextField scoreField = new TextField();
                scoreField.setPromptText("0-10");
                scoreField.getStyleClass().add("settings-number-field");
                scoreField.getStyleClass().add("jury-score-field");
                scoreField.textProperty().addListener((observable, oldValue, newValue) -> {
                    clearScoreFieldError(scoreField);
                    updateSelectionState();
                });
                scoreField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        validateScoreFieldStyle(scoreField);
                    }
                });
                criteriaFields.put(criterion.key(), scoreField);

                HBox row = new HBox(18);
                row.setAlignment(Pos.CENTER_LEFT);
                VBox textBox = new VBox(4, critTitle, critDesc);
                HBox.setHgrow(textBox, Priority.ALWAYS);
                row.getChildren().addAll(textBox, scoreField);
                criteriaBox.getChildren().add(row);
            }

            teamJuryCriteriaFields.put(item.teamName(), criteriaFields);
            teamBox.getChildren().addAll(title, subtitle, criteriaBox);
            teamBox.setOnMouseClicked(event -> selectJuryTeam(item.teamName()));
            teamBox.setUserData(item.teamName());
            juryCriteriaContainer.getChildren().add(teamBox);
        }

        if (!juryCriteriaContainer.getChildren().isEmpty()) {
            selectJuryTeam((String) juryCriteriaContainer.getChildren().getFirst().getUserData());
        }
    }

    // Marca visualmente el equipo seleccionado y despliega sus criterios.
    private void selectJuryTeam(String teamName) {
        selectedJuryTeamName = teamName;
        for (javafx.scene.Node child : juryCriteriaContainer.getChildren()) {
            boolean selected = teamName != null && teamName.equals(child.getUserData());
            child.getStyleClass().remove("jury-team-card-selected");
            if (selected) {
                child.getStyleClass().add("jury-team-card-selected");
            }
            if (child instanceof VBox otherTeamBox && otherTeamBox.getChildren().size() >= 3) {
                javafx.scene.Node otherCriteriaBox = otherTeamBox.getChildren().get(2);
                otherCriteriaBox.setVisible(selected);
                otherCriteriaBox.setManaged(selected);
            }
        }
    }

    // Marca en rojo una puntuación fuera del rango permitido cuando el usuario sale del campo.
    private void validateScoreFieldStyle(TextField field) {
        clearScoreFieldError(field);
        String text = field.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        try {
            int score = Integer.parseInt(text.trim());
            if (score < 0 || score > 10) {
                field.getStyleClass().add("jury-score-field-error");
            }
        } catch (NumberFormatException e) {
            field.getStyleClass().add("jury-score-field-error");
        }
    }

    private void clearScoreFieldError(TextField field) {
        field.getStyleClass().remove("jury-score-field-error");
    }

    // Limpia la selección y los campos numéricos del modo multicriterio.
    private void clearMulticriteriaInputs() {
        if (juryTeamComboBox != null) {
            juryTeamComboBox.getSelectionModel().clearSelection();
        }
        for (Map<String, TextField> fields : teamJuryCriteriaFields.values()) {
            for (TextField field : fields.values()) {
                field.clear();
            }
        }
        if (juryMulticriteriaCommentArea != null) {
            juryMulticriteriaCommentArea.clear();
        }
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

    // Normaliza el modo del jurado recibido del backend.
    private String normalizeVotingMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return SIMPLE_MODE;
        }
        return mode.trim().toUpperCase(Locale.ROOT);
    }

    private boolean canVoteInSelectedRole(EventResponse event) {
        String phase = event.getPhase() == null ? "" : event.getPhase().trim().toUpperCase(Locale.ROOT);
        if (juryMode) {
            return event.isJuryEnabled()
                    && ("JURY_VOTING_OPEN".equals(phase) || "PUBLIC_AND_JURY_VOTING_OPEN".equals(phase));
        }
        return "PUBLIC_VOTING_OPEN".equals(phase) || "PUBLIC_AND_JURY_VOTING_OPEN".equals(phase);
    }

    // Limpia un comentario y devuelve null cuando queda vacío.
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private final class VoteCandidateCell extends ListCell<VoteCandidateItem> {
        private final CheckBox checkBox = new CheckBox();
        private final Label titleLabel = new Label();
        private final Label subtitleLabel = new Label();
        private final TextArea commentArea = new TextArea();
        private final VBox textBox = new VBox(4.0);
        private final HBox headerRow = new HBox(12.0);
        private final VBox root = new VBox(12.0);

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

            commentArea.getStyleClass().add("vote-comment-area");
            commentArea.setPromptText("Añade un comentario (opcional)...");
            commentArea.setPrefRowCount(3);
            commentArea.setWrapText(true);
            commentArea.textProperty().addListener((observable, oldValue, newValue) -> {
                VoteCandidateItem currentItem = getItem();
                if (currentItem != null) {
                    teamComments.put(currentItem.teamName(), newValue);
                }
            });
            commentArea.setOnMouseClicked(event -> event.consume());

            textBox.getChildren().addAll(titleLabel, subtitleLabel);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.getChildren().addAll(checkBox, textBox);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().add("vote-row");
            root.getChildren().add(headerRow);
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
            boolean selected = selectedTeamNames.contains(item.teamName());
            checkBox.setSelected(selected);
            commentArea.setText(teamComments.getOrDefault(item.teamName(), ""));
            if (selected) {
                if (!root.getChildren().contains(commentArea)) {
                    root.getChildren().add(commentArea);
                }
            } else {
                root.getChildren().remove(commentArea);
            }
            setGraphic(root);
        }
    }

    // Datos compactos que alimentan una fila de votación.
    private record VoteCandidateItem(String teamName, String subtitle, ParticipantResponse participant) {
    }

    // Metadatos visuales de un criterio multicriterio.
    private record JuryCriterionDefinition(String key, String title, String description) {
    }
}
