package com.votify.frontend.results.strategy;

import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.results.ResultsViewData;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.List;

// Estrategia que muestra los resultados como gráfico de barras.
public class BarChartResultsViewStrategy implements ResultsViewStrategy {
    @Override
    // Devuelve el identificador de la vista de barras.
    public String id() {
        return "bars";
    }

    @Override
    // Construye el gráfico de barras con el ranking recibido.
    public Node buildView(ResultsViewData data) {
        List<ResultItemResponse> activePublic = data.ranking().stream().filter(r -> r.getVotes() > 0).toList();
        List<ResultItemResponse> activeJury = data.juryRanking().stream().filter(r -> r.getVotes() > 0).toList();

        if (activePublic.isEmpty() && activeJury.isEmpty()) {
            Label label = new Label("No hay resultados para mostrar.");
            label.getStyleClass().add("results-empty");
            return new StackPane(label);
        }

        List<String> categories = data.ranking().stream()
                .filter(r -> r.getVotes() > 0 || data.juryRanking().stream().anyMatch(j -> j.getTeamName().equals(r.getTeamName()) && j.getVotes() > 0))
                .map(ResultItemResponse::getTeamName)
                .toList();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Equipos");
        xAxis.setTickLabelRotation(45); // Rota los textos 45 grados para que no se superpongan
        xAxis.setCategories(FXCollections.observableArrayList(
                categories
        ));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(data.isMulticriteria() ? "Votos / Puntos" : "Votos");
        yAxis.setForceZeroInRange(true);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "", ""));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().addAll("results-chart", "results-bar-chart");
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM); // Coloca la leyenda de colores abajo
        chart.setCategoryGap(24);
        chart.setBarGap(10);
        chart.setPrefHeight(500);
        chart.setMinHeight(500);
        chart.setPadding(new Insets(10, 10, 20, 10));
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setTitle("Comparativa de votos por equipo");
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);

        // Forzamos el color del texto para que no se vea blanco sobre blanco
        chart.setStyle("-fx-text-fill: #1a1a1a;");

        // Forzamos el estilo específico de la leyenda (por si el CSS lo oculta)
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    chart.lookupAll(".chart-legend-item").forEach(node -> {
                        node.setStyle("-fx-text-fill: #1a1a1a; -fx-font-weight: bold; -fx-font-size: 13px; -fx-content-display: LEFT;");
                    });
                });
            }
        });

        XYChart.Series<String, Number> publicSeries = new XYChart.Series<>();
        publicSeries.setName("Público");
        for (ResultItemResponse item : activePublic) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(item.getTeamName(), item.getVotes());
            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip t = new Tooltip("Público\n" + item.getTeamName() + ": " + item.getVotes() + " votos");
                    Tooltip.install(newNode, t);
                }
            });
            publicSeries.getData().add(dataPoint);
        }

        XYChart.Series<String, Number> jurySeries = new XYChart.Series<>();
        jurySeries.setName("Jurado");
        String unit = data.isMulticriteria() ? " puntos" : " votos";
        for (ResultItemResponse item : activeJury) {
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(item.getTeamName(), item.getVotes());
            dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip t = new Tooltip("Jurado\n" + item.getTeamName() + ": " + item.getVotes() + unit);
                    Tooltip.install(newNode, t);
                }
            });
            jurySeries.getData().add(dataPoint);
        }
        chart.getData().add(publicSeries);
        chart.getData().add(jurySeries);

        VBox container = new VBox(16);
        container.getStyleClass().add("results-bar-list");
        container.getChildren().add(chart);
        return container;
    }
}
