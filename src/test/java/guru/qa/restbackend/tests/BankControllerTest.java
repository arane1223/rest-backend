package guru.qa.restbackend.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static guru.qa.restbackend.tests.TestData.USERS;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BankControllerTest extends TestBase {

    @Test
    void bankControllerTest() {
        Response response = given()
                .log().all()
                .get("/user/all")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        assertEquals(response.path("userName"), USERS);
    }
}
