package br.com.account.mockBuilder;

import br.com.account.dto.AccountDTO;
import br.com.account.dto.request.AccountRequest;
import br.com.account.dto.response.AccountResponse;
import br.com.account.entities.ContaEntity;
import br.com.account.enuns.Situation;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountMock {

    public static ContaEntity builderContaEntity() {
        return ContaEntity.builder()
                .id(1L)
                .dataPagamento(LocalDate.of(2024, 10, 01))
                .dataVencimento(LocalDate.of(2024, 10, 01))
                .valor(new BigDecimal("1"))
                .descricao("Paymento to Sucess")
                .situacao(Situation.PENDING.getDescription())
                .build();
    }

    public static AccountResponse builderAccountResponse() {
        return AccountResponse.builder()
                .account(builderAccount())
                .build();
    }

    public static AccountRequest builderAccountRequest() {
        return AccountRequest.builder()
                .account(builderAccount())
                .build();
    }

    public static AccountDTO builderAccount() {
        return AccountDTO.builder()
                .id(1L)
                .value(new BigDecimal("1"))
                .paymentDate(LocalDate.of(2024, 10, 01))
                .description("Paymento to Sucess")
                .dueDate(LocalDate.of(2024, 10, 01))
                .situation(Situation.PENDING.getDescription())
                .build();
    }
}
