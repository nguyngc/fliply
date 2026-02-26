package model.service;

import model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @BeforeEach
    void reset() {
        // Reset static state
        SessionService.setRole(null);
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
