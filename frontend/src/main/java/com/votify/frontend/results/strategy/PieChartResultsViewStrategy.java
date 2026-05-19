package com.votify.frontend.results.strategy;

import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.results.ResultsViewData;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Locale;

// Estrategia que muestra los resultados como gráfico circular.
public class PieChartResultsViewStrategy implements ResultsViewStrategy {
    @Override
    // Devuelve el identificador de la vista circular.
    public String id() {
        return "pie";
    }

    @Override
    // Construye el gráfico circular con el ranking recibido.
    public Node buildView(ResultsViewData data) {
        List<ResultItemResponse> activePublic = data.ranking().stream().filter(r -> r.getVotes() > 0).toList();
        List<ResultItemResponse> activeJury = data.juryRanking().stream().filter(r -> r.getVotes() > 0).toList();

        if (activePublic.isEmpty() && activeJury.isEmpty()) {
            Label label = new Label("No hay resultados para mostrar.");
            label.getStyleClass().add("results-empty");
            return new StackPane(label);
        }

        VBox container = new VBox(24);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.getStyleClass().add("results-pie-list");
        
        if (!activePublic.isEmpty()) {
            double publicTotal = activePublic.stream().mapToDouble(ResultItemResponse::getVotes).sum();
            PieChart publicChart = new PieChart(FXCollections.observableArrayList(
                    activePublic.stream()
                            .map(item -> new PieChart.Data(item.getTeamName(), item.getVotes()))
                            .toList()
            ));
            publicChart.getStyleClass().addAll("results-chart", "results-pie-chart");
            publicChart.setTitle("Distribución - Público");
            publicChart.setLegendVisible(true);
            publicChart.setLabelsVisible(false); // Oculta las etiquetas externas que desbordan la pantalla
            publicChart.setLegendSide(Side.BOTTOM); // Mueve la leyenda abajo para que haya espacio
            publicChart.setPrefHeight(360);
            
            for (PieChart.Data d : publicChart.getData()) {
                double pct = (d.getPieValue() / publicTotal) * 100.0;
                String text = String.format(Locale.US, "%s\n%d votos (%.1f%%)", d.getName(), (long)d.getPieValue(), pct);
                Tooltip.install(d.getNode(), new Tooltip(text));
            }
            container.getChildren().add(publicChart);
        }

        if (!activeJury.isEmpty()) {
            double juryTotal = activeJury.stream().mapToDouble(ResultItemResponse::getVotes).sum();
            PieChart juryChart = new PieChart(FXCollections.observableArrayList(
                    activeJury.stream()
                            .map(item -> new PieChart.Data(item.getTeamName(), item.getVotes()))
                            .toList()
            ));
            juryChart.getStyleClass().addAll("results-chart", "results-pie-chart");
            juryChart.setTitle(data.isMulticriteria() ? "Distribución - Jurado (Puntos)" : "Distribución - Jurado (Votos)");
            juryChart.setLegendVisible(true);
            juryChart.setLabelsVisible(false); // Oculta las etiquetas externas que desbordan la pantalla
            juryChart.setLegendSide(Side.BOTTOM); // Mueve la leyenda abajo para que haya espacio
            juryChart.setPrefHeight(360);

            String unit = data.isMulticriteria() ? "pts" : "votos";
            for (PieChart.Data d : juryChart.getData()) {
                double pct = (d.getPieValue() / juryTotal) * 100.0;
                String text = String.format(Locale.US, "%s\n%d %s (%.1f%%)", d.getName(), (long)d.getPieValue(), unit, pct);
                Tooltip.install(d.getNode(), new Tooltip(text));
            }
            container.getChildren().add(juryChart);
        }
        
        return container;
    }
}
