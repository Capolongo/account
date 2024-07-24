package br.com.account.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
@Table(name = "CONTA")
public class ContaEntity {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTA_SEQ")
    @SequenceGenerator(name = "CONTA_SEQ", sequenceName = "CONTA_SEQ", allocationSize = 1)
    @Id
    private Long id;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private BigDecimal valor;
    private String descricao;
    private String situacao;
}
