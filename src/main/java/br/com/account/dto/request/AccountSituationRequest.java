package br.com.account.dto.request;

import br.com.account.enuns.Situation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSituationRequest {
    private Situation situation;
}
