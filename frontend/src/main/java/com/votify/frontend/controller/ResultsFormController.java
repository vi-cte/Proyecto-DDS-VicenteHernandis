package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResultsFormController {
    private final ApiClient apiClient = new ApiClient();
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
    private void initialize() {
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
    private void showRanking() {
        selectStrategy("ranking");
    }

    @FXML
    private void showBarChart() {
        selectStrategy("bars");
    }

    @FXML
    private void showPieChart() {
        selectStrategy("pie");
    }

    @FXML
    private void closeResults() {
        try {
            SceneNavigator.showMainMenu((javafx.stage.Stage) resultsContent.getScene().getWindow());
        } catch (IOException e) {
            AlertHelper.showError("No se pudo volver al menú principal: " + e.getMessage());
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    private void bindSummary(ResultsViewData data) {
        totalVotesLabel.setText(Long.toString(data.response().getTotalVotes()));
        participantsCountLabel.setText(Integer.toString(data.participantCount()));
        winnerLabel.setText(data.winner() == null ? "Sin datos" : data.winner().getTeamName());
    }

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

    private Label errorLabel(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("results-empty");
        return label;
    }
}
