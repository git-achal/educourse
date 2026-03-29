package com.edu.educourse.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.edu.educourse.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ============================================================
 *  JwtAuthFilter — JWT Authentication Filter
 * ============================================================
 *
 *  This filter runs ONCE for every incoming HTTP request.
 *  It checks for a valid JWT token and authenticates the user
 *  before the request reaches any controller.
 *
 *  Extends OncePerRequestFilter:
 *  ------------------------------
 *  Guarantees this filter runs exactly once per request,
 *  even if the request is forwarded or dispatched internally.
 *
 *  Where it fits in the Spring Security filter chain:
 *  ---------------------------------------------------
 *  Request → [JwtAuthFilter] → [UsernamePasswordAuthenticationFilter] → Controller
 *
 *  We register it BEFORE UsernamePasswordAuthenticationFilter in SecurityConfig
 *  so JWT authentication happens first.
 *
 *  Full request flow for a protected endpoint:
 *  --------------------------------------------
 *  1. Client sends: GET /user/profile
 *     Header: Authorization: Bearer eyJhbGci...
 *
 *  2. JwtAuthFilter runs:
 *     a. Reads the Authorization header
 *     b. Strips "Bearer " prefix → gets the raw JWT
 *     c. Extracts username from JWT
 *     d. Loads user from DB via UserDetailsService
 *     e. Validates the token (username match + not expired)
 *     f. Creates UsernamePasswordAuthenticationToken with user's authorities
 *     g. Sets it in SecurityContextHolder
 *
 *  3. SecurityFilterChain checks:
 *     /user/** → hasAnyRole("ADMIN", "STUDENT")
 *     Reads authorities from SecurityContextHolder → access granted ✓
 *
 *  4. Controller method executes and returns response.
 *
 *  What happens if the token is missing or invalid?
 *  -------------------------------------------------
 *  The SecurityContextHolder is never set.
 *  Spring Security sees no authentication → returns 403 Forbidden.
 * ============================================================
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * JwtService for extracting and validating the JWT token.
     * Wraps the low-level JwtUtil operations.
     */
    @Autowired
    private JwtService jwtService;

    /**
     * UserDetailsService for loading the full user (with roles) from the database.
     * We inject the interface (not CustomUserDetailsService directly) to keep
     * this filter loosely coupled and easier to test.
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Core filter method — executes for every HTTP request.
     *
     * The method either:
     *   A) Authenticates the user and sets SecurityContext (valid JWT)
     *   B) Does nothing and lets the request proceed unauthenticated (no/invalid JWT)
     *      → Spring Security's rules in SecurityConfig then decide to allow or block.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response (not modified by this filter)
     * @param filterChain the remaining filters to execute after this one
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // ── Step 1: Read the Authorization header ─────────────────
        // Standard HTTP header for authentication: "Authorization: Bearer <token>"
        final String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        // ── Step 2: Extract the token from the header ─────────────
        // We only process the header if it exists AND starts with "Bearer "
        // The "Bearer " prefix (7 characters) is the OAuth2/JWT standard prefix.
        // If the header is missing or has a different format, we skip JWT processing.
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Strip the "Bearer " prefix (7 chars) to get the raw JWT string
            // e.g., "Bearer eyJhbGci..." → "eyJhbGci..."
            token = authHeader.substring(7);

            // Extract the username from the JWT's subject claim
            // This decodes the token payload and reads the "sub" field
            // e.g., {"sub": "john", "iat": ..., "exp": ...} → "john"
            username = jwtService.extractUsername(token);
        }

        // ── Step 3: Authenticate if username found and not yet authenticated ──
        // We check SecurityContextHolder.getContext().getAuthentication() == null
        // to avoid re-authenticating on every filter in the chain.
        // If authentication is already set (e.g., from a previous filter), skip.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load the full user details from the database
            // This gives us the user's roles (authorities) and other account details
            // UserDetailsService → CustomUserDetailsService.loadUserByUsername()
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // ── Step 4: Validate the token ────────────────────────
            // validateToken() checks:
            //   1. Username in token == username of loaded user (prevents token substitution)
            //   2. Token is not expired (exp claim is in the future)
            if (jwtService.validateToken(token, userDetails.getUsername())) {

                // ── Step 5: Create the authentication object ──────
                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                //   principal   = userDetails (the full user object)
                //   credentials = null (we don't need the password after JWT validation)
                //   authorities = userDetails.getAuthorities() (the user's roles)
                //     e.g., [SimpleGrantedAuthority("ROLE_ADMIN")]
                //
                // This is what Spring Security checks when evaluating:
                //   .hasRole("ADMIN") or @PreAuthorize("hasRole('ADMIN')")
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                   // who is authenticated
                                null,                          // credentials (not needed)
                                userDetails.getAuthorities()   // what they're allowed to do
                        );

                // Attach additional request details (IP address, session ID)
                // to the authentication token — useful for auditing/logging
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ── Step 6: Set authentication in SecurityContext ─
                // This is the critical step — it tells Spring Security that
                // the current request is authenticated as this user.
                // All subsequent security checks in this request will use this.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ── Step 7: Continue to the next filter ───────────────────
        // Always call filterChain.doFilter() to pass the request along,
        // regardless of whether authentication was set or not.
        // If authentication was NOT set, Spring Security's authorization
        // rules in SecurityConfig will block access to protected endpoints.
        filterChain.doFilter(request, response);
    }
}
