package controller.components;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class PasswordVisibilitySupportTest {

    static { new JFXPanel(); }

    @Test
    void initializeHiddenHidesVisibleFieldAndSetsIcon() {
        TextField visibleField = new TextField();
        ImageView eyeIcon = new ImageView();
        WritableImage eyeClosed = new WritableImage(1, 1);

        PasswordVisibilitySupport.initializeHidden(visibleField, eyeIcon, eyeClosed);

        assertFalse(visibleField.isVisible());
        assertFalse(visibleField.isManaged());
        assertSame(eyeClosed, eyeIcon.getImage());
    }

    @Test
    void initializeHiddenAcceptsNullInputs() {
        assertDoesNotThrow(() -> PasswordVisibilitySupport.initializeHidden(null, null, null));
    }

    @Test
    void applyTrueShowsVisibleFieldAndHidesMaskedField() {
        PasswordField masked = new PasswordField();
        TextField visible = new TextField();
        ImageView eyeIcon = new ImageView();
        WritableImage eyeOpen = new WritableImage(1, 1);
        WritableImage eyeClosed = new WritableImage(1, 1);
        masked.setText("secret");

        PasswordVisibilitySupport.apply(true, masked, visible, eyeIcon, eyeOpen, eyeClosed);

        assertTrue(visible.isVisible());
        assertTrue(visible.isManaged());
        assertFalse(masked.isVisible());
        assertFalse(masked.isManaged());
        assertEquals("secret", visible.getText());
        assertSame(eyeOpen, eyeIcon.getImage());
    }

    @Test
    void applyFalseShowsMaskedFieldAndHidesVisibleField() {
        PasswordField masked = new PasswordField();
        TextField visible = new TextField();
        ImageView eyeIcon = new ImageView();
        WritableImage eyeOpen = new WritableImage(1, 1);
        WritableImage eyeClosed = new WritableImage(1, 1);
        visible.setText("changed");

        PasswordVisibilitySupport.apply(false, masked, visible, eyeIcon, eyeOpen, eyeClosed);

        assertTrue(masked.isVisible());
        assertTrue(masked.isManaged());
        assertFalse(visible.isVisible());
        assertFalse(visible.isManaged());
        assertEquals("changed", masked.getText());
        assertSame(eyeClosed, eyeIcon.getImage());
    }

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<PasswordVisibilitySupport> constructor = PasswordVisibilitySupport.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, thrown.getCause());
    }
}
