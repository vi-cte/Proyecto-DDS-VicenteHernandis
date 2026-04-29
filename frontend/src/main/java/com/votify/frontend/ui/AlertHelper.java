package com.votify.frontend.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

import java.util.Objects;

// Utilidad para mostrar alertas JavaFX con estilos comunes.
public final class AlertHelper {
    // Evita instancias de esta clase de utilidad.
    private AlertHelper() {
    }

    // Muestra una alerta de error.
    public static void showError(String message) {
        show(Alert.AlertType.ERROR, message, "app-alert-error");
    }

    // Muestra una alerta informativa.
    public static void showInfo(String message) {
        show(Alert.AlertType.INFORMATION, message, "app-alert-info");
    }

    // Muestra una alerta de advertencia.
    public static void showWarning(String message) {
        show(Alert.AlertType.WARNING, message, "app-alert-warning");
    }

    // Construye y muestra una alerta con el tipo y estilo indicados.
    private static void show(Alert.AlertType type, String message, String styleClass) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(
                AlertHelper.class.getResource("/com/votify/frontend/view/MainMenu.css")
        ).toExternalForm());
        dialogPane.getStyleClass().add("app-alert");
        dialogPane.getStyleClass().add(styleClass);
        alert.showAndWait();
    }
}
