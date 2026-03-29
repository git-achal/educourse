package com.edu.educourse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edu.educourse.util.JwtUtil;

/**
 * ============================================================
 *  JwtService — Service Layer for JWT Operations
 * ============================================================
 *
 *  Acts as a facade (wrapper) over JwtUtil.
 *
 *  Why have JwtService if JwtUtil already exists?
 *  ------------------------------------------------
 *  Separation of concerns:
 *    - JwtUtil  → low-level library operations (build/parse tokens)
 *    - JwtService → application-level logic (what to do with tokens)
 *
 *  Benefits:
 *    1. JwtAuthFilter and AuthService only depend on JwtService,
 *       not the low-level JwtUtil. Easier to change implementation.
 *    2. Business rules (e.g., validateToken logic) live here,
 *       not in the utility class.
 *    3. Easier to mock in unit tests — inject a mock JwtService
 *       instead of dealing with JwtUtil's key configuration.
 *
 *  Flow summary:
 *  -------------
 *  Login:     AuthService → JwtService.generateToken() → JwtUtil.generateToken()
 *  Request:   JwtAuthFilter → JwtService.extractUsername() → JwtUtil.extractUsername()
 *  Validate:  JwtAuthFilter → JwtService.validateToken() → JwtUtil methods
 * ============================================================
 */
@Service
public class JwtService {

    /**
     * Inject JwtUtil which handles the raw JJWT library operations.
     * JwtService delegates to JwtUtil for all token operations.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Generates a new signed JWT token for the given username.
     *
     * Called after successful authentication in AuthService.login().
     * The token is returned to the client who must include it
     * in the Authorization header of all future requests.
     *
     * @param username the authenticated user's username (stored as JWT subject)
     * @return a signed JWT string (header.payload.signature)
     */
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }

    /**
     * Extracts the username from a JWT token string.
     *
     * Called by JwtAuthFilter on every incoming request.
     * The extracted username is used to load the user from the
     * database via CustomUserDetailsService.
     *
     * @param token the raw JWT string (without "Bearer " prefix)
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    /**
     * Validates a JWT token by checking two things:
     *
     *   1. USERNAME MATCH:
     *      The username embedded in the token must match the username
     *      of the user loaded from the database.
     *      This prevents a token issued for "john" from being used
     *      to authenticate as "admin".
     *
     *   2. EXPIRY CHECK:
     *      The token must not be expired.
     *      Tokens expire after the duration set in jwt.expiration (default: 24h).
     *      After expiry, the user must log in again to get a new token.
     *
     * Called by: JwtAuthFilter — if this returns false, the request
     * is not authenticated and Spring Security blocks access.
     *
     * @param token    the raw JWT string to validate
     * @param username the expected username (loaded from DB)
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validateToken(String token, String username) {
        // Extract the username claim from the token
        final String extractedUsername = jwtUtil.extractUsername(token);

        // Valid only if BOTH conditions are true:
        //   - the username in the token matches the loaded user's username
        //   - the token has not yet expired
        return (extractedUsername.equals(username) && !jwtUtil.isTokenExpired(token));
    }
}
