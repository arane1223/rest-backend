package guru.qa.restbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountStatusRequest {

    @NotNull(message = "Статус обязателен")
    private AccountStatus status;
}
