package guru.qa.restbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountOwnerRequest {

    @NotBlank(message = "Имя владельца обязательно")
    private String ownerName;
}
