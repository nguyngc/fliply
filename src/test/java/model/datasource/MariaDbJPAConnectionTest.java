package model.datasource;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MariaDbJPAConnectionTest {

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<MariaDbJPAConnection> ctor = MariaDbJPAConnection.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    @Test
    void getDbMode_usesSystemPropertyAndDefaultFallback() throws Exception {
        Method method = MariaDbJPAConnection.class.getDeclaredMethod("getDbMode");
        method.setAccessible(true);

        System.setProperty("fliply.db.mode", "h2");
        assertEquals("h2", method.invoke(null));

        System.clearProperty("fliply.db.mode");
        Object fallback = method.invoke(null);
        assertEquals("mariadb", fallback);
    }

    @Test
    void buildDbProperties_usesH2WhenConfigured() throws Exception {
        Method method = MariaDbJPAConnection.class.getDeclaredMethod("buildDbProperties");
        method.setAccessible(true);

        System.setProperty("fliply.db.mode", "h2");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) method.invoke(null);

        assertEquals("org.h2.Driver", props.get("jakarta.persistence.jdbc.driver"));
        assertTrue(String.valueOf(props.get("jakarta.persistence.jdbc.url")).startsWith("jdbc:h2:mem:fliply"));
    }

    @Test
    void createEntityManager_returnsOpenEntityManager() {
        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            assertNotNull(em);
            assertTrue(em.isOpen());
        }
    }

    @Test
    void shutdown_thenCreateEntityManager_recreatesFactory() {
        MariaDbJPAConnection.shutdown();

        try (EntityManager em = MariaDbJPAConnection.createEntityManager()) {
            assertNotNull(em);
            assertTrue(em.isOpen());
        }
    }

    @org.junit.jupiter.api.AfterEach
    void restoreModeProperty() {
        System.setProperty("fliply.db.mode", "h2");
    }
}


