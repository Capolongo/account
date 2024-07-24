package br.com.account.repository;

import br.com.account.entities.ContaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ContaRepository extends JpaRepository<ContaEntity, Long> {

    Page<ContaEntity> findByDataVencimentoBetweenOrDescricao(
            LocalDate startDataVencimento, LocalDate endDataVencimento,
            String descricao,
            Pageable pageable
    );

}
