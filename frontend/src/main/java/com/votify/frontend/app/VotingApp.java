package com.votify.frontend.app;

import com.votify.frontend.navigation.SceneNavigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de la aplicación de votación. En ella se llama a ejecutar la aplicación JavaFX y 
 * se carga la interfaz gráfica desde el archivo FXML.
 * La clase MainMenuController se encarga de manejar las acciones de los botones en la interfaz, como votar,
 * registrar participantes, ver resultados y salir de la aplicación.
 */
public class VotingApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Votify");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                VotingApp.class.getResourceAsStream("/com/votify/frontend/view/VotifyIcon.png")
        )));
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(720);
        SceneNavigator.showScene(
                primaryStage,
                "/com/votify/frontend/view/Access.fxml",
                "/com/votify/frontend/view/MainMenu.css",
                "Votify - Acceso"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
