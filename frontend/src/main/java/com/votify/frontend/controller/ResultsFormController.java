package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.VotifyApi;
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
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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

        try {
            ResultsResponse response = apiClient.getResults();
            int participantCount = apiClient.getParticipantResponses().size();
            List<ResultItemResponse> ranking = response.getResults() == null
                    ? List.of()
                    : response.getResults().stream()
                    .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                    .toList();

            viewData = new ResultsViewData(response, ranking, participantCount);
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
        totalVotesLabel.setText(Long.toString(data.response().getTotalVotes()));
        participantsCountLabel.setText(Integer.toString(data.participantCount()));
        winnerLabel.setText(data.winner() == null ? "Sin datos" : data.winner().getTeamName());
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
}
