package guru.qa.restbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private Long fromAccountId;
    private Long toAccountId;
    private LocalDateTime timestamp;
    private String description;
    private TransactionStatus status;
}
