package model.service;

import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @BeforeEach
    void reset() {
        // Reset static state
        SessionService.setRole(null);
    }

    @Test
    void utilityConstructorThrows() throws Exception {
        Constructor<SessionService> ctor = SessionService.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, ctor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    @Test
    void testSetAndGetRole() {
        SessionService.setRole(UserRole.TEACHER);
        assertEquals(UserRole.TEACHER, SessionService.getRole());
    }

    @Test
    void testChangeRole() {
        SessionService.setRole(UserRole.STUDENT);
        assertEquals(UserRole.STUDENT, SessionService.getRole());

        SessionService.setRole(UserRole.TEACHER);
        assertEquals(UserRole.TEACHER, SessionService.getRole());
    }

    @Test
    void testSetRoleToNull() {
        SessionService.setRole(UserRole.TEACHER);
        assertNotNull(SessionService.getRole());

        SessionService.setRole(null);
        assertNull(SessionService.getRole());
    }
}
