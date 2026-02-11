package model.service;

import model.dao.UserDao;
import model.entity.User;

public class UserService {

    private final UserDao userDao = new UserDao();

    public User register(String email, String password, String firstName, String lastName) {
        if (userDao.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User u = new User();
        u.setEmail(email);
        u.setPassword(password);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(0); // default student 0)

        userDao.persist(u);
        return u;
    }

    public User login(String email, String password) {
        return userDao.findByEmailAndPassword(email, password);
    }
}
