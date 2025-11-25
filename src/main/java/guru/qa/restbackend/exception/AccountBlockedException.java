package guru.qa.restbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(Long accountId) {
        super("Счет " + accountId + " заблокирован");
    }
}
