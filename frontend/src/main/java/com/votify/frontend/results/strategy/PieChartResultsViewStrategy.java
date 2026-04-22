package com.votify.frontend.results.strategy;

import com.votify.frontend.results.ResultsViewData;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class PieChartResultsViewStrategy implements ResultsViewStrategy {
    @Override
    public String id() {
        return "pie";
    }

    @Override
    public Node buildView(ResultsViewData data) {
        if (data.ranking().isEmpty()) {
            Label label = new Label("No hay resultados para mostrar.");
            label.getStyleClass().add("results-empty");
            return new StackPane(label);
        }

        PieChart chart = new PieChart(FXCollections.observableArrayList(
                data.ranking().stream()
                        .map(item -> new PieChart.Data(item.getTeamName(), item.getVotes()))
                        .toList()
        ));
        chart.getStyleClass().addAll("results-chart", "results-pie-chart");
        chart.setTitle("Distribución de votos");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setClockwise(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelLineLength(18);
        chart.setPrefHeight(460);
        return chart;
    }
}
