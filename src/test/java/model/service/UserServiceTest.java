package model.service;

import model.dao.UserDao;
import model.datasource.MariaDbJPAConnection;
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
    void login_createUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        String googleId = "gid-" + uid;
        String email = "u+" + uid + "@test.com";

        // if no user -> create new
        User u = userService.loginOrCreateUser(googleId, email, "Nhut", "Vo");

        assertNotNull(u);
        assertNotNull(u.getUserId());
        assertEquals(googleId, u.getGoogleId());
        assertEquals(email, u.getEmail());
        assertEquals("Nhut", u.getFirstName());
        assertEquals("Vo", u.getLastName());
        assertEquals(1, u.getRole());

        // cleanup
        userDao.delete(u);
    }

    @Test
    void login_existingUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        String googleId = "gid-" + uid;
        String email = "u+" + uid + "@test.com";

        // create user in DB
        User seeded = new User();
        seeded.setGoogleId(googleId);
        seeded.setEmail(email);
        seeded.setFirstName("Old");
        seeded.setLastName("Name");
        seeded.setRole(2);
        userDao.persist(seeded);

        Integer existingId = seeded.getUserId();
        assertNotNull(existingId);


        User result = userService.loginOrCreateUser(googleId, "new@mail.com", "New", "User");

        assertNotNull(result);
        assertEquals(existingId, result.getUserId());
        assertEquals("Old", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals(email, result.getEmail());
        assertEquals(googleId, result.getGoogleId());
        assertEquals(2, result.getRole());

        // cleanup
        userDao.delete(result);
    }
}
