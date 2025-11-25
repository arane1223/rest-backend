package guru.qa.restbackend.helpers;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import static guru.qa.restbackend.specs.BaseSpecs.baseReqSpec;
import static guru.qa.restbackend.specs.BaseSpecs.baseRespSpec;
import static io.restassured.RestAssured.given;

public class TestApiHelper {

    @Step("Сделать POST запрос")
    public static Response executePost(String path, Object body, int statusCode) {
        return given(baseReqSpec)
                .body(body)
                .when()
                .post(path)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать POST запрос")
    public static Response executePost(String path, String pathParam, Object body, int statusCode) {
        return given(baseReqSpec)
                .body(body)
                .when()
                .post(path, pathParam)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать GET запрос")
    public static Response executeGet(String path, int statusCode) {
        return given(baseReqSpec)
                .get(path)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать GET запрос")
    public static Response executeGet(String path, String pathParam, int statusCode) {
        return given(baseReqSpec)
                .get(path, pathParam)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать DELETE запрос")
    public static Response executeDelete(String path, int statusCode) {
        return given(baseReqSpec)
                .delete(path)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать DELETE запрос")
    public static Response executeDelete(String path, String pathParam, int statusCode) {
        return given(baseReqSpec)
                .delete(path, pathParam)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать PUT запрос")
    public static Response executePut(String path, String pathParam, Object body, int statusCode) {
        return given(baseReqSpec)
                .body(body)
                .when()
                .put(path, pathParam)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }

    @Step("Сделать PATCH запрос")
    public static Response executePatch(String path, String pathParam, Object body, int statusCode) {
        return given(baseReqSpec)
                .body(body)
                .when()
                .patch(path, pathParam)
                .then()
                .spec(baseRespSpec(statusCode))
                .extract().response();
    }
}
