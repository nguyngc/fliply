package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class HeaderControllerTest {
    static { new JFXPanel(); }
    private HeaderController controller;

    @BeforeEach
    void setUp() {
        controller = new HeaderController();

        // Inject all @FXML fields
        setPrivate("backButton", new Button());
        setPrivate("titleLabel", new Label());
        setPrivate("subtitleLabel", new Label());
        setPrivate("metaRow", new HBox());
        setPrivate("metaIcon", new ImageView());
        setPrivate("metaLabel", new Label());
        setPrivate("moreButton", new Button());
        setPrivate("moreIcon", new ImageView());

        // Gọi initialize() để setup menu, actionsVisible(false)
        callPrivate("initialize");
    }

    private void setPrivate(String field, Object value) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivate(String field) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(String method) {
        try {
            Method m = HeaderController.class.getDeclaredMethod(method);
            m.setAccessible(true);
            m.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- TESTS ----------------

    @Test
    void testSetTitle() {
        controller.setTitle("Dashboard");
        Label title = (Label) getPrivate("titleLabel");
        assertEquals("Dashboard", title.getText());
    }

    @Test
    void testSetSubtitle_visible() {
        controller.setSubtitle("Welcome");
        Label subtitle = (Label) getPrivate("subtitleLabel");

        assertEquals("Welcome", subtitle.getText());
        assertTrue(subtitle.isVisible());
        assertTrue(subtitle.isManaged());
    }

    @Test
    void testSetSubtitle_hidden() {
        controller.setSubtitle("");
        Label subtitle = (Label) getPrivate("subtitleLabel");

        assertFalse(subtitle.isVisible());
        assertFalse(subtitle.isManaged());
    }

    @Test
    void testBackButtonVisibility() {
        controller.setBackVisible(true);
        Button back = (Button) getPrivate("backButton");

        assertTrue(back.isVisible());
        assertTrue(back.isManaged());

        controller.setBackVisible(false);
        assertFalse(back.isVisible());
        assertFalse(back.isManaged());
    }

    @Test
    void testBackActionRuns() {
        final boolean[] called = {false};
        controller.setOnBack(() -> called[0] = true);

        callPrivateWithParam("onBack", new ActionEvent());
        assertTrue(called[0]);
    }

    private void callPrivateWithParam(String method, Object param) {
        try {
            Method m = HeaderController.class.getDeclaredMethod(method, param.getClass());
            m.setAccessible(true);
            m.invoke(controller, param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSetMeta_visible() {
        controller.setMeta("5 students");
        Label meta = (Label) getPrivate("metaLabel");
        HBox row = (HBox) getPrivate("metaRow");

        assertEquals("5 students", meta.getText());
        assertTrue(row.isVisible());
        assertTrue(row.isManaged());
    }

    @Test
    void testSetMeta_hidden() {
        controller.setMeta("");
        HBox row = (HBox) getPrivate("metaRow");

        assertFalse(row.isVisible());
        assertFalse(row.isManaged());
    }

    @Test
    void testActionsVisible() {
        controller.setActionsVisible(true);
        Button more = (Button) getPrivate("moreButton");

        assertTrue(more.isVisible());
        assertTrue(more.isManaged());

        controller.setActionsVisible(false);
        assertFalse(more.isVisible());
        assertFalse(more.isManaged());
    }

    @Test
    void testEditActionRuns() throws Exception {
        final boolean[] called = {false};
        controller.setOnEdit(() -> called[0] = true);

        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");
        menu.getItems().get(0).getOnAction().handle(new ActionEvent());

        assertTrue(called[0]);
    }

    @Test
    void testDeleteActionRuns() throws Exception {
        final boolean[] called = {false};
        controller.setOnDelete(() -> called[0] = true);

        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");
        menu.getItems().get(1).getOnAction().handle(new ActionEvent());

        assertTrue(called[0]);
    }

    @Test
    void testEditEnabled() {
        controller.setEditEnabled(false);
        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");

        assertTrue(menu.getItems().get(0).isDisable());
    }

    @Test
    void testDeleteEnabled() {
        controller.setDeleteEnabled(false);
        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");

        assertTrue(menu.getItems().get(1).isDisable());
    }
}
