package com.votify.frontend.results.strategy;

import com.votify.frontend.results.ResultsViewData;
import javafx.scene.Node;

public interface ResultsViewStrategy {
    String id();

    Node buildView(ResultsViewData data);
}
