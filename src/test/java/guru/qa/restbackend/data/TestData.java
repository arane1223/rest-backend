package guru.qa.restbackend.data;

import guru.qa.restbackend.config.AuthConfig;
import guru.qa.restbackend.domain.*;
import org.aeonbits.owner.ConfigFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TestData {
    public static final List<String> USERS = List.of(
            "Alex", "Dima", "Dasha");

    protected static AuthConfig authData = ConfigFactory.create(AuthConfig.class);
    public static final LoginInfo CORRECT_AUTH_DATA = new LoginInfo(authData.userName(), authData.password());
    public static final LoginInfo INCORRECT_AUTH_DATA = new LoginInfo(authData.brokenUserName(), authData.password());

    public static final Account FIRST_USER_DATA = new Account(
            1L,
            "40817810000000000001",
            new BigDecimal("1000.00"),
            "USD",
            AccountStatus.ACTIVE,
            LocalDateTime.now().minusDays(30),
            "Sergey Gluhov"
    );

    public static final Account SECOND_USER_DATA = new Account(
            2L,
            "40817810000000000002",
            new BigDecimal("2500.00"),
            "EUR",
            AccountStatus.ACTIVE,
            LocalDateTime.now().minusDays(20),
            "Dima Ivanov"
    );

    public static final Account THIRD_USER_DATA = new Account(
            3L,
            "40817810000000000003",
            new BigDecimal("500.00"),
            "RUB",
            AccountStatus.ACTIVE,
            LocalDateTime.now().minusDays(15),
            "Alex Petrov"
    );

    public static final Account FOURTH_USER_DATA = new Account(
            4L,
            "40817810000000000004",
            new BigDecimal("750.00"),
            "USD",
            AccountStatus.BLOCKED,
            LocalDateTime.now().minusDays(10),
            "Dasha Smirnova"
    );

    public static final TransferRequest SUCCESS_TEST_TRANSFER_REQUEST_DATA = new TransferRequest(
            6L,
            7L,
            new BigDecimal(1000),
            "transfer description"
    );

    public static final TransferRequest TEST_TRANSFER_ON_SAME_ACCOUNT_REQUEST_DATA = new TransferRequest(
            6L,
            6L,
            new BigDecimal(100),
            "transfer description"
    );


}
