package com.edu.educourse.dto;

/**
 * ============================================================
 *  AuthResponse — DTO for Login Response
 * ============================================================
 *
 *  Sent back to the client after a successful login.
 *  Contains the JWT token the client must use for all
 *  subsequent requests to protected endpoints.
 *
 *  JSON Response Example:
 *  ----------------------
 *  {
 *    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIi...",
 *    "tokenType": "Bearer"
 *  }
 *
 *  How the client uses this token:
 *  --------------------------------
 *  Include it in every protected request as an HTTP header:
 *    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 *  The JwtAuthFilter reads this header on every request,
 *  extracts and validates the token, and sets the user's
 *  authentication in the SecurityContext automatically.
 * ============================================================
 */
public class AuthResponse {

    /**
     * The signed JWT token string.
     *
     * Structure: header.payload.signature
     * Example:   eyJhbGci....eyJzdWIi....SflKxw...
     *
     * The payload contains:
     *   - sub (subject) → the username
     *   - iat (issuedAt) → token creation timestamp
     *   - exp (expiration) → token expiry timestamp
     */
    private String token;

    /**
     * The token type — always "Bearer" for JWT.
     *
     * "Bearer" is an HTTP authentication scheme defined in RFC 6750.
     * The client must prefix the token with "Bearer " in the header:
     *   Authorization: Bearer <token>
     */
    private String tokenType = "Bearer";

    // ── Constructors ─────────────────────────────────────────────

    /**
     * Default no-args constructor.
     * Required by Jackson for JSON serialization.
     */
    public AuthResponse() {
    }

    /**
     * Constructor used by AuthService after successful login.
     * tokenType defaults to "Bearer".
     *
     * @param token the signed JWT string
     */
    public AuthResponse(String token) {
        this.token = token;
    }

    /**
     * Full constructor — used when both token and tokenType are needed.
     *
     * @param token     the signed JWT string
     * @param tokenType the type (typically "Bearer")
     */
    public AuthResponse(String token, String tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    /**
     * Returns the JWT token string.
     * Jackson serializes this to the "token" field in the JSON response.
     *
     * @return the JWT token string
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the JWT token string.
     *
     * @param token the JWT token string
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the token type string ("Bearer").
     * Helps the client know how to use the token in the Authorization header.
     *
     * @return token type string
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the token type.
     *
     * @param tokenType the token type string
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
