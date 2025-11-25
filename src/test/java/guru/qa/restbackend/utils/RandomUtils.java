package guru.qa.restbackend.utils;

import com.github.javafaker.Faker;
import guru.qa.restbackend.domain.CreateAccountRequest;

import static java.lang.String.valueOf;

public class RandomUtils {
    private static Faker faker = new Faker();

    public static String getRandomOwnerName() {
        return faker.name().firstName();
    }

    public static String getRandomCurrency() {
        return faker.options().option("USD", "EUR", "RUB");
    }

    public static CreateAccountRequest generateNewAccountData() {
        String newOwnerName = getRandomOwnerName();
        String newCurrency = getRandomCurrency();
        return new CreateAccountRequest(newOwnerName, newCurrency);
    }

    public static String getRandomId() {
        return valueOf(faker.number().numberBetween(10, 100));
    }

    public static int getRandomAmount() {
        return faker.number().numberBetween(1000,100000);
    }
}
