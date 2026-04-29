package com.votify.frontend.results.strategy;

import com.votify.frontend.results.ResultsViewData;
import javafx.scene.Node;

// Estrategia para renderizar resultados en un formato visual concreto.
public interface ResultsViewStrategy {
    // Devuelve el identificador único de la estrategia.
    String id();

    // Construye el nodo JavaFX que representa la vista de resultados.
    Node buildView(ResultsViewData data);
}
