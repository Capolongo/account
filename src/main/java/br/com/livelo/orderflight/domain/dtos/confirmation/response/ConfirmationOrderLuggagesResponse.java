package br.com.livelo.orderflight.domain.dtos.confirmation.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmationOrderLuggagesResponse {
    private String type;
    private String description;
}

