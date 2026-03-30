package com.letsplay.userservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/users");
    }

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("User not found"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("/api/users", response.getBody().getPath());
    }

    @Test
    void handleBadRequestException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequestException(
                new BadRequestException("Invalid input"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions_ShouldReturnValidationErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "email", "must be valid"));

        Method method = DummyValidator.class.getDeclaredMethod("dummyMethod", String.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().getError());
        assertNotNull(response.getBody().getValidationErrors());
        assertTrue(response.getBody().getValidationErrors().contains("email: must be valid"));
    }

    @Test
    void handleConstraintViolationException_ShouldReturnCollectedViolations() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(propertyPath.toString()).thenReturn("user.email");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must be valid");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getValidationErrors().contains("user.email: must be valid"));
    }

    @Test
    void handleDuplicateKeyException_ShouldHandleEmailAndGenericMessages() {
        ResponseEntity<ErrorResponse> emailConflict = handler.handleDuplicateKeyException(
                new DuplicateKeyException("duplicate key error on email"), request);

        assertEquals(HttpStatus.CONFLICT, emailConflict.getStatusCode());
        assertEquals("Email address already exists", emailConflict.getBody().getMessage());

        ResponseEntity<ErrorResponse> genericConflict = handler.handleDuplicateKeyException(
                new DuplicateKeyException("duplicate key"), request);

        assertEquals(HttpStatus.CONFLICT, genericConflict.getStatusCode());
        assertEquals("Duplicate entry found", genericConflict.getBody().getMessage());
    }

    @Test
    void handleSecurityExceptions_ShouldMapToExpectedStatusCodes() {
        ResponseEntity<ErrorResponse> accessDenied = handler.handleAccessDeniedException(
                new AccessDeniedException("denied"), request);
        ResponseEntity<ErrorResponse> badCredentials = handler.handleBadCredentialsException(
                new BadCredentialsException("bad creds"), request);

        assertEquals(HttpStatus.FORBIDDEN, accessDenied.getStatusCode());
        assertEquals("Access denied", accessDenied.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, badCredentials.getStatusCode());
        assertEquals("Invalid credentials", badCredentials.getBody().getMessage());
    }

    @Test
    void handleRuntimeAndGenericException_ShouldReturnExpectedResponses() {
        ResponseEntity<ErrorResponse> runtimeResponse = handler.handleRuntimeException(
                new RuntimeException("runtime error"), request);
        ResponseEntity<ErrorResponse> genericResponse = handler.handleGenericException(
                new Exception("boom"), request);

        assertEquals(HttpStatus.BAD_REQUEST, runtimeResponse.getStatusCode());
        assertEquals("runtime error", runtimeResponse.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericResponse.getStatusCode());
        assertEquals("An unexpected error occurred", genericResponse.getBody().getMessage());
    }

    private static class DummyValidator {
        @SuppressWarnings("unused")
        public void dummyMethod(String value) {
            // no-op
        }
    }
}
