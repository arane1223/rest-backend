package guru.qa.restbackend.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static guru.qa.restbackend.helpers.TestApiHelper.*;
import static guru.qa.restbackend.data.TestData.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Owner("sergeyglukhov")
@Feature("User Management")
@DisplayName("Тесты на проверку управления пользователями")
public class BankControllerTests extends TestBase {

    @Test
    @Story("Получение списка пользователей")
    @DisplayName("Успешное получение списка всех пользователей ")
    void bankControllerAllUsersTest() {
        Response response = step("Отправить запрос на получение списка пользователей", () ->
                executeGet("user/all", 200));

        step("Проверить список пользователей в ответе с тестовым", () ->
                assertThat(response.path("userName").toString()).contains(USERS));
    }

    @Test
    @Story("Авторизация")
    @DisplayName("Успешная авторизация")
    void successfulBankControllerAuthTest() {
        Response response = step("Отправить запрос на авторизацию", () ->
                executePost("user/login", CORRECT_AUTH_DATA, 200));

        step("Проверить что авторизация успешна", () ->
        assertThat(response.path("userName").toString()).isEqualTo(CORRECT_AUTH_DATA.getUserName()));
    }

    @Test
    @Story("Авторизация")
    @DisplayName("Неуспешная авторизация")
    void unsuccessfulBankControllerAuthTest() {
        Response response = step("Отправить запрос на авторизацию", () ->
                executePost("user/login", INCORRECT_AUTH_DATA, 401));

        step("Проверить что авторизация неуспешна", () -> {
            assertThat(response.path("status").toString()).isEqualTo("401");
            assertThat(response.path("error").toString()).isEqualTo("Unauthorized");
        });
    }
}
