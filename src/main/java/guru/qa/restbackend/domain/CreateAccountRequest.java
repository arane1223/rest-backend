package guru.qa.restbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Имя владельца обязательно")
    private String ownerName;

    @NotBlank(message = "Валюта обязательна")
    @Pattern(regexp = "USD|EUR|RUB", message = "Поддерживаемые валюты: USD, EUR, RUB")
    private String currency;
}
