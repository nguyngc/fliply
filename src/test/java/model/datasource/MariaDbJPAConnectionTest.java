package model.datasource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MariaDbJPAConnectionTest {

    @BeforeAll
    static void initializeClassInH2Mode() throws Exception {
        System.setProperty("fliply.db.mode", "h2");
        Class.forName(MariaDbJPAConnection.class.getName());
    }

    private static Method privateMethod(String name) throws Exception {
        Method method = MariaDbJPAConnection.class.getDeclaredMethod(name);
        method.setAccessible(true);
        return method;
    }

    private static Field privateField(String name) throws Exception {
        Field field = MariaDbJPAConnection.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void reloadEnvConfig() throws Exception {
        Method reload = util.EnvConfig.class.getDeclaredMethod("reload");
        reload.setAccessible(true);
        reload.invoke(null);
    }

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<MariaDbJPAConnection> ctor = MariaDbJPAConnection.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    @Test
    void getDbMode_usesSystemPropertyAndDefaultFallback() throws Exception {
        Method method = privateMethod("getDbMode");

        System.setProperty("fliply.db.mode", "h2");
        assertEquals("h2", method.invoke(null));

        System.clearProperty("fliply.db.mode");
        Object fallback = method.invoke(null);
        assertEquals("mariadb", fallback);
    }

    @Test
    void buildDbProperties_usesH2WhenConfigured() throws Exception {
        Method method = privateMethod("buildDbProperties");

        System.setProperty("fliply.db.mode", "h2");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) method.invoke(null);

        assertEquals("org.h2.Driver", props.get("jakarta.persistence.jdbc.driver"));
        assertTrue(String.valueOf(props.get("jakarta.persistence.jdbc.url")).startsWith("jdbc:h2:mem:fliply"));
    }

    @Test
    void buildDbProperties_usesH2WhenModeIsTest() throws Exception {
        Method method = privateMethod("buildDbProperties");

        System.setProperty("fliply.db.mode", "test");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) method.invoke(null);

        assertEquals("org.h2.Driver", props.get("jakarta.persistence.jdbc.driver"));
        assertEquals("create-drop", props.get("hibernate.hbm2ddl.auto"));
        assertEquals("false", props.get("hibernate.show_sql"));
    }

    @Test
    void buildDbProperties_usesMariaDbSettingsFromDotEnvWhenModeIsBlank() throws Exception {
        Method method = privateMethod("buildDbProperties");
        Path envFile = Files.createTempFile("fliply-db", ".env");

        try {
            Files.writeString(envFile, """
                    FLIPLY_DB_MODE=mariadb
                    DB_HOST=db.example
                    DB_PORT=3306
                    DB_NAME=fliply_test
                    DB_USER=tester
                    DB_PASS=secret
                    """);
            System.setProperty("fliply.db.mode", "   ");
            System.setProperty("fliply.env.file", envFile.toString());
            reloadEnvConfig();

            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) method.invoke(null);

            assertEquals("org.mariadb.jdbc.Driver", props.get("jakarta.persistence.jdbc.driver"));
            assertEquals("jdbc:mariadb://db.example:3306/fliply_test", props.get("jakarta.persistence.jdbc.url"));
            assertEquals("tester", props.get("jakarta.persistence.jdbc.user"));
            assertEquals("secret", props.get("jakarta.persistence.jdbc.password"));
            assertEquals("org.hibernate.dialect.MariaDBDialect", props.get("hibernate.dialect"));
            assertEquals("update", props.get("hibernate.hbm2ddl.auto"));
            assertEquals("true", props.get("hibernate.show_sql"));
        } finally {
            Files.deleteIfExists(envFile);
        }
    }

    @Test
    void getDbMode_usesEnvFallbackWhenSystemPropertyBlank() throws Exception {
        Method method = privateMethod("getDbMode");
        Path envFile = Files.createTempFile("fliply-db-mode", ".env");

        try {
            Files.writeString(envFile, "FLIPLY_DB_MODE=test\n");
            System.setProperty("fliply.db.mode", " ");
            System.setProperty("fliply.env.file", envFile.toString());
            reloadEnvConfig();

            assertEquals("test", method.invoke(null));
        } finally {
            Files.deleteIfExists(envFile);
        }
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

    @Test
    void shutdown_ignoresNullAndClosedFactories() throws Exception {
        Field emfField = privateField("emf");
        Object original = emfField.get(null);

        try {
            emfField.set(null, null);
            assertDoesNotThrow(MariaDbJPAConnection::shutdown);

            EntityManagerFactory closedFactory = mock(EntityManagerFactory.class);
            when(closedFactory.isOpen()).thenReturn(false);
            emfField.set(null, closedFactory);

            assertDoesNotThrow(MariaDbJPAConnection::shutdown);
            verify(closedFactory, never()).close();
        } finally {
            emfField.set(null, original);
        }
    }

    @org.junit.jupiter.api.AfterEach
    void restoreModeProperty() throws Exception {
        System.clearProperty("fliply.env.file");
        System.setProperty("fliply.db.mode", "h2");
        reloadEnvConfig();
    }
}
