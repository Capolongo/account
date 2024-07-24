package br.com.account.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
public class AccountDTO {
    private Long id;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private BigDecimal value;
    private String description;
    private String situation;
}
