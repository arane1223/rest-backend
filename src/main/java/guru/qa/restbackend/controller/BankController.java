package guru.qa.restbackend.controller;

import guru.qa.restbackend.domain.LoginInfo;
import guru.qa.restbackend.domain.UserInfo;
import guru.qa.restbackend.exception.InvalidUsernameException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Api(tags = "User Management", description = "Управление пользователями")
public class BankController {

    private Map<String, UserInfo> users = Map.of(
            "Dima", UserInfo.builder().userName("Dima").build(),
            "Alex", UserInfo.builder().userName("Alex").build(),
            "Dasha", UserInfo.builder().userName("Dasha").build()
    );

    @PostMapping("user/login")
    @ApiOperation("Авторизация пользователя")
    public UserInfo doLogin(@RequestBody LoginInfo loginInfo) {
        if (loginInfo.getUserName().equals("Sergey")) {
            return UserInfo.builder()
                    .loginDate(new Date())
                    .userName(loginInfo.getUserName())
                    .build();
        } else {
            throw new InvalidUsernameException(loginInfo.getUserName());
        }
    }

    @GetMapping("user/all")
    @ApiOperation("Получение всех пользователей")
    public List<UserInfo> getAllUsersInfo() {
        return users.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
