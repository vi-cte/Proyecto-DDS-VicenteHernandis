package com.votify.frontend.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public final class SceneNavigator {
    private static final double DEFAULT_WIDTH = 1365;
    private static final double DEFAULT_HEIGHT = 768;

    private SceneNavigator() {
    }

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

    public static void showMainMenu(Stage stage) throws IOException {
        showScene(
                stage,
                "/com/votify/frontend/view/MainMenu.fxml",
                "/com/votify/frontend/view/MainMenu.css",
                "Votify"
        );
    }

    private static URL resource(String path) {
        return Objects.requireNonNull(SceneNavigator.class.getResource(path));
    }
}
