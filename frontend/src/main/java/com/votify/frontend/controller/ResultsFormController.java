package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.exception.ApiClientException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

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
        try {
            Stage stage = (Stage) resultsArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/votify/frontend/view/MainMenu.fxml"));
            Scene currentScene = stage.getScene();
            Scene scene = new Scene(loader.load(), currentScene.getWidth(), currentScene.getHeight());
            scene.getStylesheets().add(Objects.requireNonNull(
                    ResultsFormController.class.getResource("/com/votify/frontend/view/MainMenu.css")
            ).toExternalForm());
            stage.setTitle("Votify");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            summaryLabel.setText("No se pudo volver al menu principal: " + e.getMessage());
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }
}
