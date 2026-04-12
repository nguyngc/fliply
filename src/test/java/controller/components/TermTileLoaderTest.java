package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TermTileLoaderTest {

    static { new JFXPanel(); }

    @Test
    void loadCreatesTileAndRunsSelectionCallback() {
        final boolean[] selected = {false};

        Node node = TermTileLoader.load(TermTileLoaderTest.class, "CPU", () -> selected[0] = true);

        assertNotNull(node);
        assertInstanceOf(StackPane.class, node);

        Button button = (Button) ((StackPane) node).getChildren().getFirst();
        assertEquals("CPU", button.getText());
        assertTrue(button.getStyle().contains("#ACD7FF"));

        button.fire();
        assertTrue(selected[0]);
        assertTrue(button.getStyle().contains("white"));
    }

    @Test
    void loadTileLoadExceptionStoresCause() {
        Throwable cause = new IllegalStateException("boom");
        TermTileLoadException ex = new TermTileLoadException("Failed", cause);

        assertEquals("Failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void loadWrapsIOExceptionFromInvalidResource() throws Exception {
        Path tmp = Files.createTempFile("term-tile", ".txt");
        Files.writeString(tmp, "not fxml");
        URL badUrl = tmp.toUri().toURL();

        ClassLoader loader = new ClassLoader(TermTileLoaderTest.class.getClassLoader()) {
            @Override
            public URL getResource(String name) {
                if ("components/term_tile.fxml".equals(name) || "/components/term_tile.fxml".equals(name)) {
                    return badUrl;
                }
                return super.getResource(name);
            }
        };

        Object proxy = Proxy.newProxyInstance(loader, new Class<?>[]{Runnable.class}, (InvocationHandler) (p, m, a) -> null);
        Class<?> ownerClass = proxy.getClass();
        Runnable onSelected = () -> {};

        TermTileLoadException ex = assertThrows(TermTileLoadException.class,
                () -> TermTileLoader.load(ownerClass, "CPU", onSelected));

        assertEquals("Failed to load term_tile.fxml", ex.getMessage());
        assertNotNull(ex.getCause());
    }
}

