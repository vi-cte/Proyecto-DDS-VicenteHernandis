package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.dto.EventResponse;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.results.ResultsViewData;
import com.votify.frontend.results.strategy.BarChartResultsViewStrategy;
import com.votify.frontend.results.strategy.PieChartResultsViewStrategy;
import com.votify.frontend.results.strategy.RankingResultsViewStrategy;
import com.votify.frontend.results.strategy.ResultsViewStrategy;
import com.votify.frontend.ui.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// Controlador de la pantalla de resultados y cambio de visualización.
public class ResultsFormController {
    private final VotifyApi apiClient = ApiClient.getInstance();
    private final Map<String, ResultsViewStrategy> strategies = Map.of(
            "ranking", new RankingResultsViewStrategy(),
            "bars", new BarChartResultsViewStrategy(),
            "pie", new PieChartResultsViewStrategy()
    );

    private ResultsViewData viewData;
    private boolean fromAdminDashboard = false;
    private boolean fromHistoryDashboard = false;

    @FXML
    private Label totalVotesLabel;

    @FXML
    private Label participantsCountLabel;

    @FXML
    private Label winnerLabel;

    @FXML
    private StackPane resultsContent;

    @FXML
    private HBox myTeamCardBox;

    @FXML
    private Label myTeamNameLabel;

    @FXML
    private Label myTeamPositionLabel;

    @FXML
    private Label myTeamVotesLabel;

    @FXML
    private Label myTeamCommentsLabel;

    @FXML
    private Button viewCommentsButton;

    @FXML
    private Button rankingButton;

    @FXML
    private Button barChartButton;

    @FXML
    private Button pieChartButton;

    @FXML
    private Button exitButton;

    @FXML
    private Label userNameLabel;

    @FXML
    private ComboBox<EventResponse> eventComboBox;

    @FXML
    private Label eventDescriptionLabel;

    @FXML
    private HBox descriptionCardBox;

    @FXML
    private Label eventStatusLabel;

    @FXML
    private HBox statusCardBox;

    @FXML
    private VBox myTeamCommentsBox;

    // Indica si se ha navegado desde el panel de administración
    public void setFromAdminDashboard(boolean fromAdmin) {
        this.fromAdminDashboard = fromAdmin;
        if (fromAdmin) {
            if (userNameLabel != null) {
                userNameLabel.setText("Administrador");
            }
            if (exitButton != null) {
                exitButton.setVisible(true);
                exitButton.setManaged(true);
            }
            loadResultEvents();
        }
    }

    // Indica si se ha navegado desde el histórico de eventos
    public void setFromHistoryDashboard(boolean fromHistory) {
        this.fromHistoryDashboard = fromHistory;
        this.fromAdminDashboard = fromHistory; // Hereda la visibilidad de los botones del admin
        if (fromHistory) {
            if (userNameLabel != null) {
                userNameLabel.setText("Administrador");
            }
            if (exitButton != null) {
                exitButton.setVisible(true);
                exitButton.setManaged(true);
            }
            loadResultEvents();
        }
    }

    @FXML
    // Carga resultados y prepara las estrategias de visualización.
    private void initialize() {
        String currentUserEmail = apiClient.getCurrentUserEmail();

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            if (userNameLabel != null) {
                userNameLabel.setText("Invitado");
            }
            if (exitButton != null) {
                exitButton.setVisible(false);
                exitButton.setManaged(false);
            }
        } else if (userNameLabel != null) {
            userNameLabel.setText(currentUserEmail);
        }

