package controller.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class HeaderController {

    @FXML
    private Button backButton;

    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;

    @FXML
    private HBox metaRow;
    @FXML
    private ImageView metaIcon;
    @FXML
    private Label metaLabel;

    private Runnable backAction; // caller can inject navigation action

    // ---------- Basic text ----------
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        if (subtitle == null || subtitle.isBlank()) {
            subtitleLabel.setVisible(false);
            subtitleLabel.setManaged(false);
        } else {
            subtitleLabel.setText(subtitle);
            subtitleLabel.setVisible(true);
            subtitleLabel.setManaged(true);
        }
    }

    // ---------- Back button ----------
    public void setBackVisible(boolean visible) {
        backButton.setVisible(visible);
        backButton.setManaged(visible);
    }

    public void setOnBack(Runnable action) {
        this.backAction = action;
    }

    @FXML
    private void onBack(ActionEvent e) {
        if (backAction != null) backAction.run();
    }

    // ---------- Meta row (icon + text) ----------
    public void setMeta(String text) {
        if (text == null || text.isBlank()) {
            metaRow.setVisible(false);
            metaRow.setManaged(false);
        } else {
            metaLabel.setText(text);
            metaRow.setVisible(true);
            metaRow.setManaged(true);
        }
    }

    public void setMetaIcon(Image image) {
        if (image != null) metaIcon.setImage(image);
    }

    public void applyVariant(Variant variant) {
        // Keep it simple + flexible:
        // - STUDENT: meta row optional (often teacher name in class detail)
        // - TEACHER: meta row optional (could show "Teacher view" or school name)
        // Customize later.
        if (variant == Variant.TEACHER) {
            // example: slightly smaller title if needed (optional)
            // titleLabel.setStyle(titleLabel.getStyle() + "; -fx-font-size: 40px;");
        }
    }

    // ---------- Teacher / Student variants ----------
    public enum Variant {STUDENT, TEACHER}
}
