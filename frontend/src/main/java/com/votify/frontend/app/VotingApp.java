package com.votify.frontend.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación de votación. En ella se llama a ejecutar la aplicación JavaFX y 
 * se carga la interfaz gráfica desde el archivo FXML.
 * La clase MainMenuController se encarga de manejar las acciones de los botones en la interfaz, como votar,
 * registrar participantes, ver resultados y salir de la aplicación.
 */
public class VotingApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(VotingApp.class.getResource("/com/votify/frontend/view/MainMenu.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Votify");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
