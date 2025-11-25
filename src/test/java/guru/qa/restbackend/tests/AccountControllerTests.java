package guru.qa.restbackend.tests;

import guru.qa.restbackend.domain.*;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
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
import static guru.qa.restbackend.helpers.TestApiHelper.*;
import static guru.qa.restbackend.utils.RandomUtils.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Owner("sergeyglukhov")
@Feature("Account Management")
@DisplayName("Тесты на проверку управления банковскими счетами ")
public class AccountControllerTests extends TestBase {

    @Test
    @Story("Создание счета")
    @DisplayName("Успешное создание нового счета")
    void successfulCreateAccountTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();

        Response response = step("Отправить запрос на создание нового счета", () ->
                executePost("/account/create", newAccountDataForTest, 201));

        step("Проверить баланс, статус, валюту и имя в ответе", () -> {
            assertThat(getBalanceFromResponse(response)).isEqualByComparingTo("0");
            assertThat(getAccountStatusFromResponse(response)).isEqualTo(ACTIVE);
            assertThat(response.jsonPath().getString("currency")).isEqualTo(newAccountDataForTest.getCurrency());
            assertThat(response.jsonPath().getString("ownerName")).isEqualTo(newAccountDataForTest.getOwnerName());
        });
    }

    static Stream<Arguments> successfulGetAccountParameterizedTest() {
        return Stream.of(
                Arguments.of("1", FIRST_USER_DATA),
                Arguments.of("2", SECOND_USER_DATA),
                Arguments.of("3", THIRD_USER_DATA),
                Arguments.of("4", FOURTH_USER_DATA)
        );
    }

    @MethodSource
    @Story("Получение счета")
    @DisplayName("Успешное получение счета по ID")
    @ParameterizedTest(name = "Получение счета для ID {0}")
    void successfulGetAccountParameterizedTest(String id, Account account) {
        Response response = step("Отправить запрос на получение счета по ID", () ->
                executeGet("/account/{id}", id, 200));

        step("Проверить все строчки в ответе", () -> {
            assertThat(getAccountIdAsLong(response)).isEqualTo(account.getId());
            assertThat(response.jsonPath().getString("accountNumber")).isEqualTo(account.getAccountNumber());
            assertThat(getBalanceFromResponse(response)).isEqualByComparingTo(account.getBalance());
            assertThat(response.jsonPath().getString("currency")).isEqualTo(account.getCurrency());
            assertThat(getAccountStatusFromResponse(response)).isEqualTo(account.getStatus());
            assertThat(getCreatedDateFromResponse(response)).isEqualTo(account.getCreatedAt().toLocalDate());
            assertThat(response.jsonPath().getString("ownerName")).isEqualTo(account.getOwnerName());
        });
    }

    @Test
    @Story("Получение счета")
    @DisplayName("Успешное получение всех счетов")
    void successfulGetAllAccountsTest() {
        Response response = step("Отправить запрос на получение всех счетов по ID", () ->
                executeGet("/account/all", 200));

        step("Проверить, что в базе есть счета по ID", () ->
                assertThat(response.jsonPath().getList("id").size()).isGreaterThan(0));
    }

    static Stream<Arguments> successfulGetBalanceByIdParameterizedTest() {
        return Stream.of(
                Arguments.of("1", FIRST_USER_DATA),
                Arguments.of("2", SECOND_USER_DATA),
                Arguments.of("3", THIRD_USER_DATA),
                Arguments.of("4", FOURTH_USER_DATA)
        );
    }

    @MethodSource
    @Story("Получение баланса")
    @DisplayName("Успешное получение баланса по ID")
    @ParameterizedTest(name = "Получение баланса для счета с ID {0}")
    void successfulGetBalanceByIdParameterizedTest(String id, Account account) {
        String response = step("Отправить запрос на получение баланса", () ->
                executeGet("/account/{id}/balance", id, 200).asString());

        step("Проверить сумму баланса", () ->
                assertThat(response).isEqualTo(account.getBalance().toString()));
    }

    @Test
    @Story("Удаление счета")
    @DisplayName("Успешное удаление тестового счета по ID5")
    void successfulTestAccountDelete() {
        Response firstResponse = step("Отправить запрос на получение счета по ID5", () ->
                executeGet("/account/5", 200));

        step("Проверить, что на счете баланс равен 0", () ->
                assertThat(getBalanceFromResponse(firstResponse)).isEqualByComparingTo("0"));

        step("Отправить запрос на удаление счета", () ->
                executeDelete("/account/5", 204));

        Response afterDeleteResponse = step("Отправить запрос на получение счета после удаления", () ->
                executeGet("/account/5", 200));

        step("Проверить, что статус счета CLOSED", () ->
                assertThat(getAccountStatusFromResponse(afterDeleteResponse)).isEqualTo(CLOSED));
    }

    @Test
    @Story("Пополнение счета")
    @DisplayName("Успешное пополнение счета")
    void successfulAddingFundsToAccountTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        TransactionRequest addFundsRequest = step("Подготовить тело запроса на пополнение счета", () ->
                new TransactionRequest(new BigDecimal(getRandomAmount()), "Add funds"));

        Response addAccountResponse = step("Сделать запрос на создание счета", () ->
                executePost("/account/create", newAccountDataForTest, 201));

        step("Проверить, что счет активен и баланс на нем 0", () -> {
            assertThat(getBalanceFromResponse(addAccountResponse)).isEqualByComparingTo("0");
            assertThat(getAccountStatusFromResponse(addAccountResponse)).isEqualTo(ACTIVE);
        });

        String userId = step("Записать ID нового счета", () ->
                getAccountId(addAccountResponse));

        Response addFundsResponse = step("Сделать запрос на пополнение счета", () ->
                executePost("/account/{id}/deposit", userId, addFundsRequest, 201));

        step("Проверить, что тип операции DEPOSIT, статус SUCCESS, сумму пополнения и ID верные", () -> {
            assertThat(getTransactionTypeFromResponse(addFundsResponse)).isEqualTo(DEPOSIT);
            assertThat(getAmountFromResponse(addFundsResponse)).isEqualByComparingTo(addFundsRequest.getAmount());
            assertThat(addFundsResponse.path("toAccountId").toString()).isEqualTo(userId);
            assertThat(getTransactionStatusFromResponse(addFundsResponse)).isEqualTo(SUCCESS);
        });
    }

    @Test
    @Story("Снятие денег со счета")
    @DisplayName("Успешное снятие денег при положительном балансе")
    void successfulWithdrawingMoneyTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        TransactionRequest addFundsRequest = step("Подготовить тело запроса на пополнение счета", () ->
                new TransactionRequest(new BigDecimal(getRandomAmount()), "Add funds"));
        TransactionRequest withdrawFundsRequest = step("Подготовить тело запроса на снятие денег со счета", () ->
                new TransactionRequest(
                        new BigDecimal(addFundsRequest.getAmount().intValue() - 500), "Withdraw funds"));

        Response addAccountResponse = step("Отправить запрос на создание нового счета", () ->
                executePost("/account/create", newAccountDataForTest, 201));

        String testAccountId = step("Записать ID нового счета", () ->
                getAccountId(addAccountResponse));

        Response addFundsResponse = step("Отправить запрос на пополнение счета", () ->
                executePost("/account/{id}/deposit", testAccountId, addFundsRequest, 201));

        step("Проверить, что счет пополнился", () ->
                assertThat(getAmountFromResponse(addFundsResponse))
                        .isEqualByComparingTo(addFundsRequest.getAmount()));

        step("Отправить запрос на снятие денег со счета", () ->
                executePost("/account/{id}/withdraw", testAccountId, withdrawFundsRequest, 201));

        Response accountResponse = step("Отправить запрос на получение счета", () ->
                executeGet("/account/{id}", testAccountId, 200));

        step("Проверить, что баланс верный и статус счета ACTIVE", () -> {
            assertThat(getBalanceFromResponse(accountResponse).intValue()).isEqualTo(500);
            assertThat(getAccountStatusFromResponse(accountResponse)).isEqualTo(ACTIVE);
        });
    }

    @Test
    @Story("Переводы")
    @DisplayName("Успешный перевод между счетами ID6 и ID7")
    void successfulTransferBetweenAccountsTest() {
        String accountBalanceBeforeTransfer = step("Отправить запрос на получение баланса до перевода", () ->
                executeGet("/account/7/balance", 200).asString());

        step("Проверить сумму баланса до перевода", () ->
                assertThat(accountBalanceBeforeTransfer).isEqualTo("500.00"));

        Response transferResponse = step("Отправить запрос на перевод", () ->
                executePost("/account/transfer", SUCCESS_TEST_TRANSFER_REQUEST_DATA, 201));

        step("Проверить, что статус перевода SUCCESS", () ->
                assertThat(getTransactionStatusFromResponse(transferResponse)).isEqualTo(SUCCESS));

        String accountBalanceAfterTransfer = step("Отправить запрос на получение баланса после перевода", () ->
                executeGet("/account/7/balance", 200).asString());

        step("Проверить сумму баланса после перевода", () ->
                assertThat(accountBalanceAfterTransfer).isEqualTo("1500.00"));
    }

    @Test
    @Story("Получение истории транзакций")
    @DisplayName("Успешное получение истории транзакций по ID1")
    void successfulGettingAccountTransactionsByIdTest() {
        Response response = step("Отправить запрос на получение истории транзакций", () ->
                executeGet("/account/1/transactions", 200));

        step("Проверить, что в ответе есть список ID ≥ 2", () ->
                assertThat(response.jsonPath().getList("id").size()).isGreaterThanOrEqualTo(2));
    }

    @Test
    @Story("Изменение статуса счета")
    @DisplayName("Успешное изменение статуса счета")
    void successfulChangingAccountStatusTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        UpdateAccountStatusRequest newAccountStatus = step("Подготовить тело запроса на закрытие счета", () ->
                new UpdateAccountStatusRequest(CLOSED));

        Response addAccountResponse = step("Отправить запрос на создание нового счета", () ->
                executePost("/account/create", newAccountDataForTest, 201));

        String testAccountId = step("Записать ID нового счета", () ->
                getAccountId(addAccountResponse));

        Response changeAccountStatusResponse = step("Отправить запрос на закрытие счета", () ->
                executePut("/account/{id}/status", testAccountId, newAccountStatus, 200));

        step("Проверить, что статус счета CLOSED", () ->
                assertThat(getAccountStatusFromResponse(changeAccountStatusResponse)).isEqualTo(CLOSED));
    }

    @Test
    @Story("Изменение владельца счета")
    @DisplayName("Успешное изменение владельца счета счета")
    void successfulChangingAccountOwnerTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        UpdateAccountOwnerRequest newOwnerForTest = step("Подготовить тело запроса на изменение владельца счета", () ->
                new UpdateAccountOwnerRequest(getRandomOwnerName()));

        Response addAccountResponse = step("Отправить запрос на создание нового счета", () ->
                executePost("/account/create", newAccountDataForTest, 201));

        String testAccountId = step("Записать ID нового счета", () ->
                getAccountId(addAccountResponse));

        step("Проверить имя владельца счета до изменения", () ->
                assertThat(addAccountResponse.path("ownerName").toString())
                        .isEqualTo(newAccountDataForTest.getOwnerName()));

        Response changeOwnerResponse = step("Отправить запрос на изменение владельца счета", () ->
                executePatch("/account/{id}/owner", testAccountId, newOwnerForTest, 200));

        step("Проверить имя владельца счета изменилось на новое", () ->
                assertThat(changeOwnerResponse.path("ownerName").toString())
                        .isEqualTo(newOwnerForTest.getOwnerName()));
    }

    @Test
    @Story("Получение счета")
    @DisplayName("Неуспешное получение счета по ID, 404 - Not Found")
    void successfulGetAccountTest() {
        String randomId = getRandomId();

        Response response = step("Отправить запрос на получение счета со случайным ID", () ->
                executeGet("/account/{id}", randomId, 404));

        step("Проверить ответ", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
            assertThat(response.path("error").toString()).isEqualTo("Not Found");
            assertThat(response.path("message").toString())
                    .isEqualTo("Счет с ID %s не найден", randomId);
        });
    }

    @Test
    @Story("Получение баланса")
    @DisplayName("Неуспешное получение баланса по несуществующему ID, 404 - Not Found")
    void unsuccessfulGetBalanceByIdTest() {
        String randomId = getRandomId();

        Response response = step("Отправить запрос на получение баланса со случайным ID", () ->
                executeGet("/account/{id}/balance", randomId, 404));

        step("Проверить ответ", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
            assertThat(response.path("error").toString()).isEqualTo("Not Found");
            assertThat(response.path("message").toString())
                    .isEqualTo("Счет с ID %s не найден", randomId);
        });
    }

    @Test
    @Story("Снятие денег со счета")
    @DisplayName("Неуспешное снятие денег, с нулевым балансом, 400 - Bad Request")
    void unsuccessfulWithdrawingMoneyTest() {
        CreateAccountRequest newAccountDataForTest = generateNewAccountData();
        TransactionRequest withdrawFundsRequest = step("Подготовить тело запроса на снятие денег", () ->
                new TransactionRequest(new BigDecimal(getRandomAmount()), "Some description"));

        Response addAccountResponse = step("Отправить запрос на создание нового счета", () ->
                executePost("/account/create", newAccountDataForTest, 201));

        step("Проверить, что на балансе 0", () ->
                assertThat(getBalanceFromResponse(addAccountResponse)).isEqualByComparingTo("0"));

        String testAccountId = step("Записать ID нового счета", () ->
                addAccountResponse.jsonPath().getString("id"));

        Response withdrawResponse = step("Отправить запрос на снятие денег", () ->
                executePost("/account/{id}/withdraw", testAccountId, withdrawFundsRequest, 400));

        step("Проверить ответ, что на счете недостаточно средств", () -> {
            assertThat(getStatusCodeFromResponse(withdrawResponse)).isEqualTo(400);
            assertThat(withdrawResponse.path("error").toString()).isEqualTo("Bad Request");
            assertThat(withdrawResponse.path("message").toString())
                    .isEqualTo("Недостаточно средств на счете %s", testAccountId);
        });
    }

    @Test
    @Story("Снятие денег со счета")
    @DisplayName("Неуспешное снятие денег с заблокированного счета, 403 - Forbidden")
    void unsuccessfulWithdrawingMoneyOnBlockedAccountTest() {
        TransactionRequest withdrawFundsRequest = step("Подготовить тело запроса на снятие денег", () ->
                new TransactionRequest(new BigDecimal(getRandomAmount()), "Some description"));

        Response response = step("Отправить запрос на снятие денег", () ->
                executePost("/account/4/withdraw", withdrawFundsRequest, 403));

        step("Проверить ответ, что счет заблокирован", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(403);
            assertThat(response.path("error").toString()).isEqualTo("Forbidden");
            assertThat(response.path("message").toString()).contains("Счет", "заблокирован");
        });
    }

    @Test
    @Story("Переводы")
    @DisplayName("Неуспешный перевод на тот же счет, 400 - Bad Request")
    void unsuccessfulTransferOnSameAccountTest() {
        Response response = step("Отправить запрос на перевод денег на тот же счет", () ->
                executePost("/account/transfer", TEST_TRANSFER_ON_SAME_ACCOUNT_REQUEST_DATA, 400));

        step("Проверить ответ, что нельзя перевести деньги на тот же счет", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(400);
            assertThat(response.path("error").toString()).isEqualTo("Bad Request");
            assertThat(response.path("message").toString())
                    .isEqualTo("Нельзя перевести деньги на тот же счет");
        });
    }

    @Test
    @Story("Удаление счета")
    @DisplayName("Неуспешное удаление счета ID1 с ненулевым балансом, 400 - Bad Request")
    void unsuccessfulDeleteAccountTest() {
        Response response = step("Отправить запрос на удаление счета с ID1", () ->
                executeDelete("/account/1", 400));

        step("Проверить ответ, что невозможно удалить счет", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(400);
            assertThat(response.path("error").toString()).isEqualTo("Bad Request");
            assertThat(response.path("message").toString()).contains("Невозможно удалить счет",
                    "Сначала обнулите баланс");
        });
    }

    @Test
    @Story("Удаление счета")
    @DisplayName("Неуспешное удаление несуществующего счета, 404 - Not Found")
    void unsuccessfulDeleteNonExistentAccountTest() {
        String randomId = getRandomId();

        Response response = step("Отправить запрос на удаление счета со случайны несуществующим ID", () ->
                executeDelete("/account/{id}", randomId, 404));

        step("Проверить ответ, что счет не найден", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
            assertThat(response.path("error").toString()).isEqualTo("Not Found");
            assertThat(response.path("message").toString())
                    .isEqualTo("Счет с ID %s не найден", randomId);
        });
    }

    @Test
    @Story("Получение истории транзакций")
    @DisplayName("Неуспешное получение истории транзакций по ID, 404 - Not Found")
    void unsuccessfulGettingAccountTransactionsByIdTest() {
        String randomId = getRandomId();

        Response response = step("Отправить запрос на транзакции со случайны несуществующим ID", () ->
                executeGet("/account/{id}/transactions", randomId, 404));

        step("Проверить ответ, что счет не найден", () -> {
            assertThat(getStatusCodeFromResponse(response)).isEqualTo(404);
            assertThat(response.path("error").toString()).isEqualTo("Not Found");
            assertThat(response.path("message").toString())
                    .isEqualTo("Счет с ID %s не найден", randomId);
        });
    }
}