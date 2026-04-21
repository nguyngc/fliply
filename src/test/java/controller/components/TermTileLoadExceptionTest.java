package controller.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class TermTileLoadExceptionTest {

    @Test
    void constructorStoresMessageAndCause() {
        RuntimeException cause = new RuntimeException("boom");

        TermTileLoadException exception = new TermTileLoadException("load failed", cause);

        assertEquals("load failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
