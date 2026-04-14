package util;

import javafx.scene.control.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DialogsTest {

    @AfterEach
    void tearDown() {
        Dialogs.resetAlertFactory();
    }

    @Test
    void resetAlertFactoryRestoresDefaultFactory() {
        Function<Alert.AlertType, Alert> customFactory = type -> null;
        Dialogs.setAlertFactory(customFactory);

        Dialogs.resetAlertFactory();

        assertNotNull(Dialogs.alertFactory);
    }

    @Test
    void setAlertFactoryUsesProvidedFactory() {
        Function<Alert.AlertType, Alert> customFactory = type -> null;

        Dialogs.setAlertFactory(customFactory);

        assertSame(customFactory, Dialogs.alertFactory);
    }

    @Test
    void setAlertFactoryNullFallsBackToDefault() {
        Dialogs.setAlertFactory(null);

        assertNotNull(Dialogs.alertFactory);
    }
}
