package controller.components;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class HeaderControllerTest {

    private HeaderController controller;

    @BeforeEach
    void setUp() {
        controller = new HeaderController();

        // Inject UI components via reflection
        setPrivate("backButton", new Button());
        setPrivate("titleLabel", new Label());
        setPrivate("subtitleLabel", new Label());
        setPrivate("metaRow", new HBox());
        setPrivate("metaIcon", new ImageView());
        setPrivate("metaLabel", new Label());
        setPrivate("moreButton", new Button());
        setPrivate("moreIcon", new ImageView());

        // Call private initialize() via reflection
        callPrivateMethod("initialize");
    }

    // ---------------- Reflection Helpers ----------------

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

    private void callPrivateMethod(String methodName, Object... args) {
        try {
            Method m = HeaderController.class.getDeclaredMethod(methodName, ActionEvent.class);
            m.setAccessible(true);
            m.invoke(controller, args);
        } catch (NoSuchMethodException e) {
            // method without parameters
            try {
                Method m = HeaderController.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(controller);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MenuItem getMenuItem(String name) {
        try {
            Field f = HeaderController.class.getDeclaredField(name);
            f.setAccessible(true);
            return (MenuItem) f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Tests ----------------

    @Test
    void testSetTitle() {
        controller.setTitle("Hello");
        Label title = (Label) getPrivate("titleLabel");
        assertEquals("Hello", title.getText());
    }

    @Test
    void testSetSubtitleVisible() {
        controller.setSubtitle("Sub");
        Label subtitle = (Label) getPrivate("subtitleLabel");

        assertTrue(subtitle.isVisible());
        assertTrue(subtitle.isManaged());
        assertEquals("Sub", subtitle.getText());
    }

    @Test
    void testSetSubtitleHidden() {
        controller.setSubtitle("");
        Label subtitle = (Label) getPrivate("subtitleLabel");

        assertFalse(subtitle.isVisible());
        assertFalse(subtitle.isManaged());
    }

    @Test
    void testBackButtonVisible() {
        controller.setBackVisible(true);
        Button back = (Button) getPrivate("backButton");

        assertTrue(back.isVisible());
        assertTrue(back.isManaged());

        controller.setBackVisible(false);
        assertFalse(back.isVisible());
        assertFalse(back.isManaged());
    }

    @Test
    void testOnBack() {
        final boolean[] called = {false};
        controller.setOnBack(() -> called[0] = true);

        callPrivateMethod("onBack", new ActionEvent());
        assertTrue(called[0]);
    }

    @Test
    void testSetMetaVisible() {
        controller.setMeta("Info");

        HBox row = (HBox) getPrivate("metaRow");
        Label label = (Label) getPrivate("metaLabel");

        assertTrue(row.isVisible());
        assertTrue(row.isManaged());
        assertEquals("Info", label.getText());
    }

    @Test
    void testSetMetaHidden() {
        controller.setMeta("");

        HBox row = (HBox) getPrivate("metaRow");

        assertFalse(row.isVisible());
        assertFalse(row.isManaged());
    }

    @Test
    void testSetMetaIcon() {
        Image img = new Image("test.png");
        controller.setMetaIcon(img);

        ImageView icon = (ImageView) getPrivate("metaIcon");
        assertEquals(img, icon.getImage());
    }

    @Test
    void testSetActionsVisible() {
        controller.setActionsVisible(true);
        Button more = (Button) getPrivate("moreButton");

        assertTrue(more.isVisible());
        assertTrue(more.isManaged());

        controller.setActionsVisible(false);
        assertFalse(more.isVisible());
        assertFalse(more.isManaged());
    }

    @Test
    void testEditAction() {
        final boolean[] called = {false};
        controller.setOnEdit(() -> called[0] = true);

        MenuItem edit = getMenuItem("editItem");
        edit.getOnAction().handle(new ActionEvent());

        assertTrue(called[0]);
    }

    @Test
    void testDeleteAction() {
        final boolean[] called = {false};
        controller.setOnDelete(() -> called[0] = true);

        MenuItem del = getMenuItem("deleteItem");
        del.getOnAction().handle(new ActionEvent());

        assertTrue(called[0]);
    }

    @Test
    void testSetEditEnabled() {
        MenuItem edit = getMenuItem("editItem");

        controller.setEditEnabled(false);
        assertTrue(edit.isDisable());

        controller.setEditEnabled(true);
        assertFalse(edit.isDisable());
    }

    @Test
    void testSetDeleteEnabled() {
        MenuItem del = getMenuItem("deleteItem");

        controller.setDeleteEnabled(false);
        assertTrue(del.isDisable());

        controller.setDeleteEnabled(true);
        assertFalse(del.isDisable());
    }

    @Test
    void testOnMore_noCrash() {
        Button fake = new Button() {
            @Override
            public Bounds localToScreen(Bounds b) {
                return null;
            }
        };

        setPrivate("moreButton", fake);

        assertDoesNotThrow(() -> callPrivateMethod("onMore", new ActionEvent()));
    }
}
