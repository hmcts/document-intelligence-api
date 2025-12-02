package uk.gov.hmcts.reform.api.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.hmcts.reform.api.errorhandling.exceptions.InvalidFileException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handle(InvalidFileException ex) {
        log.error("400, file failed validation. Details: {}", ex.getMessage());

        return new ExceptionResponse(ex.getMessage(), LocalDateTime.now(), null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleValidationExceptions(Exception ex) {
        List<ValidationError> errors = extractValidationErrors(ex);
        String message = errors.isEmpty()
            ? "Bad request"
            : "Validation failed: " + errors.stream()
                .map(err -> err.getField() + " " + err.getMessage())
                .collect(Collectors.joining("; "));
        log.error("400, request validation failed. Details: {}", message);
        return new ExceptionResponse(message, LocalDateTime.now(), errors.isEmpty() ? null : errors);
    }

    private List<ValidationError> extractValidationErrors(Exception ex) {
        List<ValidationError> errors = new ArrayList<>();
        if (ex instanceof MethodArgumentNotValidException manv) {
            manv.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(new ValidationError(error.getField(), error.getDefaultMessage()))
            );
        } else if (ex instanceof BindException be) {
            be.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(new ValidationError(error.getField(), error.getDefaultMessage()))
            );
        }
        return errors;
    }
}
