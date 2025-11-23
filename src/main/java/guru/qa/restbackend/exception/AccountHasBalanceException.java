package guru.qa.restbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AccountHasBalanceException extends RuntimeException {

    public AccountHasBalanceException(Long accountId) {
        super("Невозможно удалить счет " + accountId + ". Сначала обнулите баланс.");
    }
}
