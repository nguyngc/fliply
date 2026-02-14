package controller.components;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class HeaderController {

    private final ContextMenu moreMenu = new ContextMenu();
    private final MenuItem editItem = new MenuItem("Edit");
    private final MenuItem deleteItem = new MenuItem("Delete");
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
    @FXML
    private Button moreButton;
    @FXML
    private ImageView moreIcon;

    private Runnable backAction;
    private Runnable editAction;
    private Runnable deleteAction;

    @FXML
    private void initialize() {
        // Build the menu once
        editItem.setOnAction(e -> {
            if (editAction != null) editAction.run();
        });
        deleteItem.setOnAction(e -> {
            if (deleteAction != null) deleteAction.run();
        });

        moreMenu.getItems().setAll(editItem, deleteItem);

        // default hidden
        setActionsVisible(false);
    }

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

    // ---------- Meta row ----------
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

    // ---------- More actions (Edit/Delete) ----------
    public void setActionsVisible(boolean visible) {
        moreButton.setVisible(visible);
        moreButton.setManaged(visible);
    }

    public void setOnEdit(Runnable action) {
        this.editAction = action;
    }

    public void setOnDelete(Runnable action) {
        this.deleteAction = action;
    }

    public void setEditEnabled(boolean enabled) {
        editItem.setDisable(!enabled);
    }

    public void setDeleteEnabled(boolean enabled) {
        deleteItem.setDisable(!enabled);
    }

    @FXML
    private void onMore(ActionEvent e) {
        Bounds b = moreButton.localToScreen(moreButton.getBoundsInLocal());
        if (b == null) return;

        moreMenu.show(moreButton, b.getMinX(), b.getMaxY());
    }

    // ---------- Variants ----------
    public void applyVariant(Variant variant) {
        // Keep hook for later; no forced behavior now.
    }

    public enum Variant {STUDENT, TEACHER}
}
