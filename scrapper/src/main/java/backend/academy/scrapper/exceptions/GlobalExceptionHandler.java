package backend.academy.scrapper.exceptions;

import backend.academy.scrapper.dto.ApiErrorResponse;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(IllegalArgumentException ex) {
        return new ApiErrorResponse(
                "Некорректные параметры запроса", "400", "InvalidRequest", ex.getMessage(), getStackTrace(ex));
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(NoSuchElementException ex) {

        return new ApiErrorResponse("Ссылка не найдена", "404", "NotFound", ex.getMessage(), getStackTrace(ex));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex) {

        return new ApiErrorResponse(
                "Внутренняя ошибка сервера", "500", ex.getClass().getSimpleName(), ex.getMessage(), getStackTrace(ex));
    }

    private List<String> getStackTrace(Exception ex) {
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        return stackTraceList;
    }
}
