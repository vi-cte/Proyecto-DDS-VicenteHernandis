package com.votify.frontend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ResultsController {
    public void viewResults() {
        try {
            FXMLLoader loader = new FXMLLoader(ResultsController.class.getResource("/com/votify/frontend/view/ResultsForm.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Resultados");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No se pudo abrir la pantalla de resultados: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
