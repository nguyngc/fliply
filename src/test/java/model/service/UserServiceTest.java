package model.service;

import model.dao.UserDao;
import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userDao = new UserDao();
    }

    @Test
    void register_newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String email = "register+" + uid + "@test.com";
        String password = "password123";

        User u = userService.register(email, password, "Nhut", "Vo");

        assertNotNull(u);
        assertNotNull(u.getUserId());
        assertEquals(email, u.getEmail());
        assertEquals(password, u.getPassword());
        assertEquals("Nhut", u.getFirstName());
        assertEquals("Vo", u.getLastName());
        assertEquals(0, u.getRole()); // default student role

        // cleanup
        userDao.delete(u);
    }

    @Test
    void register_duplicateEmail_throwsException() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String email = "duplicate+" + uid + "@test.com";

        // First registration succeeds
        User first = userService.register(email, "pass1", "First", "User");
        assertNotNull(first);

        // Second registration with same email throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(email, "pass2", "Second", "User");
        });

        // cleanup
        userDao.delete(first);
    }

    @Test
    void login_validCredentials() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String email = "login+" + uid + "@test.com";
        String password = "password123";

        // First register a user
        User registered = userService.register(email, password, "Test", "Login");
        assertNotNull(registered);

        // Then login
        User loggedIn = userService.login(email, password);
        assertNotNull(loggedIn);
        assertEquals(registered.getUserId(), loggedIn.getUserId());
        assertEquals(email, loggedIn.getEmail());

        // cleanup
        userDao.delete(registered);
    }

    @Test
    void login_invalidCredentials() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String email = "invalid+" + uid + "@test.com";

        // Try login with non-existent user
        User result = userService.login(email, "wrongpass");
        assertNull(result);
    }

    @Test
    void login_wrongPassword() {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String email = "wrongpass+" + uid + "@test.com";
        String password = "correctpass";

        // Register user
        User registered = userService.register(email, password, "Test", "User");
        assertNotNull(registered);

        // Try login with wrong password
        User result = userService.login(email, "wrongpassword");
        assertNull(result);

        // cleanup
        userDao.delete(registered);
    }
}
