package guru.qa.restbackend.config;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:auth.properties"
})
public interface AuthConfig extends Config {

    @Key("userName")
    String userName();

    @Key("password")
    String password();

    @Key("brokenUserName")
    String brokenUserName();
}
