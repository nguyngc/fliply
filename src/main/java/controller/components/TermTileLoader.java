package controller.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

/**
 * Factory helper for loading and initializing reusable term-tile components.
 */
public final class TermTileLoader {
    private TermTileLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Loads the tile FXML and wires selection callback so the tile marks itself as read before delegating to caller logic.
     */
    public static Node load(Class<?> owner, String term, Runnable onSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(owner.getResource("/components/term_tile.fxml"));
            Node node = loader.load();
            TermTileController ctrl = loader.getController();

            ctrl.setText(term);
            ctrl.setState(TermTileController.State.UNREAD);
            ctrl.setOnSelected(() -> {
                ctrl.setState(TermTileController.State.READ);
                onSelected.run();
            });

            return node;
        } catch (IOException | IllegalStateException ex) {
            throw new TermTileLoadException("Failed to load term_tile.fxml", ex);
        }
    }
}

