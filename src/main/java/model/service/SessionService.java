package model.service;

import model.UserRole;

/**
 * In-memory session holder for the current authenticated user's role.
 */
public final class SessionService {
    private static UserRole role;

    private SessionService() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns the currently cached role, or null when no session role is set.
     */
    public static UserRole getRole() {
        return role;
    }

    /**
     * Updates the cached role for the active session.
     */
    public static void setRole(UserRole r) {
        role = r;
    }
}
