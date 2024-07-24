package br.com.account.service;

import br.com.account.dto.request.AccountSituationRequest;
import br.com.account.dto.response.MessageResponse;
import br.com.account.dto.response.PaginationAccountResponse;
import br.com.account.entities.ContaEntity;
import br.com.account.enuns.Situation;
import br.com.account.exception.AccountException;
import br.com.account.mappers.AccountMapper;
import br.com.account.repository.ContaRepository;
import br.com.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static br.com.account.mockBuilder.AccountMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private AccountMapper mapper;
    @InjectMocks
    private AccountServiceImpl service;

    @Test
    void shouldCreateSuccessfully() throws Exception {

        var contaEntity = builderContaEntity();

        when(mapper.accountRequestToContaCreate(any(), anyString())).thenReturn(contaEntity);

        contaEntity.setId(1L);
        when(contaRepository.save(any())).thenReturn(contaEntity);

        when(mapper.contaEntityToAccountResponse(any())).thenReturn(builderAccountResponse());

        var response = service.createAccount(builderAccountRequest());

        assertEquals(contaEntity.getId(), response.getAccount().getId());
        assertEquals(contaEntity.getValor(), response.getAccount().getValue());
        assertEquals(contaEntity.getSituacao(), response.getAccount().getSituation());
        assertEquals(contaEntity.getDescricao(), response.getAccount().getDescription());
        assertEquals(contaEntity.getDataVencimento(), response.getAccount().getDueDate());
        assertEquals(contaEntity.getDataPagamento(), response.getAccount().getPaymentDate());
    }

    @Test
    void shouldCreateErrorException() {

        when(mapper.accountRequestToContaCreate(any(), anyString())).thenThrow(new RuntimeException("Error message"));
        assertThrows(AccountException.class, () -> {
            service.createAccount(builderAccountRequest());
        });
    }

    @Test
    void shouldUpdateSuccessfully() throws Exception {

        var contaEntity = builderContaEntity();

        var optionFindAll =Optional.of(contaEntity);

        when(contaRepository.findById(any())).thenReturn(optionFindAll);

        when(mapper.accountRequestToContaUpdate(any(), any(), anyLong())).thenReturn(contaEntity);

        contaEntity.setId(1L);
        when(contaRepository.save(any())).thenReturn(contaEntity);

        when(mapper.contaEntityToAccountResponse(any())).thenReturn(builderAccountResponse());

        var response = service.updateAccount(builderAccountRequest(), 1L);

        assertEquals(contaEntity.getId(), response.getAccount().getId());
        assertEquals(contaEntity.getValor(), response.getAccount().getValue());
        assertEquals(contaEntity.getSituacao(), response.getAccount().getSituation());
        assertEquals(contaEntity.getDescricao(), response.getAccount().getDescription());
        assertEquals(contaEntity.getDataVencimento(), response.getAccount().getDueDate());
        assertEquals(contaEntity.getDataPagamento(), response.getAccount().getPaymentDate());
    }

    @Test
    void shouldUpdateErrorException() {
        when(contaRepository.findById(any())).thenThrow(new RuntimeException("Error"));
        assertThrows(Exception.class, () -> {
            service.updateAccount(builderAccountRequest(), 1L);
        });
    }

    @Test
    void shouldUpdateErrorAccountException() {
        when(contaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(AccountException.class, () -> {
            service.updateAccount(builderAccountRequest(), 1L);
        });
    }

    @Test
    void shouldUpdateSituationAccountSuccessfully() throws Exception {

        var contaEntity = builderContaEntity();

        var optionFindAll = Optional.of(contaEntity);

        when(contaRepository.findById(any())).thenReturn(optionFindAll);

        lenient().when(mapper.accountRequestToContaUpdate(any(), any(), anyLong())).thenReturn(contaEntity);

        contaEntity.setId(1L);
        when(contaRepository.save(any())).thenReturn(contaEntity);

        when(mapper.contaEntityToAccountResponse(any())).thenReturn(builderAccountResponse());

        var response = service.updateSituationAccount(AccountSituationRequest.builder()
                .situation(Situation.CANCELLED).build(), 1L);

        assertEquals(contaEntity.getId(), response.getAccount().getId());
        assertEquals(contaEntity.getValor(), response.getAccount().getValue());
        assertEquals(contaEntity.getSituacao(), response.getAccount().getSituation());
        assertEquals(contaEntity.getDescricao(), response.getAccount().getDescription());
        assertEquals(contaEntity.getDataVencimento(), response.getAccount().getDueDate());
        assertEquals(contaEntity.getDataPagamento(), response.getAccount().getPaymentDate());
    }

    @Test
    void shouldUpdateSituationAccountErrorException() {
        when(contaRepository.findById(any())).thenThrow(new RuntimeException("Error"));
        assertThrows(Exception.class, () -> {
            service.updateSituationAccount(AccountSituationRequest.builder()
                    .situation(Situation.CANCELLED).build(), 1L);
        });
    }

    @Test
    void shouldUpdateSituationAccountErrorAccountException() {
        when(contaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(AccountException.class, () -> {
            service.updateSituationAccount(AccountSituationRequest.builder()
                    .situation(Situation.CANCELLED).build(), 1L);
        });
    }

    @Test
    void shouldGetByIdSuccessfully() throws Exception {

        var contaEntity = builderContaEntity();

        var optionFind =Optional.of(contaEntity);

        when(contaRepository.findById(any())).thenReturn(optionFind);

        when(mapper.contaEntityToAccountResponse(any())).thenReturn(builderAccountResponse());

        var response = service.getAccountById(1L);

        assertEquals(contaEntity.getId(), response.getAccount().getId());
        assertEquals(contaEntity.getValor(), response.getAccount().getValue());
        assertEquals(contaEntity.getSituacao(), response.getAccount().getSituation());
        assertEquals(contaEntity.getDescricao(), response.getAccount().getDescription());
        assertEquals(contaEntity.getDataVencimento(), response.getAccount().getDueDate());
        assertEquals(contaEntity.getDataPagamento(), response.getAccount().getPaymentDate());
    }

    @Test
    void shouldGetByIdErrorException() {
        when(contaRepository.findById(any())).thenThrow(new RuntimeException("Error"));
        assertThrows(Exception.class, () -> {
            service.getAccountById(1L);
        });
    }

    @Test
    void shouldGetByIdErrorAccountException() {
        when(contaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(AccountException.class, () -> {
            service.getAccountById(1L);
        });
    }

    @Test
    void shouldGetAllSuccessfully() throws Exception {

        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        ContaEntity contaEntity = builderContaEntity(); // Add necessary fields
        Page<ContaEntity> responsePage = new PageImpl<>(Collections.singletonList(contaEntity), pageable, 1);

        PaginationAccountResponse paginationAccountResponse = PaginationAccountResponse.builder()
                .accounts(Collections.singletonList(builderAccount()))
                .page(0)
                .rows(1)
                .total(10)
                .totalPages(1)
                .build();

        doReturn(responsePage).when(contaRepository).findAll(any(Pageable.class));
        doReturn(paginationAccountResponse).when(mapper).contaEntityToPaginationAccountResponse(responsePage);
        var response = service.getFindAllAccounts(0, 1);

        assertEquals(1, response.getAccounts().size());
    }

    @Test
    void shouldGetAllErrorException() {
        when(contaRepository.findAll()).thenThrow(new RuntimeException("Error"));
        assertThrows(Exception.class, () -> {
            service.getFindAllAccounts(0, 1);
        });
    }

    @Test
    void shouldGetAllErrorAccountException() {
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        Page<ContaEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(contaRepository.findAll(pageable)).thenReturn(emptyPage);
        assertThrows(AccountException.class, () -> service.getFindAllAccounts(page, size));
        verify(contaRepository, times(1)).findAll(pageable);
    }

    @Test
    void shouldGetAccountsPaySuccessfully() throws Exception {

        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        ContaEntity contaEntity = builderContaEntity();
        Page<ContaEntity> responsePage = new PageImpl<>(Collections.singletonList(contaEntity), pageable, 1);

        PaginationAccountResponse paginationAccountResponse = PaginationAccountResponse.builder()
                .accounts(Collections.singletonList(builderAccount()))
                .page(0)
                .rows(1)
                .total(10)
                .totalPages(1)
                .build();

        doReturn(responsePage).when(contaRepository).findByDataVencimentoBetweenOrDescricao(any(), any(), anyString(), any(Pageable.class));
        doReturn(paginationAccountResponse).when(mapper).contaEntityToPaginationAccountResponse(responsePage);
        var response = service.getAccountsPay(LocalDate.of(2024, 10, 01), LocalDate.of(2024, 10, 01), "teste", 0, 1);

        assertEquals(1, response.getAccounts().size());
    }

    @Test
    void shouldGetAccountsPayErrorException() {
        lenient().when(contaRepository.findAll()).thenThrow(new RuntimeException("Error"));
        assertThrows(Exception.class, () -> {
            service.getAccountsPay(LocalDate.of(2024, 10, 01), LocalDate.of(2024, 10, 01), "teste", 0, 1);
        });
    }

    @Test
    void shouldGetAccountsPayErrorAccountException() {
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        Page<ContaEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        doReturn(emptyPage).when(contaRepository).findByDataVencimentoBetweenOrDescricao(any(), any(), anyString(), any(Pageable.class));
        assertThrows(AccountException.class, () -> service.getAccountsPay(LocalDate.of(2024, 10, 01), LocalDate.of(2024, 10, 01), "teste", 0, 1));
    }

    @Test
    public void testBatchAccountsSuccess() throws Exception {
        String csvData = "descricao,valor,dataPagamento,dataVencimento\n" +
                "Test,100.00,2023-01-01,2023-01-10";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "text/csv", new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)));

        MessageResponse response = service.batchAccounts(file);

        assertEquals(HttpStatus.OK.value(), response.code());
        assertEquals("Lote Sucess", response.description());
        verify(contaRepository, times(1)).save(any(ContaEntity.class));
    }

    @Test
    void testBatchAccountsError() throws IOException {
        assertThrows(Exception.class, () -> service.batchAccounts(null));
    }
}
