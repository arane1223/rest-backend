package guru.qa.restbackend.helpers;

import guru.qa.restbackend.domain.AccountStatus;
import guru.qa.restbackend.domain.TransactionStatus;
import guru.qa.restbackend.domain.TransactionType;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResponseHelpers {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;

    public static AccountStatus getAccountStatusFromResponse(Response response) {
        String status = response.jsonPath().getString("status");
        return AccountStatus.valueOf(status);
    }

    public static BigDecimal getBalanceFromResponse(Response response) {
        Object balanceObj = response.jsonPath().get("balance");
        return new BigDecimal(balanceObj.toString());
    }

    public static LocalDate getCreatedDateFromResponse(Response response) {
        String createdAtString = response.jsonPath().getString("createdAt");
        LocalDateTime localDateTime = LocalDateTime.parse(createdAtString, DATE_TIME_FORMATTER);
        return localDateTime.toLocalDate();
    }

    public static int getStatusCodeFromResponse(Response response) {
        return response.jsonPath().getInt("status");
    }

    public static TransactionType getTransactionTypeFromResponse(Response response) {
        String type = response.jsonPath().getString("type");
        return TransactionType.valueOf(type);
    }

    public static BigDecimal getAmountFromResponse(Response response) {
        Object amountObj = response.jsonPath().get("amount");
        return new BigDecimal(amountObj.toString());
    }

    public static TransactionStatus getTransactionStatusFromResponse(Response response) {
        String status = response.jsonPath().getString("status");
        return TransactionStatus.valueOf(status);
    }

    public static String getAccountId(Response response) {
        return response.jsonPath().getString("id");
    }
}