        if (eventComboBox != null) {
            eventComboBox.valueProperty().addListener((observable, oldValue, newValue) -> loadResultsData());
            loadResultEvents();
        } else {
            loadResultsData();
        }
    }

    // Carga eventos con resultados visibles para consultar.
    private void loadResultEvents() {
        try {
            List<EventResponse> events;
            if (fromAdminDashboard || fromHistoryDashboard) {
                events = apiClient.getAdminEvents().stream()
                        .filter(e -> fromHistoryDashboard ? !e.isActive() : e.isActive())
                        .map(a -> {
                            EventResponse e = new EventResponse();
                            e.setId(a.getId());
                            e.setName(a.getName());
                            e.setEventDate(a.getEventDate());
                            e.setDescription(a.getDescription());
                            e.setRegistrationsOpen(a.isRegistrationsOpen());
                            e.setVotingOpen(a.isVotingOpen());
                            e.setResultsVisible(a.isResultsVisible());
                            e.setMaxTeamsToVote(a.getMaxTeamsToVote());
                            e.setJuryEnabled(a.isJuryEnabled());
                            e.setActive(a.isActive());
                            return e;
                        })
                        .toList();
            } else {
                events = apiClient.getEvents().stream()
                        .filter(EventResponse::isResultsVisible)
                        .toList();
            }
            eventComboBox.setItems(FXCollections.observableArrayList(events));
            if (!events.isEmpty()) {
                eventComboBox.getSelectionModel().selectFirst();
            } else {
                totalVotesLabel.setText("0");
                participantsCountLabel.setText("0");
                winnerLabel.setText("Sin datos");
                resultsContent.getChildren().setAll(errorLabel("No hay eventos disponibles."));
            }
        } catch (ApiClientException e) {
            resultsContent.getChildren().setAll(errorLabel(e.getMessage()));
        }
    }

    // Preselecciona un evento específico cuando se navega desde el dashboard.
    public void loadEvent(Long eventId) {
        if (eventComboBox != null && eventId != null) {
            eventComboBox.getItems().stream()
                    .filter(e -> e.getId().equals(eventId))
                    .findFirst()
                    .ifPresent(e -> eventComboBox.getSelectionModel().select(e));
        }
    }

    // Recarga resultados y participantes del evento seleccionado.
    private void loadResultsData() {
        EventResponse selectedEvent = eventComboBox == null ? null : eventComboBox.getValue();
        if (descriptionCardBox != null && eventDescriptionLabel != null) {
            if (selectedEvent != null && selectedEvent.getDescription() != null && !selectedEvent.getDescription().isBlank()) {
                eventDescriptionLabel.setText(selectedEvent.getDescription());
                descriptionCardBox.setVisible(true);
                descriptionCardBox.setManaged(true);
            } else {
                descriptionCardBox.setVisible(false);
                descriptionCardBox.setManaged(false);
            }
        }

        if (statusCardBox != null && eventStatusLabel != null) {
            if (selectedEvent != null) {
                String statusText = "";
                String resPub = selectedEvent.isResultsVisible() ? "resultados publicados" : "resultados no publicados";
                String resVis = selectedEvent.isResultsVisible() ? "resultados públicos" : "resultados no públicos";

                if (!selectedEvent.isActive()) {
                    statusText = "Archivado con " + resPub;
                } else if (selectedEvent.isRegistrationsOpen()) {
                    statusText = "Activo en fase de registro de participantes con " + resVis;
                } else if (selectedEvent.isVotingOpen()) {
                    statusText = "Activo en fase de votación con " + resVis;
                } else {
                    statusText = "Finalizado con " + resPub;
                }
                
                eventStatusLabel.setText(statusText);
                statusCardBox.setVisible(true);
                statusCardBox.setManaged(true);
            } else {
                statusCardBox.setVisible(false);
                statusCardBox.setManaged(false);
            }
        }

        Long eventId = selectedEventId();
        try {
            ResultsResponse response = apiClient.getResults(eventId);
            List<com.votify.frontend.dto.ParticipantResponse> allParticipants = apiClient.getParticipantResponses(eventId);
            int participantCount = allParticipants.size();

            List<ResultItemResponse> ranking = new java.util.ArrayList<>(response.getResults() == null ? List.of() : response.getResults());
            List<ResultItemResponse> publicRanking = new java.util.ArrayList<>(response.getPublicResults() == null || response.getPublicResults().isEmpty() ? ranking : response.getPublicResults());
            List<ResultItemResponse> juryRanking = new java.util.ArrayList<>(response.getJuryResults() == null ? List.of() : response.getJuryResults());

            for (com.votify.frontend.dto.ParticipantResponse p : allParticipants) {
                if (ranking.stream().noneMatch(r -> r.getTeamName().equals(p.getTeamName()))) {
                    ranking.add(new ResultItemResponse(p.getTeamName(), 0L));
                }
                if (publicRanking.stream().noneMatch(r -> r.getTeamName().equals(p.getTeamName()))) {
                    publicRanking.add(new ResultItemResponse(p.getTeamName(), 0L));
                }
                if (juryRanking.stream().noneMatch(r -> r.getTeamName().equals(p.getTeamName()))) {
                    juryRanking.add(new ResultItemResponse(p.getTeamName(), 0L));
                }
            }

            ranking.sort(Comparator.comparingLong(ResultItemResponse::getVotes).reversed());
            publicRanking.sort(Comparator.comparingLong(ResultItemResponse::getVotes).reversed());
            juryRanking.sort(Comparator.comparingLong(ResultItemResponse::getVotes).reversed());

            boolean isMulticriteria = false;
            try {
                isMulticriteria = "MULTICRITERIA".equals(apiClient.getVoteSettings(eventId).getJuryVotingMode());
            } catch (ApiClientException ignored) {}

            viewData = new ResultsViewData(response, publicRanking, juryRanking, participantCount, isMulticriteria);
            bindSummary(viewData, isMulticriteria);
            selectStrategy("ranking");
        } catch (ApiClientException e) {
            totalVotesLabel.setText("0");
            participantsCountLabel.setText("0");
            winnerLabel.setText("Sin datos");
            resultsContent.getChildren().setAll(errorLabel(e.getMessage()));
        }
    }

    @FXML
    // Selecciona la vista de ranking.
    private void showRanking() {
        selectStrategy("ranking");
    }

    @FXML
    // Selecciona la vista de gráfico de barras.
    private void showBarChart() {
        selectStrategy("bars");
    }

    @FXML
    // Selecciona la vista de gráfico circular.
    private void showPieChart() {
        selectStrategy("pie");
    }

    @FXML
    // Muestra u oculta la lista de comentarios del equipo del usuario autenticado.
    private void showTeamComments() {
        if (viewData == null) {
            return;
        }
        if (myTeamCommentsBox != null && myTeamCommentsBox.isVisible()) {
            myTeamCommentsBox.setVisible(false);
            myTeamCommentsBox.setManaged(false);
            viewCommentsButton.setText("Ver comentarios");
        } else if (myTeamCommentsBox != null) {
            myTeamCommentsBox.getChildren().setAll(buildCommentsView().getChildren());
            myTeamCommentsBox.setVisible(true);
            myTeamCommentsBox.setManaged(true);
            viewCommentsButton.setText("Ocultar comentarios");
        }
    }

    @FXML
    // Vuelve al menú o a la pantalla de acceso según la sesión.
    private void closeResults() {
        try {
            if (fromHistoryDashboard) {
                SceneNavigator.showScene(
                        (Stage) resultsContent.getScene().getWindow(),
                        "/com/votify/frontend/view/HistoryDashboard.fxml",
                        "/com/votify/frontend/view/MainMenu.css",
                        "Votify - Histórico de Eventos"
                );
                return;
            }
            if (fromAdminDashboard) {
                SceneNavigator.showScene(
                        (Stage) resultsContent.getScene().getWindow(),
                        "/com/votify/frontend/view/AdminDashboard.fxml",
                        "/com/votify/frontend/view/MainMenu.css",
                        "Votify - Administración"
                );
                return;
            }

            String currentUserEmail = apiClient.getCurrentUserEmail();
            if (currentUserEmail == null || currentUserEmail.isBlank()) {
                SceneNavigator.showScene(
                        (Stage) resultsContent.getScene().getWindow(),
                        "/com/votify/frontend/view/Access.fxml",
                        "/com/votify/frontend/view/MainMenu.css",
                        "Votify - Acceso"
                );
            } else {
                SceneNavigator.showMainMenu((Stage) resultsContent.getScene().getWindow());
            }
        } catch (IOException e) {
            AlertHelper.showError("No se pudo volver a la pantalla anterior: " + e.getMessage());
        }
    }

    @FXML
    // Cierra la sesión local y vuelve a la pantalla de acceso.
    private void exit() {
        ApiClient.getInstance().logout();
        try {
            SceneNavigator.showScene(
                    (Stage) resultsContent.getScene().getWindow(),
                    "/com/votify/frontend/view/Access.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Acceso"
            );
        } catch (IOException e) {
            AlertHelper.showError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    // Actualiza los indicadores resumen de la pantalla.
    private void bindSummary(ResultsViewData data, boolean isMulticriteria) {
        String juryUnit = isMulticriteria ? "puntos" : "votos";
        totalVotesLabel.setText(data.response().getTotalPublicVotes() + " votos público · "
                + data.response().getTotalJuryVotes() + " " + juryUnit + " jurado");
        participantsCountLabel.setText(Integer.toString(data.participantCount()));
        String publicWinner = data.winner() == null ? "Sin público" : data.winner().getTeamName();
        String juryWinner = data.juryWinner() == null ? "Sin jurado" : data.juryWinner().getTeamName();
        winnerLabel.setText(publicWinner + " / " + juryWinner);

        boolean hasMyTeam = data.myTeam() != null && data.myTeam().getTeamName() != null && !data.myTeam().getTeamName().isBlank();
        myTeamCardBox.setVisible(hasMyTeam);
        myTeamCardBox.setManaged(hasMyTeam);
        if (!hasMyTeam) {
            return;
        }
        myTeamNameLabel.setText(data.myTeam().getTeamName());
        myTeamPositionLabel.setText(data.myTeam().getPosition() <= 0 ? "-" : data.myTeam().getPosition() + "º");
        myTeamVotesLabel.setText(Long.toString(data.myTeam().getVotes()));
        myTeamCommentsLabel.setText(Long.toString(data.myTeam().getCommentsCount()));
        boolean hasComments = data.myTeam().getComments() != null && !data.myTeam().getComments().isEmpty();
        viewCommentsButton.setDisable(!hasComments);
        
        // Ocultar caja de comentarios por defecto al recargar o cambiar de evento
        if (myTeamCommentsBox != null) {
            myTeamCommentsBox.setVisible(false);
            myTeamCommentsBox.setManaged(false);
            viewCommentsButton.setText("Ver comentarios");
        }
    }

    // Renderiza la estrategia seleccionada y marca su botón.
    private void selectStrategy(String strategyId) {
        ResultsViewStrategy strategy = strategies.get(strategyId);
        if (strategy == null || viewData == null) {
            return;
        }

        rankingButton.getStyleClass().setAll("view-tab-button");
        barChartButton.getStyleClass().setAll("view-tab-button");
        pieChartButton.getStyleClass().setAll("view-tab-button");

        if ("ranking".equals(strategyId)) {
            rankingButton.getStyleClass().add("view-tab-button-active");
        } else if ("bars".equals(strategyId)) {
            barChartButton.getStyleClass().add("view-tab-button-active");
        } else if ("pie".equals(strategyId)) {
            pieChartButton.getStyleClass().add("view-tab-button-active");
        }

        resultsContent.setAlignment(Pos.TOP_CENTER);
        resultsContent.getChildren().setAll(strategy.buildView(viewData));
    }

    // Construye una etiqueta de error para mostrar en el contenido.
    private Label errorLabel(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("results-empty");
        return label;
    }

    // Construye la vista de comentarios del equipo del usuario.
    private VBox buildCommentsView() {
        VBox container = new VBox(18);
        container.getStyleClass().add("results-ranking-list");

        if (viewData.myTeam() == null || viewData.myTeam().getComments() == null || viewData.myTeam().getComments().isEmpty()) {
            container.getChildren().add(errorLabel("Todavía no hay comentarios para tu equipo."));
            return container;
        }

        for (var comment : viewData.myTeam().getComments()) {
            VBox card = new VBox(10);
            card.getStyleClass().add("team-comment-card");

            Label author = new Label(comment.getAuthorLabel() == null ? "Votante anónimo" : comment.getAuthorLabel());
            author.getStyleClass().add("vote-team-name");

            Label time = new Label(formatRelativeTime(comment.getCreatedAt()));
            time.getStyleClass().add("vote-team-subtitle");

            Label content = new Label(comment.getComment());
            content.getStyleClass().add("team-comment-text");
            content.setWrapText(true);

            card.getChildren().addAll(author, time, content);
            container.getChildren().add(card);
        }
        return container;
    }

    // Devuelve una etiqueta relativa simple para la fecha del comentario.
    private String formatRelativeTime(String rawInstant) {
        if (rawInstant == null || rawInstant.isBlank()) {
            return "Hace un momento";
        }
        try {
            Duration duration = Duration.between(Instant.parse(rawInstant), Instant.now()).abs();
            long minutes = Math.max(1, duration.toMinutes());
            if (minutes < 60) {
                return "Hace " + minutes + " min";
            }
            long hours = duration.toHours();
            if (hours < 24) {
                return "Hace " + hours + " horas";
            }
            long days = duration.toDays();
            return "Hace " + days + " días";
        } catch (Exception ignored) {
            return "Comentario reciente";
        }
    }

    // Devuelve el evento seleccionado.
    private Long selectedEventId() {
        EventResponse event = eventComboBox == null ? null : eventComboBox.getValue();
        return event == null ? null : event.getId();
    }
}
