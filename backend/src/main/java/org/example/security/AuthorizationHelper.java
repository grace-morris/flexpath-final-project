package org.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Small shared helper so every controller doesn't repeat the same
 * check for admin
 */
public class AuthorizationHelper {

    private AuthorizationHelper() {
    }

    /**
     * check for admin
     * @param authentication the authentication instance for the user
     * @return whether the user is admin
     */
    public static boolean isAdmin(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals("ADMIN")) {
                return true;
            }
        }
        return false;
    }
}
