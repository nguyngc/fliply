package model.service;

import model.dao.UserDao;
import model.entity.User;

public class UserService {

    private final UserDao userDao = new UserDao();

    // login/register
    public User loginOrCreateUser(String googleId, String email, String firstName, String lastName) {

        User existing = userDao.findByGoogleId(googleId);
        if (existing != null) return existing;

        User u = new User();
        u.setGoogleId(googleId);
        u.setEmail(email);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(1); // default student

        userDao.persist(u);
        return u;
    }
}
