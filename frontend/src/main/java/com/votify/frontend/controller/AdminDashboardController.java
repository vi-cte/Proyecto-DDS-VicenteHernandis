package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.AdminEventResponse;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

// Controlador del dashboard administrativo de eventos.
public class AdminDashboardController {
    private final ApiClient apiClient = ApiClient.getInstance();

    @FXML private Label eventsTitleLabel;
    @FXML private FlowPane eventsContainer;

    @FXML
    private void initialize() {
        loadEvents();
    }

    // Carga eventos y renderiza tarjetas.
    private void loadEvents() {
        try {
            List<AdminEventResponse> events = apiClient.getAdminEvents();
            eventsTitleLabel.setText("Eventos Activos (" + events.stream().filter(AdminEventResponse::isActive).count() + ")");
            eventsContainer.getChildren().setAll(events.stream().map(this::eventCard).toList());
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
        }
    }

    // Construye una tarjeta de evento.
    private VBox eventCard(AdminEventResponse event) {
        VBox card = new VBox();
        card.getStyleClass().add("admin-event-card");
        card.setPrefWidth(748);

        VBox header = new VBox(18);
        header.getStyleClass().add("admin-event-header");
        Label title = new Label(event.getName());
        title.getStyleClass().add("admin-event-title");
        Button settingsButton = settingsButton(event);
        HBox titleRow = new HBox(12, title, settingsButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);
        Label date = new Label(event.getEventDate() == null ? "Sin fecha" : event.getEventDate());
        date.getStyleClass().add("admin-event-date");
        HBox stats = new HBox(18, statBox("Total de Votos", Long.toString(event.getTotalVotes())), statBox("Participantes", Long.toString(event.getParticipants())));
        header.getChildren().addAll(titleRow, date, stats);

        VBox body = new VBox(16);
        body.getStyleClass().add("admin-event-body");
        HBox bodyTitle = new HBox();
        bodyTitle.setAlignment(Pos.CENTER_LEFT);
        Label rankingTitle = new Label("Ranking en Tiempo Real");
        rankingTitle.getStyleClass().add("admin-ranking-title");
        Label live = new Label("●  Actualizando en vivo");
        live.getStyleClass().add("admin-live-label");
        HBox.setHgrow(rankingTitle, Priority.ALWAYS);
        bodyTitle.getChildren().addAll(rankingTitle, live);
        body.getChildren().add(bodyTitle);

        List<ResultItemResponse> ranking = event.getRanking() == null ? List.of() : event.getRanking().stream()
                .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                .limit(3)
                .toList();
        if (ranking.isEmpty()) {
            Label empty = new Label("Todavía no hay votos en este evento.");
            empty.getStyleClass().add("results-empty");
            body.getChildren().add(empty);
        } else {
            long total = Math.max(1, ranking.stream().mapToLong(ResultItemResponse::getVotes).sum());
            for (ResultItemResponse item : ranking) {
                body.getChildren().add(rankRow(item, total));
            }
        }
        card.getChildren().addAll(header, body);
        return card;
    }

    // Botón de ajustes de cada evento.
    private Button settingsButton(AdminEventResponse event) {
        Button button = new Button();
        button.getStyleClass().add("admin-event-settings-button");
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("admin-event-settings-icon");
        icon.setContent("M19.4 13c0-.3.1-.6.1-.9s0-.6-.1-.9l2.1-1.7c.2-.2.2-.4.1-.6l-2-3.5c-.1-.2-.3-.3-.5-.2l-2.5 1c-.5-.4-1.1-.7-1.7-1l-.4-2.7c0-.2-.2-.4-.5-.4h-4c-.2 0-.4.2-.5.4l-.4 2.7c-.6.3-1.2.6-1.7 1l-2.5-1c-.2-.1-.5 0-.5.2l-2 3.5c-.1.2-.1.5.1.6l2.1 1.7c-.1.3-.1.6-.1.9s0 .6.1.9l-2.1 1.7c-.2.2-.2.4-.1.6l2 3.5c.1.2.3.3.5.2l2.5-1c.5.4 1.1.7 1.7 1l.4 2.7c0 .2.2.4.5.4h4c.2 0 .4-.2.5-.4l.4-2.7c.6-.3 1.2-.6 1.7-1l2.5 1c.2.1.5 0 .5-.2l2-3.5c.1-.2.1-.5-.1-.6l-2.1-1.7zM12 15.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5z");
        button.setGraphic(icon);
        button.setOnAction(action -> openEventSettings(event));
        return button;
    }

