package util;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DialogsTest {

    static { new JFXPanel(); }

    @AfterEach
    void tearDown() {
        Dialogs.resetAlertFactory();
    }

    @Test
    void showUsesConfiguredFactoryAndPopulatesAlert() {
        AtomicReference<Alert> alertRef = new AtomicReference<>();
        CountDownLatch created = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        Dialogs.setAlertFactory(type -> {
            Alert alert = new Alert(type);
            alertRef.set(alert);
            created.countDown();
            return alert;
        });

        Platform.runLater(() -> {
            Dialogs.show(Alert.AlertType.INFORMATION, "Title", "Content");
            finished.countDown();
        });

        await(created);
        Alert alert = alertRef.get();
        assertNotNull(alert);
        Platform.runLater(alert::hide);
        await(finished);
        assertEquals("Title", alert.getTitle());
        assertEquals("Content", alert.getContentText());
    }

    @Test
    void resetAlertFactoryRestoresDefaultFactory() {
        Dialogs.setAlertFactory(Alert::new);
        Dialogs.resetAlertFactory();

        assertNotNull(Dialogs.alertFactory);
    }

    @Test
    void setAlertFactoryNullFallsBackToDefault() {
        Dialogs.setAlertFactory(null);

        assertNotNull(Dialogs.alertFactory);
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for JavaFX task");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}





