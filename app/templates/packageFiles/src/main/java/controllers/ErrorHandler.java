package <%=packageName%>.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.micrometer.spring.web.ControllerMetrics;

/**
 * A sample error handler. You can add your own exceptions below to control the error codes that should be used in
 * various error scenarios.
 */
@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @Autowired
    ControllerMetrics metrics;

    @ExceptionHandler({ RuntimeException.class })
    protected ResponseEntity<Object> handleGenericError(RuntimeException e, WebRequest request) {
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    protected ResponseEntity<Object> handleBadRequest(IllegalArgumentException e, WebRequest request) {
        return handleException(e, request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> handleException(final RuntimeException e, WebRequest request,
        HttpStatus httpStatus) {
        metrics.tagWithException(e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> error = new HashMap<>();
        error.put("errorMessage", e.getMessage());
        if (e.getCause() != null) {
            error.put("cause", e.getCause().getMessage());
        }
        return handleExceptionInternal(e, error, headers, httpStatus, request);
    }
}

