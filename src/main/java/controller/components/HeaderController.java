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
import util.LocaleManager;

import java.util.ResourceBundle;

/**
 * Controller for a reusable header component.
 * Provides functionality for title, subtitle, back button, metadata display, and action menu (Edit/Delete).
 * Supports RTL (Right-to-Left) locales with automatic icon flipping.
 */
public class HeaderController {
    // Resource bundle for localized strings
    ResourceBundle rb = ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

    // Context menu for additional actions (Edit/Delete)
    private final ContextMenu moreMenu = new ContextMenu();
    // Menu item for edit action
    private final MenuItem editItem = new MenuItem(rb.getString("header.edit"));
    // Menu item for delete action
    private final MenuItem deleteItem = new MenuItem(rb.getString("header.delete"));
    
    // Back button to navigate to previous screen
    @FXML
    private Button backButton;
    
    // Main title label
    @FXML
    private Label titleLabel;
    
    // Subtitle label (optional, can be hidden)
    @FXML
    private Label subtitleLabel;
    
    // Container for metadata information
    @FXML
    private HBox metaRow;
    
    // Icon for metadata display
    @FXML
    private ImageView metaIcon;
    
    // Label for metadata text (e.g., teacher name)
    @FXML
    private Label metaLabel;
    
    // Button to show more actions menu (Edit/Delete)
    @FXML
    private Button moreButton;
    
    // Icon for the more actions button
    @FXML
    private ImageView moreIcon;
    
    // Icon for the back button (flips for RTL locales)
    @FXML
    private ImageView backIcon;

    // Callback for back button click
    private Runnable backAction;
    // Callback for edit action
    private Runnable editAction;
    // Callback for delete action
    private Runnable deleteAction;

    /**
     * Initializes the controller when the FXML is loaded.
     * Sets up RTL support for the back icon and configures the more actions menu.
     */
    @FXML
    private void initialize() {
        // Flip back icon horizontally if the locale is RTL (Right-to-Left)
        if (backIcon != null) {
            backIcon.setScaleX(isRtlLocale() ? -1 : 1);
        }

        // Configure the edit menu item with its action handler
        editItem.setOnAction(e -> {
            if (editAction != null) editAction.run();
        });
        
        // Configure the delete menu item with its action handler
        deleteItem.setOnAction(e -> {
            if (deleteAction != null) deleteAction.run();
        });

        // Add both items to the more menu
        moreMenu.getItems().setAll(editItem, deleteItem);

        // Hide the more actions button by default (only show when explicitly set)
        setActionsVisible(false);
    }

    // ========== Title and Subtitle Methods ==========

    /**
     * Sets the main title text displayed in the header.
     *
     * @param title The title text to display
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Sets the subtitle text displayed below the title.
     * If the subtitle is null or blank, the subtitle label is hidden and not included in layout.
     *
     * @param subtitle The subtitle text to display, or null/blank to hide
     */
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

    // ========== Back Button Methods ==========

    /**
     * Controls the visibility of the back button.
     *
     * @param visible True to show the back button, false to hide it
     */
    public void setBackVisible(boolean visible) {
        backButton.setVisible(visible);
        backButton.setManaged(visible);
    }

    /**
     * Sets the callback to be invoked when the back button is clicked.
     *
     * @param action The Runnable to execute on back button click
     */
    public void setOnBack(Runnable action) {
        this.backAction = action;
    }

    /**
     * Handles the back button click event.
     * Invokes the registered back action callback if one exists.
     *
     * @param e The ActionEvent triggered by the back button
     */
    @FXML
    private void onBack(ActionEvent e) {
        if (backAction != null) backAction.run();
    }

    // ========== Metadata Row Methods ==========

    /**
     * Sets the metadata text to be displayed in the meta row.
     * If the text is null or blank, the meta row is hidden and not included in layout.
     *
     * @param text The metadata text to display (e.g., teacher name), or null/blank to hide
     */
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

    /**
     * Sets the icon displayed in the metadata row.
     *
     * @param image The Image to display as the metadata icon
     */
    public void setMetaIcon(Image image) {
        if (image != null) metaIcon.setImage(image);
    }

    // ========== More Actions (Edit/Delete) Methods ==========

    /**
     * Controls the visibility of the more actions button (Edit/Delete menu).
     *
     * @param visible True to show the more actions button, false to hide it
     */
    public void setActionsVisible(boolean visible) {
        moreButton.setVisible(visible);
        moreButton.setManaged(visible);
    }

    /**
     * Sets the callback to be invoked when the edit action is selected.
     *
     * @param action The Runnable to execute on edit action
     */
    public void setOnEdit(Runnable action) {
        this.editAction = action;
    }

    /**
     * Sets the callback to be invoked when the delete action is selected.
     *
     * @param action The Runnable to execute on delete action
     */
    public void setOnDelete(Runnable action) {
        this.deleteAction = action;
    }

    /**
     * Controls whether the edit menu item is enabled or disabled.
     *
     * @param enabled True to enable the edit option, false to disable it
     */
    public void setEditEnabled(boolean enabled) {
        editItem.setDisable(!enabled);
    }

    /**
     * Controls whether the delete menu item is enabled or disabled.
     *
     * @param enabled True to enable the delete option, false to disable it
     */
    public void setDeleteEnabled(boolean enabled) {
        deleteItem.setDisable(!enabled);
    }

    /**
     * Handles the more actions button click event.
     * Shows the context menu with Edit and Delete options positioned below the button.
     *
     * @param e The ActionEvent triggered by the more button
     */
    @FXML
    private void onMore(ActionEvent e) {
        // Get the screen bounds of the more button
        Bounds b = moreButton.localToScreen(moreButton.getBoundsInLocal());
        if (b == null) return;

        // Show the context menu below the more button
        moreMenu.show(moreButton, b.getMinX(), b.getMaxY());
    }

    // ========== Variant Support Methods ==========

    /**
     * Applies a visual variant (styling) based on user role.
     * Currently serves as a hook for future variant-specific behavior.
     *
     * @param variant The Variant to apply (STUDENT or TEACHER)
     */
    public void applyVariant(Variant variant) {
        // Keep hook for later; no forced behavior now.
    }

    /**
     * Enum defining header variants for different user roles.
     */
    public enum Variant {STUDENT, TEACHER}

    /**
     * Checks if the current locale is a Right-to-Left (RTL) language.
     * Detects Arabic, Persian, Urdu, and Hebrew locales.
     *
     * @return True if the current locale is RTL, false otherwise
     */
    private boolean isRtlLocale() {
        String language = LocaleManager.getLocale().getLanguage();
        return "ar".equals(language)
                || "fa".equals(language)
                || "ur".equals(language)
                || "he".equals(language);
    }
}
