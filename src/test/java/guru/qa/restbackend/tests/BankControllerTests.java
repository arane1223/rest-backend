package guru.qa.restbackend.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static guru.qa.restbackend.specs.BaseSpecs.*;
import static guru.qa.restbackend.data.TestData.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты на проверку управления пользователями")
public class BankControllerTests extends TestBase {

    @Test
    @DisplayName("Успешное получение списка всех пользователей ")
    void bankControllerAllUsersTest() {
        Response response = given(baseReqSpec)
                .get("user/all")
                .then()
                .spec(baseRespSpec(200))
                .extract()
                .response();

        assertThat(response.path("userName").toString()).contains(USERS);
    }

    @Test
    @DisplayName("Успешная авторизация")
    void successfulBankControllerAuthTest() {
        Response response = given(baseReqSpec)
                .body(CORRECT_AUTH_DATA)
                .post("user/login")
                .then()
                .spec(baseRespSpec(200))
                .extract()
                .response();

        assertThat(response.path("userName").toString()).isEqualTo(CORRECT_AUTH_DATA.getUserName());
    }

    @Test
    @DisplayName("Неуспешная авторизация")
    void unsuccessfulBankControllerAuthTest() {
        Response response = given(baseReqSpec)
                .body(INCORRECT_AUTH_DATA)
                .post("user/login")
                .then()
                .spec(baseRespSpec(401))
                .extract()
                .response();

        assertThat(response.path("status").toString()).isEqualTo("401");
        assertThat(response.path("error").toString()).isEqualTo("Unauthorized");
    }
}
