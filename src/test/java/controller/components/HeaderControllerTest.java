package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.LocaleManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class HeaderControllerTest {
    static { new JFXPanel(); }
    private HeaderController controller;
    private Locale previousLocale;

    @BeforeEach
    void setUp() {
        previousLocale = LocaleManager.getLocale();
        LocaleManager.setLocale(Locale.of("en", "US"));
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
        setPrivate("backIcon", new ImageView());

        // Gọi initialize() để setup menu, actionsVisible(false)
        callPrivate("initialize");
    }

    @AfterEach
    void tearDown() {
        LocaleManager.setLocale(previousLocale);
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
    void testSetSubtitle_hiddenWhenNull() {
        controller.setSubtitle(null);
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

    @Test
    void testBackActionWithoutCallbackDoesNothing() {
        assertDoesNotThrow(() -> callPrivateWithParam("onBack", new ActionEvent()));
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
    void testSetMeta_hiddenWhenNull() {
        controller.setMeta(null);
        HBox row = (HBox) getPrivate("metaRow");

        assertFalse(row.isVisible());
        assertFalse(row.isManaged());
    }

    @Test
    void testSetMetaIcon() {
        WritableImage icon = new WritableImage(1, 1);
        controller.setMetaIcon(icon);

        assertSame(icon, ((ImageView) getPrivate("metaIcon")).getImage());
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
    void testEditActionRuns() {
        final boolean[] called = {false};
        controller.setOnEdit(() -> called[0] = true);

        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");
        menu.getItems().getFirst().getOnAction().handle(new ActionEvent());

        assertTrue(called[0]);
    }

    @Test
    void testDeleteActionRuns() {
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

        assertTrue(menu.getItems().getFirst().isDisable());

        controller.setEditEnabled(true);
        assertFalse(menu.getItems().getFirst().isDisable());
    }

    @Test
    void testDeleteEnabled() {
        controller.setDeleteEnabled(false);
        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");

        assertTrue(menu.getItems().get(1).isDisable());

        controller.setDeleteEnabled(true);
        assertFalse(menu.getItems().get(1).isDisable());
    }

    @Test
    void testEditAndDeleteActionsIgnoreMissingCallbacks() {
        ContextMenu menu = (ContextMenu) getPrivate("moreMenu");

        assertDoesNotThrow(() -> menu.getItems().getFirst().getOnAction().handle(new ActionEvent()));
        assertDoesNotThrow(() -> menu.getItems().get(1).getOnAction().handle(new ActionEvent()));
    }

    @Test
    void testOnMoreDoesNothingWhenButtonHasNoScreenBounds() {
        assertDoesNotThrow(() -> callPrivateWithParam("onMore", new ActionEvent()));
    }

    @Test
    void testInitializeFlipsBackIconForRtlLocale() {
        LocaleManager.setLocale(Locale.of("ar", "AR"));
        HeaderController rtlController = new HeaderController();
        injectField(rtlController, "backButton", new Button());
        injectField(rtlController, "titleLabel", new Label());
        injectField(rtlController, "subtitleLabel", new Label());
        injectField(rtlController, "metaRow", new HBox());
        injectField(rtlController, "metaIcon", new ImageView());
        injectField(rtlController, "metaLabel", new Label());
        injectField(rtlController, "moreButton", new Button());
        injectField(rtlController, "moreIcon", new ImageView());
        ImageView backIcon = new ImageView();
        injectField(rtlController, "backIcon", backIcon);

        callPrivate(rtlController, "initialize");

        assertEquals(-1.0, backIcon.getScaleX());
    }

    private void injectField(HeaderController target, String field, Object value) {
        try {
            Field f = HeaderController.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivate(HeaderController target, String method) {
        try {
            Method m = HeaderController.class.getDeclaredMethod(method);
            m.setAccessible(true);
            m.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
