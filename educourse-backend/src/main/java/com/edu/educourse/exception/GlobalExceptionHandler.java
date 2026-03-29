package com.edu.educourse.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ============================================================
 *  GlobalExceptionHandler — Centralized Error Handling
 * ============================================================
 *
 *  Without this class: Spring would return an ugly Whitelabel Error Page
 *  or a raw stack trace whenever an exception occurs.
 *
 *  With this class: every exception is caught here and converted
 *  into a clean, structured JSON response that the client can understand.
 *
 *  @RestControllerAdvice:
 *  -----------------------
 *  A combination of @ControllerAdvice + @ResponseBody.
 *  @ControllerAdvice → applies this class globally to ALL @RestController classes.
 *  @ResponseBody     → the return values of handler methods are written as JSON.
 *
 *  How exception handling works:
 *  ------------------------------
 *  1. A method in a @RestController throws an exception.
 *  2. Spring catches the exception and looks for an @ExceptionHandler
 *     in this class that handles that exception type.
 *  3. The matching handler method runs and returns a ResponseEntity.
 *  4. Spring serializes the ResponseEntity body to JSON and sends it to the client.
 *
 *  Standard error response format returned by all handlers:
 *  ----------------------------------------------------------
 *  {
 *    "status": 401,
 *    "error": "Unauthorized",
 *    "message": "Invalid username or password",
 *    "timestamp": "2025-01-01T12:00:00.123456"
 *  }
 * ============================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid on request body DTOs.
     *
     * When is this triggered?
     *   When a field annotated with @NotBlank, @Size, etc. fails validation.
     *   e.g., POST /api/auth/register with { "username": "", "password": "123" }
     *   → @NotBlank on username fails
     *   → @Size(min=6) on password fails
     *   → MethodArgumentNotValidException is thrown by Spring before the controller runs
     *
     * Response format (400 Bad Request):
     * {
     *   "username": "Username is required",
     *   "password": "Password must be at least 6 characters"
     * }
     *
     * Note: this handler returns a different format (field → message map)
     * instead of the standard error format, because multiple fields can fail.
     *
     * @param ex the validation exception containing all field errors
     * @return 400 Bad Request with a map of fieldName → errorMessage
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Collect all field-level validation errors into a map
        Map<String, String> errors = new HashMap<>();

        // getAllErrors() returns a list of ObjectError (and FieldError subtype)
        // We cast each to FieldError to get the specific field name
        ex.getBindingResult().getAllErrors().forEach(error -> {
            // getField() → the name of the DTO field that failed validation
            // e.g., "username", "password"
            String fieldName = ((FieldError) error).getField();

            // getDefaultMessage() → the message from the annotation
            // e.g., "Username is required", "Password must be at least 6 characters"
            String errorMessage = error.getDefaultMessage();

            errors.put(fieldName, errorMessage);
        });

        // Return 400 Bad Request with the map of errors
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handles failed login attempts with wrong username or password.
     *
     * When is this triggered?
     *   When AuthenticationManager.authenticate() fails because:
     *   - The username doesn't exist in the database, OR
     *   - The password doesn't match the stored BCrypt hash.
     *   Spring Security throws BadCredentialsException in both cases
     *   (intentionally vague to prevent username enumeration attacks).
     *
     * Response (401 Unauthorized):
     * {
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Invalid username or password",
     *   "timestamp": "..."
     * }
     *
     * @param ex the BadCredentialsException thrown by Spring Security
     * @return 401 Unauthorized with error details
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    /**
     * Handles the case when a requested username is not found in the DB.
     *
     * When is this triggered?
     *   When CustomUserDetailsService.loadUserByUsername() throws
     *   UsernameNotFoundException because the user doesn't exist.
     *   This can happen during JWT filter processing.
     *
     * Response (404 Not Found):
     * {
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "User not found with username: john",
     *   "timestamp": "..."
     * }
     *
     * @param ex the UsernameNotFoundException with descriptive message
     * @return 404 Not Found with error details
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(
            UsernameNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles duplicate registration attempts.
     *
     * When is this triggered?
     *   When AuthService.register() detects a username already exists
     *   and throws UserAlreadyExistsException.
     *
     * Response (409 Conflict):
     * {
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "Username 'john' is already taken.",
     *   "timestamp": "..."
     * }
     *
     * @param ex the UserAlreadyExistsException with the duplicate username message
     * @return 409 Conflict with error details
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(
            UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Catch-all handler for any unexpected exceptions not handled above.
     *
     * When is this triggered?
     *   Any RuntimeException or other unexpected error that doesn't match
     *   the more specific handlers above.
     *   e.g., NullPointerException, database connection failure, etc.
     *
     * We return a generic message deliberately — to avoid leaking
     * internal implementation details (stack traces, class names) to clients.
     * Log the actual error server-side for debugging.
     *
     * Response (500 Internal Server Error):
     * {
     *   "status": 500,
     *   "error": "Internal Server Error",
     *   "message": "An unexpected error occurred. Please try again later.",
     *   "timestamp": "..."
     * }
     *
     * @param ex the unexpected exception
     * @return 500 Internal Server Error with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // In production, log the actual exception here:
        // log.error("Unexpected error: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    /**
     * Helper method that builds a consistent error response body.
     *
     * Used by all exception handler methods above to ensure
     * all error responses have the same JSON structure.
     *
     * Output format:
     * {
     *   "status": <HTTP status code as integer>,
     *   "error": <HTTP status reason phrase>,
     *   "message": <the human-readable description>,
     *   "timestamp": <ISO-8601 datetime string>
     * }
     *
     * @param status  the HTTP status to send (e.g., HttpStatus.UNAUTHORIZED)
     * @param message the human-readable error description
     * @return a ResponseEntity with the error map as body
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", status.value());           // e.g., 401
        errorBody.put("error", status.getReasonPhrase());  // e.g., "Unauthorized"
        errorBody.put("message", message);                 // e.g., "Invalid username or password"
        errorBody.put("timestamp", LocalDateTime.now().toString()); // e.g., "2025-01-01T12:00:00"

        return ResponseEntity.status(status).body(errorBody);
    }
}
