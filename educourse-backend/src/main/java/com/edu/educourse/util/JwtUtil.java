package com.edu.educourse.util;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * ============================================================
 *  JwtUtil — Low-level JWT Token Operations
 * ============================================================
 *
 *  This class handles all raw JWT operations using the JJWT library:
 *    - Token generation (signing with secret key)
 *    - Token parsing (extracting claims)
 *    - Token validation (expiry check)
 *
 *  JWT Structure:
 *  --------------
 *  A JWT has 3 parts separated by dots: header.payload.signature
 *
 *  Header  (Base64): {"alg":"HS256","typ":"JWT"}
 *  Payload (Base64): {"sub":"john","iat":1700000000,"exp":1700086400}
 *  Signature       : HMAC-SHA256(header + "." + payload, secretKey)
 *
 *  The signature ensures the token hasn't been tampered with.
 *  Anyone can decode the header/payload — they are Base64, not encrypted.
 *  But without the secret key, no one can forge a valid signature.
 *
 *  Key design:
 *  -----------
 *  JwtUtil is used only by JwtService.
 *  Controllers and filters use JwtService, not JwtUtil directly.
 *  This keeps responsibilities layered and testable.
 *
 *  @Component → Spring manages this as a singleton bean.
 *  @Value     → injects values from application.yaml at startup.
 * ============================================================
 */
@Component
public class JwtUtil {

    /**
     * The secret key string injected from application.yaml (jwt.secret).
     *
     * Used to sign tokens when generating, and to verify the
     * signature when parsing/validating tokens.
     *
     * Requirements:
     *  - Must be at least 32 characters (256 bits) for HMAC-SHA256.
     *  - In production: inject via environment variable, never hardcode.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token validity duration in milliseconds, from application.yaml (jwt.expiration).
     *
     * Default: 86400000 ms = 24 hours
     * After expiration, the token is rejected and the user must log in again.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Builds a cryptographic SecretKey from the plain-text secret string.
     *
     * Why Keys.hmacShaKeyFor() instead of the old approach?
     * -------------------------------------------------------
     * The old API used: .signWith(SignatureAlgorithm.HS256, secret)
     * This is DEPRECATED in JJWT 0.11.5 because it accepted weak keys.
     *
     * Keys.hmacShaKeyFor(bytes) creates a proper javax.crypto.SecretKey
     * object, which JJWT verifies is strong enough for HMAC-SHA256.
     *
     * This method is called each time a token is generated or parsed
     * (rather than storing the key as a field, to avoid any state issues).
     *
     * @return a SecretKey suitable for HMAC-SHA256 signing
     */
    private SecretKey getSigningKey() {
        // Convert the secret String to a byte array and wrap in a SecretKey
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a signed JWT token for the given username.
     *
     * Token claims set:
     *   - subject (sub)    → the username (used to identify the user)
     *   - issuedAt (iat)   → current timestamp (when was the token created)
     *   - expiration (exp) → issuedAt + jwtExpiration (when does it expire)
     *
     * The token is then signed with HMAC-SHA256 using our secret key.
     * This produces the 3-part "header.payload.signature" JWT string.
     *
     * Called by: JwtService.generateToken() → AuthService.login()
     *
     * @param username the authenticated user's username to embed in the token
     * @return a compact JWT string (e.g., "eyJhbGci...eyJzdWIi...SflKxw")
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)                                                // who the token is for
                .setIssuedAt(new Date())                                             // issued right now
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // expires after 24h
                .signWith(getSigningKey())                                           // sign with HMAC-SHA256
                .compact();                                                          // build the final string
    }

    /**
     * Extracts the username (subject claim) from a JWT token.
     *
     * Uses extractClaim() with a Claims::getSubject resolver
     * to specifically get the "sub" field from the token payload.
     *
     * Called by: JwtService.extractUsername() → JwtAuthFilter
     *
     * @param token the JWT string to parse
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        // Claims::getSubject is a method reference equivalent to: claims -> claims.getSubject()
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * Uses extractClaim() with a Claims::getExpiration resolver
     * to get the "exp" field from the token payload.
     *
     * Called by: isTokenExpired() below
     *
     * @param token the JWT string to parse
     * @return the Date when this token expires
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract ANY claim from a JWT token.
     *
     * This is a flexible helper used by extractUsername() and
     * extractExpiration(). You can pass any Function<Claims, T>
     * to extract whichever field you need.
     *
     * How it works:
     *   1. Parses the token to get all Claims
     *   2. Applies the claimsResolver function to extract the specific claim
     *
     * Example usages:
     *   extractClaim(token, Claims::getSubject)    → returns username String
     *   extractClaim(token, Claims::getExpiration) → returns expiry Date
     *
     * @param token          the JWT string to parse
     * @param claimsResolver a function that extracts a specific field from Claims
     * @param <T>            the return type (String for subject, Date for expiration, etc.)
     * @return the value of the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token); // parse the full token
        return claimsResolver.apply(claims);           // apply the resolver to get the specific field
    }

    /**
     * Parses the JWT token and returns all claims from its payload.
     *
     * This is the core parsing step:
     *   1. parserBuilder() → creates a new JWT parser
     *   2. setSigningKey()  → tells the parser which key to use for signature verification
     *   3. build()          → builds the parser
     *   4. parseClaimsJws() → parses and verifies the token; throws exceptions if invalid
     *   5. getBody()        → returns the Claims (the payload)
     *
     * Exceptions thrown by parseClaimsJws() (handled by GlobalExceptionHandler):
     *   - ExpiredJwtException   → token has expired
     *   - MalformedJwtException → token format is invalid
     *   - SignatureException    → signature doesn't match (tampered token)
     *   - UnsupportedJwtException → token type is not supported
     *
     * @param token the JWT string to parse
     * @return Claims object containing all token payload data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // verify signature with our secret key
                .build()
                .parseClaimsJws(token)          // parse the token (throws if invalid/expired)
                .getBody();                     // get the payload (claims)
    }

    /**
     * Checks whether the JWT token has expired.
     *
     * Extracts the expiration Date from the token and compares
     * it with the current date/time.
     *
     * Called by: JwtService.validateToken()
     *
     * @param token the JWT string to check
     * @return true if the token has expired, false if still valid
     */
    public boolean isTokenExpired(String token) {
        // extractExpiration returns the "exp" Date from the token payload
        // .before(new Date()) checks if that date is in the past
        return extractExpiration(token).before(new Date());
    }
}
