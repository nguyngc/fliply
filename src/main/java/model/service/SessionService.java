package model.service;

import model.UserRole;

public class SessionService {
    private static UserRole role;

    public static UserRole getRole() {
        return role;
    }

    public static void setRole(UserRole r) {
        role = r;
    }
}
