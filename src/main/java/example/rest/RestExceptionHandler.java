package example.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(InvalidStateStoreException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ErrorResponse handleStateStoreException(final Exception exception) {
        if (StringUtils.contains(exception.getMessage(), "not RUNNING")) {
            log.error(exception.getMessage(), exception);
            return ErrorResponse.builder()
                    .code(ErrorResponse.SALES_STATE_STORE_NOT_READY)
                    .message("Sales state data store is still starting. Retry your request in a moment.")
                    .build();
        }
        return handleException(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleRequestValidationError(final Exception exception) {
        log.error(exception.getMessage(), exception);
        return ErrorResponse.builder()
                .code(ErrorResponse.INVALID_ORDER_REQUEST)
                .message("Order request is invalid.")
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleException(final Exception exception) {
        log.error(exception.getMessage(), exception);
        return ErrorResponse.builder()
                .code(ErrorResponse.INTERNAL_ERROR)
                .message("Internal error occurred. Check application log for details.")
                .build();
    }

}
