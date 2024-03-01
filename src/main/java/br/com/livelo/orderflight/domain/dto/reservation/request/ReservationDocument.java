package br.com.livelo.orderflight.domain.dto.reservation.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReservationDocument {
	private String documentNumber;
    private String type;
}
