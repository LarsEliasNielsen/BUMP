package cloud.larn.bump.api

import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.format.DateTimeParseException

@RestControllerAdvice
class GlobalExceptionHandler {

    @Suppress("unused")
    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentNotValidException::class, DateTimeParseException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidRequest(): ErrorResponse =
        ErrorResponse(error = "Request is not valid")
}
