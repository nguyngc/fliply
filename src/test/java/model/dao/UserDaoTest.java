package model.dao;

import model.entity.User;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        // Clean any existing test data that might conflict
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        cleanupTestData();
    }

    private void cleanupTestData() {
        // Clean any users with test emails to avoid conflicts
        try {
            User existingUser = userDao.findByEmail("nhut@test.com");
            if (existingUser != null) {
                userDao.delete(existingUser);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private User newUser() {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("test+" + uid + "@example.com");
        u.setPassword("password123");
        u.setRole(1);
        return u;
    }

    @Test
    void crudUser() {
        // CREATE
        User u = newUser();
        userDao.persist(u);
        assertNotNull(u.getUserId());

        Integer id = u.getUserId();

        // READ
        User found = userDao.find(id);
        assertNotNull(found);
        assertEquals(u.getEmail(), found.getEmail());
        assertEquals(u.getPassword(), found.getPassword());

        // UPDATE
        found.setLastName("VoUpdated");
        found.setRole(2);
        userDao.update(found);

        User updated = userDao.find(id);
        assertNotNull(updated);
        assertEquals("VoUpdated", updated.getLastName());
        assertEquals(2, updated.getRole());

        // DELETE
        userDao.delete(updated);
        assertNull(userDao.find(id));
    }

    @Test
    void queryUser() {
        // setup
        User u = newUser();
        userDao.persist(u);
        Integer id = u.getUserId();
        assertNotNull(id);

        try {
            // findByEmail
            User byEmail = userDao.findByEmail(u.getEmail());
            assertNotNull(byEmail);
            assertEquals(id, byEmail.getUserId());

            // existsByEmail
            assertTrue(userDao.existsByEmail(u.getEmail()));

            // findByEmailAndPassword
            User byEmailAndPassword = userDao.findByEmailAndPassword(u.getEmail(), u.getPassword());
            assertNotNull(byEmailAndPassword);
            assertEquals(id, byEmailAndPassword.getUserId());

        } finally {
            // cleanup - ensure deletion even if test fails
            userDao.delete(u);
            assertFalse(userDao.existsByEmail(u.getEmail()));
        }
    }
}
