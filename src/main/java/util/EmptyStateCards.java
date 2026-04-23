package util;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Shared builder for simple informational empty states.
 */
public final class EmptyStateCards {
    private EmptyStateCards() {
    }

    public static VBox create(String title, String body) {
        VBox box = new VBox(8);
        box.setFillWidth(true);
        box.setPadding(new Insets(16));
        box.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-color: rgba(61,143,239,0.12);
                -fx-border-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 12, 0.18, 0, 4);
                """);

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #1F1F39; -fx-font-size: 16px; -fx-font-weight: 600;");

        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: rgba(0,0,0,0.62); -fx-font-size: 14px; -fx-font-weight: 400;");

        box.getChildren().addAll(titleLabel, bodyLabel);
        return box;
    }
}
