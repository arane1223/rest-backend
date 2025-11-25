package guru.qa.restbackend.tests;

import guru.qa.restbackend.domain.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static guru.qa.restbackend.data.TestData.*;
import static guru.qa.restbackend.domain.AccountStatus.*;
import static guru.qa.restbackend.domain.TransactionStatus.SUCCESS;
import static guru.qa.restbackend.domain.TransactionType.DEPOSIT;
import static guru.qa.restbackend.helpers.ResponseHelpers.*;
import static guru.qa.restbackend.specs.BaseSpecs.*;
import static guru.qa.restbackend.utils.RandomUtils.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты на проверку управления банковскими счетами ")
public class AccountControllerTests extends TestBase {

    //Позитивные сценарии:
    //Создание счета
    @Test
    @DisplayName("Успешное создание нового счета")
    void successfulCreateAccountTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();

        Response response = given(baseReqSpec)
                .body(newAccountDataForTest)
                .when()
                .post("/account/create")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(getBalanceFromResponse(response)).isEqualByComparingTo("0");
        assertThat(getAccountStatusFromResponse(response)).isEqualTo(ACTIVE);
        assertThat(response.jsonPath().getString("currency")).isEqualTo(newAccountDataForTest.getCurrency());
        assertThat(response.jsonPath().getString("ownerName")).isEqualTo(newAccountDataForTest.getOwnerName());
    }

    //Получение счета (GET /account/{id})
    static Stream<Arguments> successfulGetAccountParameterizedTest() {
        return Stream.of(
                Arguments.of("1", FIRST_USER_DATA),
                Arguments.of("2", SECOND_USER_DATA),
                Arguments.of("3", THIRD_USER_DATA),
                Arguments.of("4", FOURTH_USER_DATA)
        );
    }

    @MethodSource
    @DisplayName("Успешное получение счета по ID")
    @ParameterizedTest(name = "Получение счета для ID {0}")
    void successfulGetAccountParameterizedTest(String id, Account account) {
        Response response = given(baseReqSpec)
                .get("/account/" + id)
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(response.jsonPath().getLong("id")).isEqualTo(account.getId());
        assertThat(response.jsonPath().getString("accountNumber")).isEqualTo(account.getAccountNumber());
        assertThat(getBalanceFromResponse(response)).isEqualByComparingTo(account.getBalance());
        assertThat(response.jsonPath().getString("currency")).isEqualTo(account.getCurrency());
        assertThat(getAccountStatusFromResponse(response)).isEqualTo(account.getStatus());
        assertThat(getCreatedDateFromResponse(response)).isEqualTo(account.getCreatedAt().toLocalDate());
        assertThat(response.jsonPath().getString("ownerName")).isEqualTo(account.getOwnerName());
    }

    //Получение всех счетов
    @Test
    @DisplayName("Успешное получение всех счетов")
    void successfulGetAllAccountsTest() {
        Response response = given(baseReqSpec)
                .get("/account/all")
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(response.jsonPath().getList("id").size()).isGreaterThan(0);
    }

    //Получение баланса
    static Stream<Arguments> successfulGetBalanceByIdParameterizedTest() {
        return Stream.of(
                Arguments.of("1", FIRST_USER_DATA),
                Arguments.of("2", SECOND_USER_DATA),
                Arguments.of("3", THIRD_USER_DATA),
                Arguments.of("4", FOURTH_USER_DATA)
        );
    }

    @MethodSource
    @DisplayName("Успешное получение баланса по ID")
    @ParameterizedTest(name = "Получение счета для ID {0}")
    void successfulGetBalanceByIdParameterizedTest(String id, Account account) {
        String response = given(baseReqSpec)
                .get("/account/{id}/balance", id)
                .then()
                .spec(baseRespSpec(200))
                .extract().response().asString();

        assertThat(response).isEqualTo(account.getBalance().toString());
    }

    //Удаление счета
    @Test
    @DisplayName("Успешное удаление тестового счета по ID5")
    void successfulTestAccountDelete() {
        Response firstResponse = given(baseReqSpec)
                .get("/account/5")
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(getBalanceFromResponse(firstResponse)).isEqualByComparingTo("0");

        given(baseReqSpec)
                .delete("/account/5")
                .then()
                .spec(baseRespSpec(204))
                .extract().response();

        Response afterDeleteResponse = given(baseReqSpec)
                .get("/account/5")
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(getAccountStatusFromResponse(afterDeleteResponse)).isEqualTo(CLOSED);
    }

    //Пополнение счета
    @Test
    @DisplayName("Успешное пополнение счета")
    void successfulAddingFundsToAccountTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        TransactionRequest addFundsRequest = new TransactionRequest(
                new BigDecimal(getRandomAmount()), "Add funds");

        Response addAccountResponse = given(baseReqSpec)
                .body(newAccountDataForTest)
                .when()
                .post("/account/create")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(getBalanceFromResponse(addAccountResponse)).isEqualByComparingTo("0");
        assertThat(getAccountStatusFromResponse(addAccountResponse)).isEqualTo(ACTIVE);

        String userId = getAccountId(addAccountResponse);

        Response addFundsResponse = given(baseReqSpec)
                .body(addFundsRequest)
                .when()
                .post("/account/{id}/deposit", userId)
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(getTransactionTypeFromResponse(addFundsResponse)).isEqualTo(DEPOSIT);
        assertThat(getAmountFromResponse(addFundsResponse))
                .isEqualByComparingTo(addFundsRequest.getAmount());
        assertThat(addFundsResponse.path("toAccountId").toString()).isEqualTo(userId);
        assertThat(getTransactionStatusFromResponse(addFundsResponse)).isEqualTo(SUCCESS);
    }

    //Снятие денег
    @Test
    @DisplayName("Успешное снятие денег при положительном балансе")
    void successfulWithdrawingMoneyTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        TransactionRequest addFundsRequest = new TransactionRequest(
                new BigDecimal(getRandomAmount()), "Add funds");
        TransactionRequest withdrawFundsRequest = new TransactionRequest(
                new BigDecimal(addFundsRequest.getAmount().intValue() - 500), "Withdraw funds");

        Response addAccountResponse = given(baseReqSpec)
                .body(newAccountDataForTest)
                .when()
                .post("/account/create")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        String testAccountId = getAccountId(addAccountResponse);

        Response addFundsResponse = given(baseReqSpec)
                .body(addFundsRequest)
                .when()
                .post("/account/{id}/deposit", testAccountId)
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(getAmountFromResponse(addFundsResponse))
                .isEqualByComparingTo(addFundsRequest.getAmount());

        given(baseReqSpec)
                .body(withdrawFundsRequest)
                .when()
                .post("/account/{id}/withdraw", testAccountId)
                .then()
                .spec(baseRespSpec(201));

        Response accountResponse = given(baseReqSpec)
                .get("/account/{id}", testAccountId)
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(getBalanceFromResponse(accountResponse).intValue()).isEqualTo(500);
        assertThat(getAccountStatusFromResponse(accountResponse)).isEqualTo(ACTIVE);
    }

    //Перевод между счетами
    @Test
    @DisplayName("Успешный перевод между счетами ID6 и ID7")
    void successfulTransferBetweenAccountsTest() {
        String accountBalanceBeforeTransfer = given(baseReqSpec)
                .get("/account/7/balance")
                .then()
                .spec(baseRespSpec(200))
                .extract().response().asString();

        assertThat(accountBalanceBeforeTransfer).isEqualTo("500.00");

        Response transferResponse = given(baseReqSpec)
                .body(SUCCESS_TEST_TRANSFER_REQUEST_DATA)
                .when()
                .post("/account/transfer")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(getTransactionStatusFromResponse(transferResponse))
                .isEqualTo(SUCCESS);

        String accountBalanceAfterTransfer = given(baseReqSpec)
                .get("/account/7/balance")
                .then()
                .spec(baseRespSpec(200))
                .extract().response().asString();

        assertThat(accountBalanceAfterTransfer).isEqualTo("1500.00");
    }

    //История транзакций
    @Test
    @DisplayName("Успешное получение истории транзакций по ID1")
    void successfulGettingAccountTransactionsByIdTest() {
        Response response = given(baseReqSpec)
                .get("/account/1/transactions")
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(response.jsonPath().getList("id").size()).isGreaterThanOrEqualTo(2);
    }

    //Изменение статуса счета
    @Test
    @DisplayName("Успешное изменение статуса счета")
    void successfulChangingAccountStatusTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        UpdateAccountStatusRequest newAccountStatus = new UpdateAccountStatusRequest(CLOSED);

        Response addAccountResponse = given(baseReqSpec)
                .body(newAccountDataForTest)
                .when()
                .post("/account/create")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        String testAccountId = getAccountId(addAccountResponse);

        Response changeAccountStatusResponse = given(baseReqSpec)
                .body(newAccountStatus)
                .when()
                .put("/account/{id}/status", testAccountId)
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(getAccountStatusFromResponse(changeAccountStatusResponse))
                .isEqualTo(CLOSED);
    }

    //Изменение владельца счета
    @Test
    @DisplayName("Успешное изменение владельца счета счета")
    void successfulChangingAccountOwnerTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        UpdateAccountOwnerRequest newOwnerForTest = new UpdateAccountOwnerRequest(getRandomOwnerName());

        Response addAccountResponse = given(baseReqSpec)
                .body(newAccountDataForTest)
                .when()
                .post("/account/create")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(addAccountResponse.path("ownerName").toString())
                .isEqualTo(newAccountDataForTest.getOwnerName());

        String testAccountId = getAccountId(addAccountResponse);

        Response changeOwnerResponse = given(baseReqSpec)
                .body(newOwnerForTest)
                .when()
                .patch("/account/{id}/owner", testAccountId)
                .then()
                .spec(baseRespSpec(200))
                .extract().response();

        assertThat(changeOwnerResponse.path("ownerName").toString())
                .isEqualTo(newOwnerForTest.getOwnerName());
    }

    //Негативные сценарии:
    //404 - счет не найден
    @Test
    @DisplayName("Неуспешное получение счета по ID, 404 - Not Found")
    void successfulGetAccountTest() {
        String randomId = getRandomId();

        Response response = given(baseReqSpec)
                .get("/account/" + randomId)
                .then()
                .spec(baseRespSpec(404))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
        assertThat(response.path("error").toString()).isEqualTo("Not Found");
        assertThat(response.path("message").toString()).isEqualTo("Счет с ID %s не найден", randomId);
    }

    //404 - счет не найден при запросе баланса
    @Test
    @DisplayName("Неуспешное получение баланса по несуществующему ID, 404 - Not Found")
    void unsuccessfulGetBalanceByIdTest() {
        String randomId = getRandomId();
        Response response = given(baseReqSpec)
                .get("/account/{id}/balance", randomId)
                .then()
                .spec(baseRespSpec(404))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
        assertThat(response.path("error").toString()).isEqualTo("Not Found");
        assertThat(response.path("message").toString()).isEqualTo("Счет с ID %s не найден", randomId);
    }

    //400 - недостаточно средств
    @Test
    @DisplayName("Неуспешное снятие денег, 400 - Bad Request")
    void unsuccessfulWithdrawingMoneyTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        TransactionRequest withdrawFundsRequest = new TransactionRequest(
                new BigDecimal(getRandomAmount()), "Some description");

        Response addAccountResponse = given(baseReqSpec)
                .body(newAccountDataForTest)
                .when()
                .post("/account/create")
                .then()
                .spec(baseRespSpec(201))
                .extract().response();

        assertThat(addAccountResponse.jsonPath().getString("ownerName"))
                .isEqualTo(newAccountDataForTest.getOwnerName());

        String testAccountId = addAccountResponse.jsonPath().getString("id");

        Response withdrawResponse = given(baseReqSpec)
                .body(withdrawFundsRequest)
                .when()
                .post("/account/{id}/withdraw", testAccountId)
                .then()
                .spec(baseRespSpec(400))
                .extract().response();

        assertThat(getStatusCodeFromResponse(withdrawResponse)).isEqualTo(400);
        assertThat(withdrawResponse.path("error").toString()).isEqualTo("Bad Request");
        assertThat(withdrawResponse.path("message").toString())
                .isEqualTo("Недостаточно средств на счете %s", testAccountId);

    }

    //403 - счет заблокирован
    @Test
    @DisplayName("Неуспешное снятие денег с заблокированного счета, 403 - Forbidden")
    void unsuccessfulWithdrawingMoneyOnBlockedAccountTest() {
        TransactionRequest withdrawFundsRequest = new TransactionRequest(
                new BigDecimal(getRandomAmount()), "Some description");

        Response response = given(baseReqSpec)
                .body(withdrawFundsRequest)
                .when()
                .post("/account/4/withdraw")
                .then()
                .spec(baseRespSpec(403))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(403);
        assertThat(response.path("error").toString()).isEqualTo("Forbidden");
        assertThat(response.path("message").toString()).contains("Счет", "заблокирован");
    }

    //400 - перевод на тот же счет
    @Test
    @DisplayName("Неуспешный перевод на тот же счет, 400 - Bad Request")
    void unsuccessfulTransferOnSameAccountTest() {
        Response response = given(baseReqSpec)
                .body(TEST_TRANSFER_ON_SAME_ACCOUNT_REQUEST_DATA)
                .when()
                .post("/account/transfer")
                .then()
                .spec(baseRespSpec(400))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(400);
        assertThat(response.path("error").toString()).isEqualTo("Bad Request");
        assertThat(response.path("message").toString())
                .isEqualTo("Нельзя перевести деньги на тот же счет");
    }

    //400 - удаление счета с ненулевым балансом
    @Test
    @DisplayName("Неуспешное удаление счета с ненулевым балансом, 400 - Bad Request")
    void unsuccessfulDeleteAccountTest() {
        Response response = given(baseReqSpec)
                .delete("/account/1")
                .then()
                .spec(baseRespSpec(400))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(400);
        assertThat(response.path("error").toString()).isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("Неуспешное удаление несуществующего счета, 404 - Not Found")
    void unsuccessfulDeleteNonExistentAccountTest() {
        String randomId = getRandomId();

        Response response = given(baseReqSpec)
                .delete("/account/" + randomId)
                .then()
                .spec(baseRespSpec(404))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
        assertThat(response.path("error").toString()).isEqualTo("Not Found");
        assertThat(response.path("message").toString()).isEqualTo("Счет с ID %s не найден", randomId);
    }

    //404 - История транзакций не найдена
    @Test
    @DisplayName("Неуспешное получение истории транзакций по ID, 404 - Not Found")
    void unsuccessfulGettingAccountTransactionsByIdTest() {
        String randomId = getRandomId();

        Response response = given(baseReqSpec)
                .get("/account/{id}/transactions", randomId)
                .then()
                .spec(baseRespSpec(404))
                .extract().response();

        assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
        assertThat(response.path("error").toString()).isEqualTo("Not Found");
        assertThat(response.path("message").toString()).isEqualTo("Счет с ID %s не найден", randomId);
    }
}