package com.cleevio.vexl.common.config;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ExceptionConfig {

    @ExceptionHandler({ApiException.class})
    public final ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        final ResponseStatus status = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        if (status != null) {
            return handleException(Collections.singleton(ex.getMessage()), ex.getErrorCode(), status.code());
        }

        return handleException(Collections.singleton(ex.getMessage()), ex.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This exception occurs if request body is missing or has invalid format
     *
     * @param ex      Exception
     * @param request Request
     * @return Error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<ErrorResponse> handleBodyMissingException(Exception ex, WebRequest request) {
        return handleException(Collections.singleton("Invalid or missing body"), "0", HttpStatus.BAD_REQUEST);
    }

    /**
     * This exception occurs if body does not meet required constraints
     *
     * @param ex      Exception
     * @param request Request
     * @return Error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorResponse> handleBodyInvalidException(MethodArgumentNotValidException ex, WebRequest request) {
        Set<String> output = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.toSet());

        output.addAll(ex.getBindingResult().getGlobalErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toSet()));

        return handleException(output, "0", HttpStatus.BAD_REQUEST);
    }

    /**
     * This exception occurs if body does not meet required constraints
     *
     * @param ex      Exception
     * @param request Request
     * @return Error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<ErrorResponse> handleConstraintException(ConstraintViolationException ex, WebRequest request) {
        Set<String> output = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.toSet());

        return handleException(output, "0", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            BindException.class
    })
    public final ResponseEntity<ErrorResponse> handleMethodInvalidException(Exception ex, WebRequest request) {
        return handleException(Collections.singleton(ex.getMessage()), "0", HttpStatus.BAD_REQUEST);
    }

    /**
     * This exception occurs if user is not allowed to perform requester action
     *
     * @param ex      Exception
     * @param request Request
     * @return Error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorResponse> handleAclException(Exception ex, WebRequest request) {
        return handleException(Collections.singleton("Access denied"), "0", HttpStatus.FORBIDDEN);
    }

    /**
     * This exception occurs if http method is not supported
     *
     * @param ex      Exception
     * @param request Request
     * @return Error response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public final ResponseEntity<ErrorResponse> handleMethodNotAllowedException(Exception ex, WebRequest request) {
        return handleException(Collections.singleton("Method not allowed"), "0", HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle other exceptions
     *
     * @param ex       Exception
     * @param request  Request
     * @param response Response
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        log.error(ex.toString());
        ex.printStackTrace();
        return handleException(Collections.singleton("Internal server error"), "0", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<ErrorResponse> handleException(Collection<String> message, String code, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(message, code), new HttpHeaders(), status);
    }
}
