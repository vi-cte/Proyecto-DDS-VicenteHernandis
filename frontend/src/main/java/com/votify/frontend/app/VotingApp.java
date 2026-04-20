package com.votify.frontend.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

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
        Scene scene = new Scene(loader.load(), 1365, 768);
        scene.getStylesheets().add(Objects.requireNonNull(
                VotingApp.class.getResource("/com/votify/frontend/view/MainMenu.css")
        ).toExternalForm());
        primaryStage.setTitle("Votify");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                VotingApp.class.getResourceAsStream("/com/votify/frontend/view/VotifyIcon.png")
        )));
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
