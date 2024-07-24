package br.com.account.enuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Situation {
    PENDING("Pagamento pendente"),
    COMPLETED("Pagamento concluído"),
    FAILED("Pagamento falhou"),
    CANCELLED("Pagamento cancelado");

    private String description;


}