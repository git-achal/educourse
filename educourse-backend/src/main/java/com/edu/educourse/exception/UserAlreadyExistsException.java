package com.edu.educourse.exception;

/**
 * ============================================================ UserAlreadyExistsException — Custom Exception for
 * Duplicate Registration ============================================================
 *
 * Thrown by AuthService.register() when a client tries to register with a username that already exists in the database.
 *
 * Why extend RuntimeException? ----------------------------- RuntimeException (unchecked exception) means we don't need
 * to declare it in method signatures with "throws". It propagates up the call stack automatically until caught.
 *
 * Who catches this? ----------------- GlobalExceptionHandler catches this and returns a 409 Conflict HTTP response with
 * a clear error message.
 *
 * Example response when this is thrown: { "status": 409, "error": "Conflict", "message": "Username 'john' is already
 * taken. Please choose a different username.", "timestamp": "2025-01-01T12:00:00" }
 * ============================================================
 */
public class UserAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the exception with a descriptive message.
	 *
	 * The message is passed to RuntimeException and stored internally. GlobalExceptionHandler retrieves it via
	 * ex.getMessage() to include it in the JSON error response body.
	 *
	 * @param message a description of why the exception was thrown e.g., "Username 'john' is already taken."
	 */
	public UserAlreadyExistsException(String message) {
		super(message);
	}
}
