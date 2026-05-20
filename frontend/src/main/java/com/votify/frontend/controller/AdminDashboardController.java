package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.AdminEventResponse;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

// Controlador del dashboard administrativo de eventos.
public class AdminDashboardController {
    private final ApiClient apiClient = ApiClient.getInstance();
    private AutoCloseable dashboardUpdatesSubscription;
    private volatile boolean refreshQueued;

    @FXML private Label eventsTitleLabel;
    @FXML private HBox eventsContainer;

    @FXML
    private void initialize() {
        loadEvents();
        subscribeToDashboardUpdates();
    }

    // Carga eventos y renderiza tarjetas.
    private void loadEvents() {
        try {
            List<AdminEventResponse> events = apiClient.getAdminEvents();
            List<AdminEventResponse> activeEvents = events.stream().filter(AdminEventResponse::isActive).toList();
            eventsTitleLabel.setText("Eventos Activos (" + activeEvents.size() + ")");
            eventsContainer.getChildren().setAll(IntStream.range(0, activeEvents.size()).mapToObj(i -> eventCard(activeEvents.get(i), i)).toList());
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
        }
    }

    // Construye una tarjeta de evento.
    private VBox eventCard(AdminEventResponse event, int index) {
        VBox card = new VBox();
        card.getStyleClass().add("admin-event-card");
        card.setPrefWidth(748);

        VBox header = new VBox(18);
        header.getStyleClass().add("admin-event-header");
        String[] colors = {"#2724f6", "#8224f6", "#168c3f", "#5d24f6", "#9524f6"};
        String bgColor = colors[index % colors.length];
        header.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16 16 0 0;");

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
        Label rankingTitle = new Label("Rankings en Tiempo Real");
        rankingTitle.getStyleClass().add("admin-ranking-title");
        HBox.setHgrow(rankingTitle, Priority.ALWAYS);
        bodyTitle.getChildren().add(rankingTitle);
        body.getChildren().add(bodyTitle);

        body.getChildren().add(rankingSection("Top 3 Público", event.getPublicRanking(), "votos"));
        body.getChildren().add(rankingSection("Top 3 Jurado", event.getJuryRanking(), "pts"));
        card.getChildren().addAll(header, body);

        // Navegar a la vista de resultados cuando se hace clic en la tarjeta del evento
        card.setStyle("-fx-cursor: hand;");
        card.setOnMouseClicked(mouseEvent -> {
            try {
                closeDashboardUpdatesSubscription();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/votify/frontend/view/ResultsForm.fxml"));
                Parent root = loader.load();

                com.votify.frontend.controller.ResultsFormController controller = loader.getController();
                controller.setFromAdminDashboard(true);
                controller.loadEvent(event.getId());

                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/com/votify/frontend/view/MainMenu.css").toExternalForm());
                
                Stage stage = (Stage) card.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Votify - Resultados");
            } catch (IOException e) {
                AlertHelper.showError("Error abriendo la pantalla de resultados: " + e.getMessage());
            }
        });

        return card;
    }

    private VBox rankingSection(String titleText, List<ResultItemResponse> sourceRanking, String valueSuffix) {
        VBox section = new VBox(10);
        section.getStyleClass().add("admin-ranking-section");
        Label title = new Label(titleText);
        title.getStyleClass().add("admin-ranking-subtitle");
        section.getChildren().add(title);

        List<ResultItemResponse> ranking = sourceRanking == null ? List.of() : sourceRanking.stream()
                .sorted(Comparator.comparingLong(ResultItemResponse::getVotes).reversed())
                .limit(3)
                .toList();
        if (ranking.isEmpty()) {
            Label empty = new Label("Todavía no hay equipos inscritos.");
            empty.getStyleClass().add("results-empty");
            section.getChildren().add(empty);
        } else {
            long total = Math.max(1, ranking.stream().mapToLong(ResultItemResponse::getVotes).sum());
            for (ResultItemResponse item : ranking) {
                section.getChildren().add(rankRow(item, total, valueSuffix));
            }
        }
        return section;
    }

    // Botón de ajustes de cada evento.
    private Button settingsButton(AdminEventResponse event) {
        Button button = new Button();
        button.getStyleClass().add("admin-event-settings-button");
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("admin-event-settings-icon");
        icon.setContent("M19.4 13c0-.3.1-.6.1-.9s0-.6-.1-.9l2.1-1.7c.2-.2.2-.4.1-.6l-2-3.5c-.1-.2-.3-.3-.5-.2l-2.5 1c-.5-.4-1.1-.7-1.7-1l-.4-2.7c0-.2-.2-.4-.5-.4h-4c-.2 0-.4.2-.5.4l-.4 2.7c-.6.3-1.2.6-1.7 1l-2.5-1c-.2-.1-.5 0-.5.2l-2 3.5c-.1.2-.1.5.1.6l2.1 1.7c-.1.3-.1.6-.1.9s0 .6.1.9l-2.1 1.7c-.2.2-.2.4-.1.6l2 3.5c.1.2.3.3.5.2l2.5-1c.5.4 1.1.7 1.7 1l.4 2.7c0 .2.2.4.5.4h4c.2 0 .4-.2.5-.4l.4-2.7c.6-.3 1.2-.6 1.7-1l2.5 1c.2.1.5 0 .5-.2l2-3.5c.1-.2.1-.5-.1-.6l-2.1-1.7zM12 15.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5z");
        button.setGraphic(icon);
        button.setOnAction(action -> {
            action.consume();
            openEventSettings(event);
        });
        button.setOnMouseClicked(mouseEvent -> mouseEvent.consume());
        return button;
    }

