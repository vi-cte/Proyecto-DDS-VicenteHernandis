package com.votify.frontend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ResultsController {
    public void viewResults(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(ResultsController.class.getResource("/com/votify/frontend/view/ResultsForm.fxml"));
            Scene currentScene = stage.getScene();
            double width = currentScene == null ? 1365 : currentScene.getWidth();
            double height = currentScene == null ? 768 : currentScene.getHeight();
            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(Objects.requireNonNull(
                    ResultsController.class.getResource("/com/votify/frontend/view/MainMenu.css")
            ).toExternalForm());

            stage.setTitle("Votify - Resultados");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No se pudo abrir la pantalla de resultados: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
