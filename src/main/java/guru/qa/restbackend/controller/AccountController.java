package guru.qa.restbackend.controller;

import guru.qa.restbackend.domain.*;
import guru.qa.restbackend.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/account")
@Api(tags = "Account Management", description = "Управление банковскими счетами")
public class AccountController {

    private final PaymentService paymentService;

    @Autowired
    public AccountController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @ApiOperation(value = "Создание счета", notes = "Создает новый банковский счет с нулевым балансом")
    public ResponseEntity<Account> createAccount(
            @ApiParam(value = "Данные для создания счета", required = true)
            @RequestBody @Valid CreateAccountRequest request) {

        Account account = paymentService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Получить счет", notes = "Возвращает информацию о счете по его ID")
    public ResponseEntity<Account> getAccount(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id) {

        Account account = paymentService.getAccount(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/all")
    @ApiOperation(value = "Получить все счета", notes = "Возвращает список всех счетов в системе")
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = paymentService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}/balance")
    @ApiOperation(value = "Получить баланс", notes = "Возвращает текущий баланс счета")
    public ResponseEntity<BigDecimal> getBalance(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id) {

        BigDecimal balance = paymentService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{id}/deposit")
    @ApiOperation(value = "Пополнить счет", notes = "Зачисляет деньги на счет")
    public ResponseEntity<Transaction> deposit(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Данные транзакции", required = true)
            @RequestBody @Valid TransactionRequest request) {

        Transaction transaction = paymentService.deposit(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/{id}/withdraw")
    @ApiOperation(value = "Снять деньги", notes = "Списывает деньги со счета")
    public ResponseEntity<Transaction> withdraw(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Данные транзакции", required = true)
            @RequestBody @Valid TransactionRequest request) {

        Transaction transaction = paymentService.withdraw(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    @ApiOperation(value = "Перевод между счетами", notes = "Переводит деньги с одного счета на другой")
    public ResponseEntity<Transaction> transfer(
            @ApiParam(value = "Данные перевода", required = true)
            @RequestBody @Valid TransferRequest request) {

        Transaction transaction = paymentService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}/transactions")
    @ApiOperation(value = "История транзакций", notes = "Возвращает все транзакции счета")
    public ResponseEntity<List<Transaction>> getTransactions(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Фильтр по типу транзакции", required = false)
            @RequestParam(required = false) TransactionType type) {

        List<Transaction> transactions;

        if (type != null) {
            transactions = paymentService.getAccountTransactionsByType(id, type);
        } else {
            transactions = paymentService.getAccountTransactions(id);
        }

        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "Изменить статус счета",
            notes = "Позволяет заблокировать, активировать или закрыть счет")
    public ResponseEntity<Account> updateAccountStatus(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Новый статус", required = true)
            @RequestBody @Valid UpdateAccountStatusRequest request) {

        Account account = paymentService.updateAccountStatus(id, request);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}/owner")
    @ApiOperation(value = "Изменить владельца счета",
            notes = "Обновляет имя владельца счета")
    public ResponseEntity<Account> updateAccountOwner(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @ApiParam(value = "Новое имя владельца", required = true)
            @RequestBody @Valid UpdateAccountOwnerRequest request) {

        Account account = paymentService.updateAccountOwner(id, request);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Удалить счет",
            notes = "Закрывает счет. Баланс должен быть равен нулю.")
    public ResponseEntity<Void> deleteAccount(
            @ApiParam(value = "ID счета", required = true, example = "1")
            @PathVariable Long id) {

        paymentService.deleteAccount(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

}