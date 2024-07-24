package br.com.account.mappers;

import br.com.account.dto.AccountDTO;
import br.com.account.dto.request.AccountRequest;
import br.com.account.dto.response.AccountResponse;
import br.com.account.dto.response.PaginationAccountResponse;
import br.com.account.entities.ContaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "dataVencimento", source = "request.account.dueDate")
    @Mapping(target = "dataPagamento", source = "request.account.paymentDate")
    @Mapping(target = "valor", source = "request.account.value")
    @Mapping(target = "descricao", source = "request.account.description")
    ContaEntity accountRequestToContaCreate(AccountRequest request, String situacao);

    @Mapping(target = "dataVencimento", source = "request.account.dueDate")
    @Mapping(target = "dataPagamento", source = "request.account.paymentDate")
    @Mapping(target = "valor", source = "request.account.value")
    @Mapping(target = "descricao", source = "request.account.description")
    ContaEntity accountRequestToContaUpdate(AccountRequest request, @MappingTarget ContaEntity conta, Long id);

    ContaEntity accountSituationRequestToContaUpdate(@MappingTarget ContaEntity conta, String situacao, Long id);

    @Mapping(target = "account.id", source = "conta.id")
    @Mapping(target = "account.dueDate", source = "conta.dataVencimento")
    @Mapping(target = "account.paymentDate", source = "conta.dataPagamento")
    @Mapping(target = "account.value", source = "conta.valor")
    @Mapping(target = "account.description", source = "conta.descricao")
    @Mapping(target = "account.situation", source = "conta.situacao")
    AccountResponse contaEntityToAccountResponse(ContaEntity conta);

    @Mapping(target = "dueDate", source = "conta.dataVencimento")
    @Mapping(target = "paymentDate", source = "conta.dataPagamento")
    @Mapping(target = "value", source = "conta.valor")
    @Mapping(target = "description", source = "conta.descricao")
    @Mapping(target = "situation", source = "conta.situacao")
    AccountDTO contaEntityToAccountDTO(ContaEntity conta);

    @Mapping(target = "accounts", expression = "java(contentToAccountDTO(conta.getContent()))")
    @Mapping(target = "page", expression = "java(pageCountValidation(conta.getPageable()))")
    @Mapping(source = "pageable.pageSize", target = "rows")
    @Mapping(source = "totalElements", target = "total")
    PaginationAccountResponse contaEntityToPaginationAccountResponse(Page<ContaEntity> conta);

    default List<AccountDTO> contentToAccountDTO(List<ContaEntity> content) {

        if(content.isEmpty()) {
            return List.of();
        }
        return content.stream().map(cont -> AccountDTO.builder()
                .value(cont.getValor())
                .situation(cont.getSituacao())
                .paymentDate(cont.getDataPagamento())
                .dueDate(cont.getDataVencimento())
                .description(cont.getDescricao())
                .id(cont.getId())
                .build()).toList();
    }

    default int pageCountValidation(Pageable pageable) {
        return pageable.getPageNumber() + 1;
    }

}
