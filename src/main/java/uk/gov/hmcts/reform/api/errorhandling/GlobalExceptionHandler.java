package uk.gov.hmcts.reform.api.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.hmcts.reform.api.errorhandling.exceptions.InvalidFileException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handle(InvalidFileException ex) {
        log.error("400, file failed validation. Details: {}", ex.getMessage());

        return new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
    }
}
