package com.votify.frontend.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;

import java.util.Objects;

public final class AlertHelper {
    private AlertHelper() {
    }

    public static void showError(String message) {
        show(Alert.AlertType.ERROR, message, "app-alert-error");
    }

    public static void showInfo(String message) {
        show(Alert.AlertType.INFORMATION, message, "app-alert-info");
    }

    public static void showWarning(String message) {
        show(Alert.AlertType.WARNING, message, "app-alert-warning");
    }

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
