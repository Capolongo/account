package br.com.livelo.orderflight.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FLIGHT_LEG")
@EqualsAndHashCode(callSuper = false)
public class FlightLegEntity extends BaseEntity {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FLIGHT_LEG_SEQ")
    @SequenceGenerator(name = "FLIGHT_LEG_SEQ", sequenceName = "FLIGHT_LEG_SEQ", allocationSize = 1)
    @Id
    private Long id;

    private String flightNumber;

    private Integer flightDuration;

    private String airline;

    private String managedBy;

    private Integer timeToWait;
    
    private String originIata;

    private String originDescription;

    private String destinationIata;

    private String destinationDescription;

    private LocalDateTime departureDate;

    private LocalDateTime arrivalDate;

    private String type;
}
