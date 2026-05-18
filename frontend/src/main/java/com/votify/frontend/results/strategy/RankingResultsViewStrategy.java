package com.votify.frontend.results.strategy;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.results.ResultsViewData;
import com.votify.frontend.ui.TeamInfoDialog;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

// Estrategia que muestra los resultados como ranking de equipos.
public class RankingResultsViewStrategy implements ResultsViewStrategy {

    private final ApiClient apiClient = ApiClient.getInstance();

    @Override
    // Devuelve el identificador de la vista de ranking.
    public String id() {
        return "ranking";
    }

    @Override
    // Construye la lista visual del ranking con tarjetas por equipo.
    public Node buildView(ResultsViewData data) {
        VBox container = new VBox(18);
        container.getStyleClass().add("results-ranking-list");

        if (data.ranking().isEmpty()) {
            container.getChildren().add(emptyState());
        } else {
            container.getChildren().add(sectionTitle("Ganadores del público"));
            long totalVotes = Math.max(1, data.response().getTotalPublicVotes());
            for (int i = 0; i < data.ranking().size(); i++) {
                ResultItemResponse item = data.ranking().get(i);
                container.getChildren().add(rankCard(item, i + 1, totalVotes));
            }
        }

        if (!data.juryRanking().isEmpty()) {
            container.getChildren().add(sectionTitle("Ganadores del jurado"));
            long totalJuryVotes = Math.max(1, data.juryRanking().stream().mapToLong(ResultItemResponse::getVotes).sum());
            for (int i = 0; i < data.juryRanking().size(); i++) {
                ResultItemResponse item = data.juryRanking().get(i);
                container.getChildren().add(rankCard(item, i + 1, totalJuryVotes));
            }
        }
        return container;
    }

    // Crea una tarjeta visual para una posición del ranking.
    private Node rankCard(ResultItemResponse item, int position, long totalVotes) {
        HBox card = new HBox(18);
        card.getStyleClass().add("ranking-card");
        card.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = new StackPane();
        badge.getStyleClass().addAll("ranking-badge", badgeStyleClass(position));
        badge.getChildren().add(badgeContent(position));

        VBox center = new VBox(12);
        HBox.setHgrow(center, Priority.ALWAYS);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        VBox teamBox = new VBox(4);
        HBox.setHgrow(teamBox, Priority.ALWAYS);
        Label teamName = new Label(item.getTeamName());
        teamName.getStyleClass().addAll("ranking-team-name", "team-link");
        
        teamBox.getChildren().add(teamName);

        // Abre la vista previa del equipo al hacer clic en cualquier parte de la fila.
        card.setStyle("-fx-cursor: hand;");
        card.setOnMouseClicked(event -> {
            try {
                for (com.votify.frontend.dto.EventResponse ev : apiClient.getEvents()) {
                    java.util.Optional<com.votify.frontend.dto.ParticipantResponse> match = apiClient.getParticipantResponses(ev.getId()).stream()
                            .filter(p -> p.getTeamName() != null && p.getTeamName().equalsIgnoreCase(item.getTeamName()))
                            .findFirst();
                    if (match.isPresent()) {
                        TeamInfoDialog.show(match.get());
                        return;
                    }
                }
            } catch (Exception ignored) {}
        });

        VBox scoreBox = new VBox(2);
        scoreBox.setAlignment(Pos.CENTER_RIGHT);
        Label votes = new Label(Long.toString(item.getVotes()));
        votes.getStyleClass().add("ranking-votes");
        scoreBox.getChildren().add(votes);

        top.getChildren().addAll(teamBox, scoreBox);

        HBox progressRow = new HBox(14);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        double percentage = (item.getVotes() * 100.0) / totalVotes;
        ProgressBar progressBar = new ProgressBar(item.getVotes() / (double) totalVotes);
        progressBar.getStyleClass().addAll("ranking-progress", progressStyleClass(position));
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        Label percent = new Label(String.format(java.util.Locale.US, "%.1f%%", percentage));
        percent.getStyleClass().add("ranking-percent");
        progressRow.getChildren().addAll(progressBar, percent);

        center.getChildren().addAll(top, progressRow);
        card.getChildren().addAll(badge, center);
        return card;
    }

    // Construye el contenido de la insignia de posición.
    private Node badgeContent(int position) {
        if (position <= 3) {
            SVGPath trophy = new SVGPath();
            trophy.setContent("M8 6H16V8H18C18.55 8 19 8.45 19 9V10C19 12.21 17.21 14 15 14H14.82C14.4 15.19 13.3 16.04 12 16.04C10.7 16.04 9.6 15.19 9.18 14H9C6.79 14 5 12.21 5 10V9C5 8.45 5.45 8 6 8H8V6ZM7 10C7 11.1 7.9 12 9 12V10H7ZM15 12C16.1 12 17 11.1 17 10H15V12ZM11 17H13V19H16V21H8V19H11V17Z");
            trophy.getStyleClass().addAll("ranking-badge-cup", badgeCupStyleClass(position));
            return trophy;
        }

        Label badgeLabel = new Label(Integer.toString(position));
        badgeLabel.getStyleClass().add("ranking-badge-text");
        return badgeLabel;
    }

    // Devuelve la clase CSS de la insignia según la posición.
    private String badgeStyleClass(int position) {
        return switch (position) {
            case 1 -> "ranking-badge-gold";
            case 2 -> "ranking-badge-silver";
            case 3 -> "ranking-badge-bronze";
            default -> "ranking-badge-default";
        };
    }

    // Devuelve la clase CSS de la copa según la posición.
    private String badgeCupStyleClass(int position) {
        return switch (position) {
            case 1 -> "ranking-badge-cup-gold";
            case 2 -> "ranking-badge-cup-silver";
            case 3 -> "ranking-badge-cup-bronze";
            default -> "ranking-badge-cup-default";
        };
    }

    // Devuelve la clase del progreso según la posición.
    private String progressStyleClass(int position) {
        return switch (position) {
            case 1 -> "ranking-progress-gold";
            case 2 -> "ranking-progress-silver";
            case 3 -> "ranking-progress-bronze";
            default -> "ranking-progress-default";
        };
    }

    // Construye el estado vacío cuando no hay resultados.
    private Node emptyState() {
        Label label = new Label("No hay resultados para mostrar.");
        label.getStyleClass().add("results-empty");
        return label;
    }

    // Construye un título para separar rankings.
    private Node sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("results-section-title");
        return label;
    }
}
