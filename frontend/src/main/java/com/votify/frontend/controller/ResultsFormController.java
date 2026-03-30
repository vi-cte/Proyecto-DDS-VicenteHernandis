package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.exception.ApiClientException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ResultsFormController {
    private final ApiClient apiClient = new ApiClient();

    @FXML
    private Label summaryLabel;

    @FXML
    private TextArea resultsArea;

    @FXML
    private void initialize() {
        try {
            ResultsResponse response = apiClient.getResults();
            if (response.getResults() == null || response.getResults().isEmpty()) {
                summaryLabel.setText("Todavia no se ha emitido ningun voto.");
                resultsArea.setText("No hay resultados para mostrar.");
                return;
            }

            summaryLabel.setText("Votos totales: " + response.getTotalVotes());
            StringBuilder sb = new StringBuilder();
            for (ResultItemResponse result : response.getResults()) {
                sb.append(result.getTeamName())
                        .append(" : ")
                        .append(result.getVotes())
                        .append(" votos\n");
            }
            resultsArea.setText(sb.toString());
        } catch (ApiClientException e) {
            summaryLabel.setText("Error al cargar resultados");
            resultsArea.setText(e.getMessage());
        }
    }

    @FXML
    private void closeResults() {
        Stage stage = (Stage) resultsArea.getScene().getWindow();
        stage.close();
    }
}
