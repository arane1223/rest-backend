package guru.qa.restbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AccountAlreadyClosedException extends RuntimeException {

    public AccountAlreadyClosedException(Long accountId) {
        super("Счет " + accountId + " уже закрыт");
    }
}
