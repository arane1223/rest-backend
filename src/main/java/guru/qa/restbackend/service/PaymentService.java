package guru.qa.restbackend.service;

import guru.qa.restbackend.domain.*;
import guru.qa.restbackend.exception.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Сервис для управления счетами и транзакциями.
 * Содержит всю бизнес-логику платежной системы.
 */
@Service
public class PaymentService {

    // Хранилища данных (в памяти, для учебных целей)
    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();

    // Генераторы ID
    private final AtomicLong accountIdGenerator = new AtomicLong(1);
    private final AtomicLong transactionIdGenerator = new AtomicLong(1);

    /**
     * Создание нового счета.
     *
     * @param request запрос с данными для создания счета
     * @return созданный счет с уникальным ID и номером
     */
    public Account createAccount(CreateAccountRequest request) {
        Long accountId = accountIdGenerator.getAndIncrement();

        Account account = Account.builder()
                .id(accountId)
                .accountNumber(generateAccountNumber(accountId))
                .balance(BigDecimal.ZERO)  // Начальный баланс = 0
                .currency(request.getCurrency())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .ownerName(request.getOwnerName())
                .build();

        accounts.put(accountId, account);
        return account;
    }

    /**
     * Получение счета по ID.
     *
     * @param accountId ID счета
     * @return счет
     * @throws AccountNotFoundException если счет не найден
     */
    public Account getAccount(Long accountId) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    /**
     * Получение всех счетов.
     *
     * @return список всех счетов
     */
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * Получение баланса счета.
     *
     * @param accountId ID счета
     * @return баланс
     */
    public BigDecimal getBalance(Long accountId) {
        Account account = getAccount(accountId);
        return account.getBalance();
    }

    /**
     * Пополнение счета.
     *
     * @param accountId ID счета
     * @param request   запрос с суммой пополнения
     * @return транзакция пополнения
     */
    public Transaction deposit(Long accountId, TransactionRequest request) {
        Account account = getAccount(accountId);
        validateAccountActive(account);
        validateAmount(request.getAmount());

        // Увеличиваем баланс
        account.setBalance(account.getBalance().add(request.getAmount()));

        // Создаем транзакцию
        Transaction transaction = Transaction.builder()
                .id(transactionIdGenerator.getAndIncrement())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .fromAccountId(null)  // Пополнение извне
                .toAccountId(accountId)
                .timestamp(LocalDateTime.now())
                .description(request.getDescription() != null ?
                        request.getDescription() : "Пополнение счета")
                .status(TransactionStatus.SUCCESS)
                .build();

        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Снятие денег со счета.
     *
     * @param accountId ID счета
     * @param request   запрос с суммой снятия
     * @return транзакция снятия
     */
    public Transaction withdraw(Long accountId, TransactionRequest request) {
        Account account = getAccount(accountId);
        validateAccountActive(account);
        validateAmount(request.getAmount());

        // Проверяем достаточность средств
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(accountId);
        }

        // Уменьшаем баланс
        account.setBalance(account.getBalance().subtract(request.getAmount()));

        // Создаем транзакцию
        Transaction transaction = Transaction.builder()
                .id(transactionIdGenerator.getAndIncrement())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .fromAccountId(accountId)
                .toAccountId(null)  // Снятие наличных
                .timestamp(LocalDateTime.now())
                .description(request.getDescription() != null ?
                        request.getDescription() : "Снятие со счета")
                .status(TransactionStatus.SUCCESS)
                .build();

        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Перевод денег между счетами.
     *
     * @param request запрос с данными перевода
     * @return транзакция перевода
     */
    public Transaction transfer(TransferRequest request) {
        // Проверка: нельзя переводить на тот же счет
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new SameAccountTransferException();
        }

        Account fromAccount = getAccount(request.getFromAccountId());
        Account toAccount = getAccount(request.getToAccountId());

        validateAccountActive(fromAccount);
        validateAccountActive(toAccount);
        validateAmount(request.getAmount());

        // Проверяем достаточность средств
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(request.getFromAccountId());
        }

        // Выполняем перевод
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        // Создаем транзакцию
        Transaction transaction = Transaction.builder()
                .id(transactionIdGenerator.getAndIncrement())
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .timestamp(LocalDateTime.now())
                .description(request.getDescription() != null ?
                        request.getDescription() : "Перевод между счетами")
                .status(TransactionStatus.SUCCESS)
                .build();

        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Получение всех транзакций по счету.
     *
     * @param accountId ID счета
     * @return список транзакций
     */
    public List<Transaction> getAccountTransactions(Long accountId) {
        // Проверяем существование счета
        getAccount(accountId);

        return transactions.values().stream()
                .filter(t -> accountId.equals(t.getFromAccountId()) ||
                        accountId.equals(t.getToAccountId()))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Получение транзакций по счету с фильтрацией по типу.
     *
     * @param accountId ID счета
     * @param type      тип транзакции
     * @return список отфильтрованных транзакций
     */
    public List<Transaction> getAccountTransactionsByType(Long accountId, TransactionType type) {
        return getAccountTransactions(accountId).stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Обновление статуса счета.
     *
     * @param accountId ID счета
     * @param request новый статус
     * @return обновленный счет
     */
    public Account updateAccountStatus(Long accountId, UpdateAccountStatusRequest request) {
        Account account = getAccount(accountId);

        // Проверка: нельзя повторно закрыть уже закрытый счет
        if (account.getStatus() == AccountStatus.CLOSED &&
                request.getStatus() == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException(accountId);
        }

        // Проверка: при закрытии счета баланс должен быть нулевым
        if (request.getStatus() == AccountStatus.CLOSED &&
                account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountHasBalanceException(accountId);
        }

        account.setStatus(request.getStatus());
        return account;
    }

    /**
     * Обновление владельца счета.
     *
     * @param accountId ID счета
     * @param request новое имя владельца
     * @return обновленный счет
     */
    public Account updateAccountOwner(Long accountId, UpdateAccountOwnerRequest request) {
        Account account = getAccount(accountId);

        // Проверка: нельзя изменять владельца закрытого счета
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException(accountId);
        }

        account.setOwnerName(request.getOwnerName());
        return account;
    }

    /**
     * Удаление (закрытие) счета.
     * Счет можно удалить только если:
     * - баланс равен нулю
     * - статус не CLOSED
     *
     * @param accountId ID счета
     */
    public void deleteAccount(Long accountId) {
        Account account = getAccount(accountId);

        // Проверка: нельзя удалить счет с деньгами
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountHasBalanceException(accountId);
        }

        // Проверка: нельзя удалить уже закрытый счет
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountAlreadyClosedException(accountId);
        }

        // Помечаем счет как закрытый (не удаляем физически для сохранения истории)
        account.setStatus(AccountStatus.CLOSED);
    }

    // ========== ПРИВАТНЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Генерация номера счета (упрощенная версия).
     */
    private String generateAccountNumber(Long accountId) {
        return String.format("40817810%012d", accountId);
    }

    /**
     * Проверка, что счет активен (не заблокирован и не закрыт).
     */
    private void validateAccountActive(Account account) {
        if (account.getStatus() == AccountStatus.BLOCKED ||
                account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountBlockedException(account.getId());
        }
    }

    /**
     * Валидация суммы транзакции.
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Сумма не может быть null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Сумма должна быть больше нуля");
        }
        if (amount.scale() > 2) {
            throw new InvalidAmountException("Максимум 2 знака после запятой");
        }
    }
}
