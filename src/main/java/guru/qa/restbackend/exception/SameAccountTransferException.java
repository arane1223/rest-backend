package guru.qa.restbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SameAccountTransferException extends RuntimeException {
    public SameAccountTransferException() {
        super("Нельзя перевести деньги на тот же счет");
    }
}