package guru.qa.restbackend.tests;

import guru.qa.restbackend.config.AuthConfig;
import guru.qa.restbackend.domain.LoginInfo;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;

public class TestData {
    public static final List<String> USERS = List.of(
            "Alex", "Dima", "Dasha");

    protected static AuthConfig authData = ConfigFactory.create(AuthConfig.class);
    public static final LoginInfo CORRECT_AUTH_DATA = new LoginInfo(authData.userName(), authData.password());
    public static final LoginInfo INCORRECT_AUTH_DATA = new LoginInfo(authData.brokenUserName(), authData.password());
}
