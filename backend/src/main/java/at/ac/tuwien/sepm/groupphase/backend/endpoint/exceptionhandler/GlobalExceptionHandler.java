package at.ac.tuwien.sepm.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDetailDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Register all your Java exceptions here to map them into meaningful HTTP exceptions
 * If you have special cases which are only important for specific endpoints, use ResponseStatusExceptions
 * https://www.baeldung.com/exception-handling-for-rest-with-spring#responsestatusexception
 */
@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * Use the @ExceptionHandler annotation to write handler for custom exceptions.
     */
    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * Override methods from ResponseEntityExceptionHandler to send a customized HTTP response for a know exception
     * from e.g. Spring
     */
    @SneakyThrows
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        var errorDto = new ValidationErrorDto();
        var errorDetails = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> new ValidationErrorDetailDto(err.getField(), err.getDefaultMessage()))
            .collect(Collectors.toList());
        errorDto.setErrors(errorDetails);

        var errorRestDto = new ValidationErrorRestDto("Validation errors", List.of(errorDto));
        var responseBody = objectMapper.writeValueAsString(errorRestDto);

        return new ResponseEntity<>(responseBody, headers, status);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ValidationErrorRestDto handleValidationException(ValidationException e) {
        log.warn("Terminating request processing with status 422 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
        return e.getValidationErrorRestDto();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ValidationErrorRestDto handleConflictException(ConflictException e) {
        log.warn("Terminating request processing with status 409 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
        return e.getValidationErrorRestDto();
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolationException(RuntimeException ex, WebRequest request) {
        log.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {AccessForbiddenException.class})
    protected ResponseEntity<Object> handleAccessForbiddenException(RuntimeException ex, WebRequest request) {
        log.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

}
