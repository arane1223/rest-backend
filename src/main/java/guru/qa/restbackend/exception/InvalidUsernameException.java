package guru.qa.restbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class InvalidUsernameException extends RuntimeException {

    public InvalidUsernameException() {
        super("Неверное имя пользователя или пароль");
    }

    public InvalidUsernameException(String username) {
        super("Пользователь '" + username + "' не найден");
    }
}
