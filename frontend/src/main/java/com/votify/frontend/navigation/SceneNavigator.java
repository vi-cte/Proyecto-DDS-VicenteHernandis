package com.votify.frontend.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

// Utilidad centralizada para cambiar escenas JavaFX.
public final class SceneNavigator {
    private static final double DEFAULT_WIDTH = 1365;
    private static final double DEFAULT_HEIGHT = 768;

    // Evita instancias de esta clase de utilidad.
    private SceneNavigator() {
    }

    // Carga un FXML, aplica estilos y lo muestra en el escenario.
    public static void showScene(Stage stage, String fxmlPath, String stylesheetPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(resource(fxmlPath));
        Scene currentScene = stage.getScene();
        double width = currentScene == null ? DEFAULT_WIDTH : currentScene.getWidth();
        double height = currentScene == null ? DEFAULT_HEIGHT : currentScene.getHeight();
        Scene scene = new Scene(loader.load(), width, height);
        if (stylesheetPath != null && !stylesheetPath.isBlank()) {
            scene.getStylesheets().add(resource(stylesheetPath).toExternalForm());
        }
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    // Muestra el menú principal de la aplicación.
    public static void showMainMenu(Stage stage) throws IOException {
        showScene(
                stage,
                "/com/votify/frontend/view/MainMenu.fxml",
                "/com/votify/frontend/view/MainMenu.css",
                "Votify"
        );
    }

    // Resuelve un recurso del classpath o falla si no existe.
    private static URL resource(String path) {
        return Objects.requireNonNull(SceneNavigator.class.getResource(path));
    }
}