    // Abre un diálogo con los ajustes del evento seleccionado.
    private void openEventSettings(AdminEventResponse event) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Ajustes del evento");
        dialog.setHeaderText(event.getName());

        ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/votify/frontend/view/MainMenu.css").toExternalForm());

        CheckBox registrations = new CheckBox("Inscripciones abiertas");
        registrations.setSelected(event.isRegistrationsOpen());
        CheckBox voting = new CheckBox("Votación abierta");
        voting.setSelected(event.isVotingOpen());
        CheckBox results = new CheckBox("Resultados visibles");
        results.setSelected(event.isResultsVisible());
        CheckBox jury = new CheckBox("Jurado habilitado");
        jury.setSelected(event.isJuryEnabled());
        ComboBox<String> juryMode = new ComboBox<>();
        juryMode.getItems().setAll("SIMPLE", "MULTICRITERIA");
        juryMode.setValue(normalizeMode(event.getJuryVotingMode()));
        TextField maxVotes = new TextField(Integer.toString(event.getMaxTeamsToVote()));
        maxVotes.setPrefWidth(90);

        registrations.setOnAction(action -> {
            if (registrations.isSelected()) {
                voting.setSelected(false);
            }
        });
        voting.setOnAction(action -> {
            if (voting.isSelected()) {
                registrations.setSelected(false);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.getStyleClass().add("admin-event-settings-grid");
        grid.add(registrations, 0, 0, 2, 1);
        grid.add(voting, 0, 1, 2, 1);
        grid.add(results, 0, 2, 2, 1);
        grid.add(jury, 0, 3, 2, 1);
        grid.add(new Label("Modo del jurado"), 0, 4);
        grid.add(juryMode, 1, 4);
        grid.add(new Label("Votos por persona"), 0, 5);
        grid.add(maxVotes, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != saveButton) {
                return false;
            }
            try {
                int max = Integer.parseInt(maxVotes.getText().trim());
                apiClient.updateAdminEvent(
                        event.getId(),
                        event.getName(),
                        event.getEventDate(),
                        event.getDescription(),
                        registrations.isSelected(),
                        voting.isSelected(),
                        results.isSelected(),
                        max,
                        jury.isSelected(),
                        normalizeMode(juryMode.getValue())
                );
                return true;
            } catch (NumberFormatException e) {
                AlertHelper.showError("El número de votos debe ser numérico.");
                return false;
            } catch (ApiClientException e) {
                AlertHelper.showError(e.getMessage());
                return false;
            }
        });

        dialog.showAndWait().ifPresent(saved -> {
            if (saved) {
                loadEvents();
            }
        });
    }

    private VBox statBox(String label, String value) {
        VBox box = new VBox(8);
        box.getStyleClass().add("admin-stat-box");
        Label caption = new Label(label);
        caption.getStyleClass().add("admin-stat-caption");
        Label number = new Label(value);
        number.getStyleClass().add("admin-stat-value");
        box.getChildren().addAll(caption, number);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private HBox rankRow(ResultItemResponse item, long totalVotes) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("admin-rank-row");
        VBox text = new VBox(8);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label name = new Label(item.getTeamName());
        name.getStyleClass().add("admin-rank-name");
        ProgressBar progress = new ProgressBar(item.getVotes() / (double) totalVotes);
        progress.getStyleClass().add("ranking-progress");
        text.getChildren().addAll(name, progress);
        Label votes = new Label(Long.toString(item.getVotes()));
        votes.getStyleClass().add("admin-rank-votes");
        row.getChildren().addAll(text, votes);
        return row;
    }

    @FXML
    private void openCreateEvent() {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            Parent root = FXMLLoader.load(getClass().getResource("/com/votify/frontend/view/CreateEventDialog.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Crear evento");
            stage.showAndWait();
            loadEvents();
        } catch (IOException e) {
            AlertHelper.showError("No se pudo abrir la creación de evento: " + e.getMessage());
        }
    }

    @FXML
    private void showHistory() {
        AlertHelper.showInfo("El histórico completo se añadirá en la siguiente iteración.");
    }

    @FXML
    private void exit() {
        try {
            SceneNavigator.showScene((Stage) eventsContainer.getScene().getWindow(), "/com/votify/frontend/view/Access.fxml", "/com/votify/frontend/view/MainMenu.css", "Votify - Acceso");
        } catch (IOException e) {
            AlertHelper.showError(e.getMessage());
        }
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "SIMPLE";
        }
        return mode.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
