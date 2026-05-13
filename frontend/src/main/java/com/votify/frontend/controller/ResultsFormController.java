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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
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

    @FXML
    private Label totalVotesLabel;

    @FXML
    private Label participantsCountLabel;

    @FXML
    private Label winnerLabel;

    @FXML
    private StackPane resultsContent;

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
            List<EventResponse> events = apiClient.getEvents().stream()
                    .filter(EventResponse::isResultsVisible)
                    .toList();
            eventComboBox.setItems(FXCollections.observableArrayList(events));
            if (!events.isEmpty()) {
                eventComboBox.getSelectionModel().selectFirst();
            } else {
                totalVotesLabel.setText("0");
                participantsCountLabel.setText("0");
                winnerLabel.setText("Sin datos");
                resultsContent.getChildren().setAll(errorLabel("No hay eventos con resultados visibles."));
            }
        } catch (ApiClientException e) {
            resultsContent.getChildren().setAll(errorLabel(e.getMessage()));
        }
    }

    // Recarga resultados y participantes del evento seleccionado.
    private void loadResultsData() {
        Long eventId = selectedEventId();
        try {
            ResultsResponse response = apiClient.getResults(eventId);
            int participantCount = apiClient.getParticipantResponses(eventId).size();
            List<ResultItemResponse> ranking = response.getResults() == null
                    ? List.of()
                    : response.getResults().stream()
                    .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                    .toList();
            List<ResultItemResponse> publicRanking = response.getPublicResults() == null || response.getPublicResults().isEmpty()
                    ? ranking
                    : response.getPublicResults().stream()
                    .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                    .toList();
            List<ResultItemResponse> juryRanking = response.getJuryResults() == null
                    ? List.of()
                    : response.getJuryResults().stream()
                    .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                    .toList();

            viewData = new ResultsViewData(response, publicRanking, juryRanking, participantCount);
            bindSummary(viewData);
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
    // Vuelve al menú o a la pantalla de acceso según la sesión.
    private void closeResults() {
        try {
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
    private void bindSummary(ResultsViewData data) {
        totalVotesLabel.setText(data.response().getTotalPublicVotes() + " público · "
                + data.response().getTotalJuryVotes() + " jurado");
        participantsCountLabel.setText(Integer.toString(data.participantCount()));
        String publicWinner = data.winner() == null ? "Sin público" : data.winner().getTeamName();
        String juryWinner = data.juryWinner() == null ? "Sin jurado" : data.juryWinner().getTeamName();
        winnerLabel.setText(publicWinner + " / " + juryWinner);
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

    // Devuelve el evento seleccionado.
    private Long selectedEventId() {
        EventResponse event = eventComboBox == null ? null : eventComboBox.getValue();
        return event == null ? null : event.getId();
    }
}
