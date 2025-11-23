package guru.qa.restbackend.controller;

import guru.qa.restbackend.domain.*;
import guru.qa.restbackend.service.PaymentService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST контроллер для управления счетами и транзакциями.
 */
@RestController
@RequestMapping("/account")
@Api(tags = "Account Management", description = "Управление банковскими счетами")
public class AccountController {

    private final PaymentService paymentService;

    @Autowired
    public AccountController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Создание нового счета.
     * POST /account/create
     */
    @PostMapping("/create")
    @ApiOperation(value = "Создание счета", notes = "Создает новый банковский счет с нулевым балансом")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Счет успешно создан", response = Account.class),
            @ApiResponse(code = 400, message = "Невалидные данные запроса (отсутствует имя или валюта, неподдерживаемая валюта)", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Account> createAccount(
            @ApiParam(value = "Данные для создания счета", required = true)
            @RequestBody @Valid CreateAccountRequest request) {

        Account account = paymentService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    /**
     * Получение информации о счете по ID.
     * GET /account/{id}
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "Получить счет", notes = "Возвращает информацию о счете по его ID")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Счет успешно получен", response = Account.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Account> getAccount(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id) {

        Account account = paymentService.getAccount(id);
        return ResponseEntity.ok(account);
    }

    /**
     * Получение всех счетов.
     * GET /account/all
     */
    @GetMapping("/all")
    @ApiOperation(value = "Получить все счета", notes = "Возвращает список всех счетов в системе")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Список счетов успешно получен", response = Account.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = paymentService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Получение баланса счета.
     * GET /account/{id}/balance
     */
    @GetMapping("/{id}/balance")
    @ApiOperation(value = "Получить баланс", notes = "Возвращает текущий баланс счета")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Баланс успешно получен", response = BigDecimal.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<BigDecimal> getBalance(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id) {

        BigDecimal balance = paymentService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

    /**
     * Пополнение счета.
     * POST /account/{id}/deposit
     */
    @PostMapping("/{id}/deposit")
    @ApiOperation(value = "Пополнить счет", notes = "Зачисляет деньги на счет")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Транзакция успешно выполнена", response = Transaction.class),
            @ApiResponse(code = 400, message = "Невалидная сумма (отрицательная, ноль, больше 2 знаков после запятой)", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Счет заблокирован или закрыт", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Transaction> deposit(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Данные транзакции", required = true)
            @RequestBody @Valid TransactionRequest request) {

        Transaction transaction = paymentService.deposit(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Снятие денег со счета.
     * POST /account/{id}/withdraw
     */
    @PostMapping("/{id}/withdraw")
    @ApiOperation(value = "Снять деньги", notes = "Списывает деньги со счета")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Транзакция успешно выполнена", response = Transaction.class),
            @ApiResponse(code = 400, message = "Невалидная сумма или недостаточно средств на счете", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Счет заблокирован или закрыт", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Transaction> withdraw(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Данные транзакции", required = true)
            @RequestBody @Valid TransactionRequest request) {

        Transaction transaction = paymentService.withdraw(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Перевод денег между счетами.
     * POST /account/transfer
     */
    @PostMapping("/transfer")
    @ApiOperation(value = "Перевод между счетами", notes = "Переводит деньги с одного счета на другой")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Перевод успешно выполнен", response = Transaction.class),
            @ApiResponse(code = 400, message = "Невалидные данные (тот же счет, недостаточно средств, невалидная сумма)", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Один из счетов заблокирован или закрыт", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Один из счетов не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Transaction> transfer(
            @ApiParam(value = "Данные перевода", required = true)
            @RequestBody @Valid TransferRequest request) {

        Transaction transaction = paymentService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Получение всех транзакций по счету.
     * GET /account/{id}/transactions
     */
    @GetMapping("/{id}/transactions")
    @ApiOperation(value = "История транзакций", notes = "Возвращает все транзакции счета с возможностью фильтрации по типу")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Список транзакций успешно получен", response = Transaction.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<List<Transaction>> getTransactions(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Фильтр по типу транзакции (DEPOSIT, WITHDRAWAL, TRANSFER)", required = false)
            @RequestParam(required = false) TransactionType type) {

        List<Transaction> transactions;

        if (type != null) {
            transactions = paymentService.getAccountTransactionsByType(id, type);
        } else {
            transactions = paymentService.getAccountTransactions(id);
        }

        return ResponseEntity.ok(transactions);
    }

    /**
     * Обновление статуса счета.
     * PUT /account/{id}/status
     */
    @PutMapping("/{id}/status")
    @ApiOperation(value = "Изменить статус счета",
            notes = "Позволяет заблокировать, активировать или закрыть счет. При закрытии баланс должен быть нулевым.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Статус счета успешно изменен", response = Account.class),
            @ApiResponse(code = 400, message = "Невалидный статус, счет уже закрыт, или баланс не нулевой при попытке закрытия", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Account> updateAccountStatus(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Новый статус (ACTIVE, BLOCKED, CLOSED)", required = true)
            @RequestBody @Valid UpdateAccountStatusRequest request) {

        Account account = paymentService.updateAccountStatus(id, request);
        return ResponseEntity.ok(account);
    }

    /**
     * Обновление владельца счета.
     * PATCH /account/{id}/owner
     */
    @PatchMapping("/{id}/owner")
    @ApiOperation(value = "Изменить владельца счета",
            notes = "Обновляет имя владельца счета. Нельзя изменить владельца закрытого счета.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Владелец счета успешно изменен", response = Account.class),
            @ApiResponse(code = 400, message = "Невалидное имя владельца или счет закрыт", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Account> updateAccountOwner(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Новое имя владельца", required = true)
            @RequestBody @Valid UpdateAccountOwnerRequest request) {

        Account account = paymentService.updateAccountOwner(id, request);
        return ResponseEntity.ok(account);
    }

    /**
     * Удаление (закрытие) счета.
     * DELETE /account/{id}
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Удалить счет",
            notes = "Закрывает счет (soft delete). Баланс должен быть равен нулю, статус не должен быть CLOSED.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Счет успешно закрыт"),
            @ApiResponse(code = 400, message = "Баланс счета не нулевой или счет уже закрыт", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Счет с указанным ID не найден", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Внутренняя ошибка сервера", response = ErrorResponse.class)
    })
    public ResponseEntity<Void> deleteAccount(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id) {

        paymentService.deleteAccount(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}