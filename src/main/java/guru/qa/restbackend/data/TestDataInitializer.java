package guru.qa.restbackend.data;

import guru.qa.restbackend.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для создания тестовых данных.
 * Отвечает только за генерацию начальных данных для разработки и демонстрации.
 */
@Component
public class TestDataInitializer {

    /**
     * Создает список тестовых счетов с различными характеристиками.
     *
     * @return список из 7 предзаполненных счетов
     */
    public List<Account> createTestAccounts() {
        List<Account> accounts = new ArrayList<>();

        // Счет 1: Активный счет Sergey с балансом 1000 USD
        accounts.add(Account.builder()
                .id(1L)
                .accountNumber("40817810000000000001")
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(30))
                .ownerName("Sergey Gluhov")
                .build());

        // Счет 2: Активный счет Dima с балансом 2500 EUR
        accounts.add(Account.builder()
                .id(2L)
                .accountNumber("40817810000000000002")
                .balance(new BigDecimal("2500.00"))
                .currency("EUR")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(20))
                .ownerName("Dima Ivanov")
                .build());

        // Счет 3: Активный счет Alex с балансом 500 RUB
        accounts.add(Account.builder()
                .id(3L)
                .accountNumber("40817810000000000003")
                .balance(new BigDecimal("500.00"))
                .currency("RUB")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(15))
                .ownerName("Alex Petrov")
                .build());

        // Счет 4: Заблокированный счет Dasha с балансом 750 USD
        accounts.add(Account.builder()
                .id(4L)
                .accountNumber("40817810000000000004")
                .balance(new BigDecimal("750.00"))
                .currency("USD")
                .status(AccountStatus.BLOCKED)
                .createdAt(LocalDateTime.now().minusDays(10))
                .ownerName("Dasha Smirnova")
                .build());

        // Счет 5: Пустой активный счет для тестирования удаления
        accounts.add(Account.builder()
                .id(5L)
                .accountNumber("40817810000000000005")
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(5))
                .ownerName("Test User")
                .build());

        // Счет 6: Активный счет Ivan с балансом 1500 USD для тестирования переводов (отправитель)
        accounts.add(Account.builder()
                .id(6L)
                .accountNumber("40817810000000000006")
                .balance(new BigDecimal("1500.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(7))
                .ownerName("Ivan Sidorov")
                .build());

        // Счет 7: Активный счет Maria с балансом 500 USD для тестирования переводов (получатель)
        accounts.add(Account.builder()
                .id(7L)
                .accountNumber("40817810000000000007")
                .balance(new BigDecimal("500.00"))
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(3))
                .ownerName("Maria Volkova")
                .build());

        return accounts;
    }

    /**
     * Создает список тестовых транзакций между счетами.
     *
     * @return список из 5 предзаполненных транзакций
     */
    public List<Transaction> createTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // Транзакция 1: Пополнение счета 1
        transactions.add(Transaction.builder()
                .id(1L)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.00"))
                .fromAccountId(null)
                .toAccountId(1L)
                .timestamp(LocalDateTime.now().minusDays(30))
                .description("Начальное пополнение")
                .status(TransactionStatus.SUCCESS)
                .build());

        // Транзакция 2: Пополнение счета 2
        transactions.add(Transaction.builder()
                .id(2L)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("3000.00"))
                .fromAccountId(null)
                .toAccountId(2L)
                .timestamp(LocalDateTime.now().minusDays(20))
                .description("Зарплата")
                .status(TransactionStatus.SUCCESS)
                .build());

        // Транзакция 3: Перевод со счета 2 на счет 3
        transactions.add(Transaction.builder()
                .id(3L)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("500.00"))
                .fromAccountId(2L)
                .toAccountId(3L)
                .timestamp(LocalDateTime.now().minusDays(15))
                .description("Возврат долга")
                .status(TransactionStatus.SUCCESS)
                .build());

        // Транзакция 4: Снятие со счета 1
        transactions.add(Transaction.builder()
                .id(4L)
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("0.00"))
                .fromAccountId(1L)
                .toAccountId(null)
                .timestamp(LocalDateTime.now().minusDays(10))
                .description("Снятие наличных")
                .status(TransactionStatus.SUCCESS)
                .build());

        // Транзакция 5: Перевод со счета 6 на счет 7 для демонстрации
        transactions.add(Transaction.builder()
                .id(5L)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("300.00"))
                .fromAccountId(6L)
                .toAccountId(7L)
                .timestamp(LocalDateTime.now().minusDays(2))
                .description("Переводит друга")
                .status(TransactionStatus.SUCCESS)
                .build());

        return transactions;
    }
}
