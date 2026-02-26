package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private void setId(User u, int id) {
        try {
            Field f = User.class.getDeclaredField("userId");
            f.setAccessible(true);
            f.set(u, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserId() {
        User u = new User();
        setId(u, 10);
        assertEquals(10, u.getUserId());
    }

    @Test
    void getFirstName() {
        User u = new User();
        u.setFirstName("John");
        assertEquals("John", u.getFirstName());
    }

    @Test
    void getLastName() {
        User u = new User();
        u.setLastName("Doe");
        assertEquals("Doe", u.getLastName());
    }

    @Test
    void getEmail() {
        User u = new User();
        u.setEmail("a@b.com");
        assertEquals("a@b.com", u.getEmail());
    }

    @Test
    void getPassword() {
        User u = new User();
        u.setPassword("123");
        assertEquals("123", u.getPassword());
    }

    @Test
    void getRole() {
        User u = new User();
        u.setRole(1);
        assertEquals(1, u.getRole());
    }

    @Test
    void setFirstName() {
        User u = new User();
        u.setFirstName("Alice");
        assertEquals("Alice", u.getFirstName());
    }

    @Test
    void setLastName() {
        User u = new User();
        u.setLastName("Smith");
        assertEquals("Smith", u.getLastName());
    }

    @Test
    void setEmail() {
        User u = new User();
        u.setEmail("x@y.com");
        assertEquals("x@y.com", u.getEmail());
    }

    @Test
    void setPassword() {
        User u = new User();
        u.setPassword("pw");
        assertEquals("pw", u.getPassword());
    }

    @Test
    void setRole() {
        User u = new User();
        u.setRole(0);
        assertEquals(0, u.getRole());
    }

    @Test
    void testToString() {
        User u = new User();
        setId(u, 5);
        u.setFirstName("A");
        u.setLastName("B");
        u.setEmail("c@d.com");
        u.setPassword("pw");
        u.setRole(1);

        String s = u.toString();
        assertTrue(s.contains("userId=5"));
        assertTrue(s.contains("firstName='A'"));
        assertTrue(s.contains("lastName='B'"));
        assertTrue(s.contains("email='c@d.com'"));
        assertTrue(s.contains("password='pw'"));
        assertTrue(s.contains("role=1"));
    }

    @Test
    void isTeacher() {
        User u = new User();
        u.setRole(1);
        assertTrue(u.isTeacher());
        assertFalse(u.isStudent());
    }

    @Test
    void isStudent() {
        User u = new User();
        u.setRole(0);
        assertTrue(u.isStudent());
        assertFalse(u.isTeacher());
    }
}
