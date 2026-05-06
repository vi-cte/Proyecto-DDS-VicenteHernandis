package com.votify.frontend.results.strategy;

import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.results.ResultsViewData;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
        if (data.ranking().isEmpty()) {
            Label label = new Label("No hay resultados para mostrar.");
            label.getStyleClass().add("results-empty");
            return new StackPane(label);
        }

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Equipos");
        xAxis.setCategories(FXCollections.observableArrayList(
                data.ranking().stream()
                        .map(ResultItemResponse::getTeamName)
                        .toList()
        ));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Votos");
        yAxis.setForceZeroInRange(true);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "", ""));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().addAll("results-chart", "results-bar-chart");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCategoryGap(24);
        chart.setBarGap(10);
        chart.setPrefHeight(440);
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setTitle("Comparativa de votos por equipo");
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Votos");
        for (int i = 0; i < data.ranking().size(); i++) {
            ResultItemResponse item = data.ranking().get(i);
            XYChart.Data<String, Number> point = new XYChart.Data<>(item.getTeamName(), item.getVotes());
            int colorIndex = (i % 5) + 1;
            point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.getStyleClass().add("results-bar-color-" + colorIndex);
                }
            });
            series.getData().add(point);
        }
        chart.getData().add(series);

        VBox container = new VBox(16);
        container.getStyleClass().add("results-bar-list");
        container.getChildren().add(chart);
        return container;
    }
}
