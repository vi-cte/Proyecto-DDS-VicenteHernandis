package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.AdminEventResponse;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class HistoryDashboardController {
    private final ApiClient apiClient = ApiClient.getInstance();

    @FXML private VBox historyContainer;
    @FXML private Button backButton;
    @FXML private Label userNameLabel;
    @FXML private Button exitButton;

    @FXML
    private void initialize() {
        if (userNameLabel != null) userNameLabel.setText("Administrador");
        loadArchivedEvents();
    }

    private void loadArchivedEvents() {
        try {
            List<AdminEventResponse> events = apiClient.getAdminEvents().stream()
                    .filter(e -> !e.isActive())
                    .toList();
            
            if (events.isEmpty()) {
                Label emptyLabel = new Label("No hay eventos en el histórico.");
                emptyLabel.getStyleClass().add("results-empty");
                historyContainer.getChildren().add(emptyLabel);
                return;
            }

            for (AdminEventResponse event : events) {
                historyContainer.getChildren().add(buildHistoryCard(event));
            }
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
        }
    }

    private HBox buildHistoryCard(AdminEventResponse event) {
        HBox summaryCard = new HBox(24);
        summaryCard.getStyleClass().add("results-summary-card");
        summaryCard.setPadding(new Insets(20, 24, 20, 24));
        summaryCard.setAlignment(Pos.CENTER_LEFT);

        VBox eventInfo = new VBox(6);
        Label title = new Label(event.getName());
        title.setStyle("-fx-font-weight: 800; -fx-text-fill: #0f1f3d; -fx-font-size: 18px;");
        Label date = new Label(event.getEventDate() == null ? "Sin fecha" : event.getEventDate());
        date.getStyleClass().add("results-stat-label");
        eventInfo.getChildren().addAll(title, date);
        HBox.setHgrow(eventInfo, Priority.ALWAYS);

        VBox votesBox = new VBox(6);
        Label votesLbl = new Label("Total Votos");
        votesLbl.getStyleClass().add("results-stat-label");
        Label votesVal = new Label(String.valueOf(event.getTotalVotes()));
        votesVal.getStyleClass().add("results-stat-value");
        votesBox.getChildren().addAll(votesLbl, votesVal);

        VBox partsBox = new VBox(6);
        Label partsLbl = new Label("Participantes");
        partsLbl.getStyleClass().add("results-stat-label");
        Label partsVal = new Label(String.valueOf(event.getParticipants()));
        partsVal.getStyleClass().add("results-stat-value");
        partsBox.getChildren().addAll(partsLbl, partsVal);

        String publicWinner = "Sin datos";
        String juryWinner = "Sin datos";

        List<ResultItemResponse> publicRanking = event.getRanking() == null ? List.of() : event.getRanking().stream()
                .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                .toList();
        if (!publicRanking.isEmpty()) {
            publicWinner = publicRanking.get(0).getTeamName();
        }

        try {
            com.votify.frontend.dto.ResultsResponse detailedResults = apiClient.getResults(event.getId());
            if (detailedResults != null && detailedResults.getJuryResults() != null && !detailedResults.getJuryResults().isEmpty()) {
                List<ResultItemResponse> juryRanking = detailedResults.getJuryResults().stream()
                        .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                        .toList();
                if (!juryRanking.isEmpty()) {
                    juryWinner = juryRanking.get(0).getTeamName();
                }
            }
        } catch (Exception e) {
            // Se ignora si no hay datos del jurado o falla la llamada detallada
        }

        VBox publicBox = new VBox(6);
        Label publicLabel = new Label("Ganador del Público");
        publicLabel.getStyleClass().add("results-stat-label");
        Label publicTeamLabel = new Label(publicWinner);
        publicTeamLabel.getStyleClass().add("results-stat-winner");
        publicBox.getChildren().addAll(publicLabel, publicTeamLabel);

        VBox juryBox = new VBox(6);
        Label juryLabel = new Label("Ganador del Jurado");
        juryLabel.getStyleClass().add("results-stat-label");
        Label juryTeamLabel = new Label(juryWinner);
        juryTeamLabel.getStyleClass().add("results-stat-winner");
        juryBox.getChildren().addAll(juryLabel, juryTeamLabel);

        Button deleteBtn = new Button("Eliminar");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> {
            e.consume();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar evento archivado");
            alert.setContentText("¿Estás seguro de que deseas eliminar permanentemente este evento archivado? Se perderán todos sus datos y resultados. Esta acción no se puede deshacer.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        apiClient.deleteAdminEvent(event.getId());
                        historyContainer.getChildren().clear();
                        loadArchivedEvents();
                    } catch (ApiClientException ex) {
                        AlertHelper.showError(ex.getMessage());
                    }
                }
            });
        });

        summaryCard.getChildren().addAll(eventInfo, votesBox, partsBox, publicBox, juryBox, deleteBtn);

        summaryCard.setStyle("-fx-cursor: hand;");
        summaryCard.setOnMouseClicked(mouseEvent -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/votify/frontend/view/ResultsForm.fxml"));
                Parent root = loader.load();

                com.votify.frontend.controller.ResultsFormController controller = loader.getController();
                controller.setFromHistoryDashboard(true);
                controller.loadEvent(event.getId());

                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/votify/frontend/view/MainMenu.css").toExternalForm());
                
                Stage stage = (Stage) summaryCard.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Votify - Resultados");
            } catch (IOException e) {
                AlertHelper.showError("Error abriendo la pantalla de resultados: " + e.getMessage());
            }
        });

        return summaryCard;
    }

    @FXML
    private void exit() {
        ApiClient.getInstance().logout();
        try {
            SceneNavigator.showScene((Stage) historyContainer.getScene().getWindow(), "/com/votify/frontend/view/Access.fxml", "/com/votify/frontend/view/MainMenu.css", "Votify - Acceso");
        } catch (IOException e) {
            AlertHelper.showError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            SceneNavigator.showScene((Stage) historyContainer.getScene().getWindow(), "/com/votify/frontend/view/AdminDashboard.fxml", "/com/votify/frontend/view/MainMenu.css", "Votify - Administración");
        } catch (IOException e) {
            AlertHelper.showError("Error al volver al dashboard: " + e.getMessage());
        }
    }
}