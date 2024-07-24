package br.com.account.dto.response;

import br.com.account.dto.AccountDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {
    private AccountDTO account;
}
