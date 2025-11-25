package guru.qa.restbackend.utils;

import com.github.javafaker.Faker;
import guru.qa.restbackend.domain.CreateAccountRequest;
import io.qameta.allure.Step;

import static java.lang.String.valueOf;

public class RandomUtils {
    private static Faker faker = new Faker();

    public static String getRandomOwnerName() {
        return faker.name().firstName();
    }

    public static String getRandomCurrency() {
        return faker.options().option("USD", "EUR", "RUB");
    }

    @Step("Сгенерировать рандомные данные для нового пользователя")
    public static CreateAccountRequest generateNewAccountData() {
        String newOwnerName = getRandomOwnerName();
        String newCurrency = getRandomCurrency();
        return new CreateAccountRequest(newOwnerName, newCurrency);
    }

    @Step("Сгенерировать рандомный ID")
    public static String getRandomId() {
        return valueOf(faker.number().numberBetween(10, 100));
    }

    @Step("Сгенерировать случайную сумму")
    public static int getRandomAmount() {
        return faker.number().numberBetween(1000,100000);
    }
}