    // Abre un diálogo con los ajustes del evento seleccionado.
    private void openEventSettings(AdminEventResponse event) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/votify/frontend/view/CreateEventDialog.fxml"));
            Parent root = loader.load();
            
            com.votify.frontend.controller.CreateEventDialogController controller = loader.getController();
            controller.loadEvent(event);
            
            stage.setScene(new Scene(root));
            stage.setTitle("Ajustes del evento");
            stage.showAndWait();
            loadEvents();
        } catch (IOException e) {
            AlertHelper.showError("No se pudo abrir la configuración del evento: " + e.getMessage());
        }
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

    private HBox rankRow(ResultItemResponse item, long totalVotes, String valueSuffix) {
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
        Label votes = new Label(item.getVotes() + " " + valueSuffix);
        votes.getStyleClass().add("admin-rank-votes");
        votes.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        row.getChildren().addAll(text, votes);
        return row;
    }

    private void subscribeToDashboardUpdates() {
        dashboardUpdatesSubscription = apiClient.subscribeAdminDashboardUpdates(this::scheduleDashboardRefresh);
        eventsContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((windowObservable, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnHidden(event -> closeDashboardUpdatesSubscription());
                    }
                });
            }
        });
    }

    private void scheduleDashboardRefresh() {
        if (refreshQueued) {
            return;
        }
        refreshQueued = true;
        Platform.runLater(() -> {
            try {
                loadEvents();
            } finally {
                refreshQueued = false;
            }
        });
    }

    private void closeDashboardUpdatesSubscription() {
        if (dashboardUpdatesSubscription == null) {
            return;
        }
        try {
            dashboardUpdatesSubscription.close();
        } catch (Exception ignored) {
        }
        dashboardUpdatesSubscription = null;
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
    private void openCreateJury() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        Label title = new Label("Crear cuenta de jurado");
        title.getStyleClass().add("about-title");
        Label subtitle = new Label("Introduce las credenciales que usará el miembro del jurado");
        subtitle.getStyleClass().add("vote-subtitle");

        TextField emailField = new TextField();
        emailField.setPromptText("correo@ejemplo.com");
        emailField.getStyleClass().add("input-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mínimo 8 caracteres");
        passwordField.getStyleClass().add("input-field");

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("admin-secondary-button");
        cancelButton.setOnAction(event -> stage.close());

        Button createButton = new Button("Crear jurado");
        createButton.getStyleClass().add("action-button");
        createButton.setOnAction(event -> {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String password = passwordField.getText() == null ? "" : passwordField.getText();
            if (email.isBlank() || password.isBlank()) {
                AlertHelper.showError("Introduce correo y contraseña.");
                return;
            }
            try {
                apiClient.createJuryAccount(email, password);
                AlertHelper.showInfo("Cuenta de jurado creada correctamente.");
                stage.close();
            } catch (ApiClientException e) {
                AlertHelper.showError(e.getMessage());
            }
        });

        VBox emailBox = fieldBox("Correo", emailField);
        VBox passwordBox = fieldBox("Contraseña", passwordField);
        HBox actions = new HBox(14, cancelButton, createButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(24, new VBox(8, title, subtitle), emailBox, passwordBox, actions);
        root.getStyleClass().add("dialog-root");
        root.setPadding(new Insets(40, 42, 40, 42));

        Scene scene = new Scene(root, 520, 390);
        scene.getStylesheets().add(getClass().getResource("/com/votify/frontend/view/MainMenu.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Crear jurado");
        stage.showAndWait();
    }

    private VBox fieldBox(String label, TextField field) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");
        return new VBox(6, fieldLabel, field);
    }

    @FXML
    private void showHistory() {
        try {
            closeDashboardUpdatesSubscription();
            SceneNavigator.showScene((Stage) eventsContainer.getScene().getWindow(), "/com/votify/frontend/view/HistoryDashboard.fxml", "/com/votify/frontend/view/MainMenu.css", "Votify - Histórico de Eventos");
        } catch (IOException e) {
            AlertHelper.showError("No se pudo abrir el histórico: " + e.getMessage());
        }
    }

    @FXML
    private void exit() {
        try {
            closeDashboardUpdatesSubscription();
            SceneNavigator.showScene((Stage) eventsContainer.getScene().getWindow(), "/com/votify/frontend/view/Access.fxml", "/com/votify/frontend/view/MainMenu.css", "Votify - Acceso");
        } catch (IOException e) {
            AlertHelper.showError(e.getMessage());
        }
    }
}
