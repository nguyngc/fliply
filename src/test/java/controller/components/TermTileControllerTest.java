package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TermTileControllerTest {

    static { new JFXPanel(); }

    private TermTileController controller;

    @BeforeEach
    void setUp() {
        controller = new TermTileController();
        setPrivate("tileButton", new Button());
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = TermTileController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = TermTileController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void initializeAndStateChangesUpdateStyle() throws Exception {
        callPrivate("initialize");
        Button button = (Button) getPrivate("tileButton");
        assertTrue(button.getStyle().contains("#ACD7FF"));

        controller.setState(TermTileController.State.READ);
        assertTrue(button.getStyle().contains("white"));
        assertEquals(TermTileController.State.READ, controller.getState());

        controller.setText("CPU");
        assertEquals("CPU", button.getText());
    }

    @Test
    void onClickRunsSelectionCallback() throws Exception {
        final boolean[] selected = {false};
        controller.setOnSelected(() -> selected[0] = true);
        callPrivate("onClick");
        assertTrue(selected[0]);
    }

    @Test
    void onClickWithoutSelectionCallbackDoesNothing() {
        assertDoesNotThrow(() -> callPrivate("onClick"));
    }

    @Test
    void setStateHandlesMissingButton() {
        TermTileController detachedController = new TermTileController();

        assertDoesNotThrow(() -> detachedController.setState(TermTileController.State.READ));
        assertEquals(TermTileController.State.READ, detachedController.getState());
    }

    private void callPrivate(String method) throws Exception {
        Method m = TermTileController.class.getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(controller);
    }
}
