package br.com.account.controller;

import br.com.account.dto.request.AccountRequest;
import br.com.account.dto.request.AccountSituationRequest;
import br.com.account.dto.response.AccountResponse;
import br.com.account.dto.response.MessageResponse;
import br.com.account.dto.response.PaginationAccountResponse;
import br.com.account.enuns.Situation;
import br.com.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import static br.com.account.mockBuilder.AccountMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

//        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAccount() throws Exception {
        AccountRequest request = new AccountRequest();
        AccountResponse response = builderAccountResponse();

        when(accountService.createAccount(any())).thenReturn(response);

        ResponseEntity<AccountResponse> result = accountController.createAccount(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    public void testUpdateAccount() throws Exception {
        AccountRequest request = new AccountRequest();

        var response = builderAccountResponse();

        when(accountService.updateAccount(any(AccountRequest.class), anyLong())).thenReturn(response);

        ResponseEntity<AccountResponse> result = accountController.updateAccount(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    public void testGetAccountAll() throws Exception {
        PaginationAccountResponse response = PaginationAccountResponse.builder()
                .accounts(Collections.singletonList(builderAccount()))
                .page(0)
                .rows(1)
                .total(10)
                .totalPages(1)
                .build();

        when(accountService.getFindAllAccounts(0, 1)).thenReturn(response);

        ResponseEntity<PaginationAccountResponse> result = accountController.getFindAllAccounts(0, 1);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    public void testGetAccountById() throws Exception {
        Long id = 1L;
        AccountResponse response = builderAccountResponse();

        when(accountService.getAccountById(eq(id))).thenReturn(response);

        ResponseEntity<AccountResponse> result = accountController.getAccountById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getAccountsPayReturnOk() throws Exception {
        // Given
        LocalDate startDueDate = LocalDate.of(2024, 1, 1);
        LocalDate endDueDate = LocalDate.of(2024, 12, 31);
        String description = "Test Description";
        int page = 0;
        int size = 10;

        PaginationAccountResponse response = PaginationAccountResponse.builder()
                .accounts(Collections.singletonList(builderAccount()))
                .page(0)
                .rows(1)
                .total(10)
                .totalPages(1)
                .build();
        // Configure o paginationAccountResponse conforme necessário

        when(accountService.getAccountsPay(eq(startDueDate), eq(endDueDate), eq(description), eq(page), eq(size)))
                .thenReturn(response);

        // When
        ResponseEntity<PaginationAccountResponse> responseEntity = accountController.getAccountsPay(
                startDueDate, endDueDate, description, page, size
        );

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(response, responseEntity.getBody());

        // Verify
        verify(accountService).getAccountsPay(eq(startDueDate), eq(endDueDate), eq(description), eq(page), eq(size));
    }

    @Test
    void updateSituationAccountShouldReturnOk() throws Exception {
        // Given
        String id = "1";
        AccountSituationRequest request = AccountSituationRequest.builder()
                .situation(Situation.CANCELLED).build();

        AccountResponse accountResponse = builderAccountResponse();


        when(accountService.updateSituationAccount(any(AccountSituationRequest.class), any(Long.class)))
                .thenReturn(accountResponse);


        ResponseEntity<AccountResponse> responseEntity = accountController.updateSituationAccount(id, request);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(accountResponse, responseEntity.getBody());

        // Verify
        verify(accountService).updateSituationAccount(any(AccountSituationRequest.class), any(Long.class));
    }

    @Test
    void batchAccountsReturnSucess() throws Exception {
        // Given
        MultipartFile file = createMockMultipartFile();
        MessageResponse messageResponse = new MessageResponse(200, "sucess");
        // Configure o messageResponse conforme necessário

        when(accountService.batchAccounts(any(MultipartFile.class)))
                .thenReturn(messageResponse);

        // When
        ResponseEntity<MessageResponse> responseEntity = accountController.batchAccounts(file);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(messageResponse, responseEntity.getBody());

        // Verify
        verify(accountService).batchAccounts(any(MultipartFile.class));
    }

    private MultipartFile createMockMultipartFile() throws IOException {
        String fileContent = "test content";
        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
        return new MockMultipartFile("file", "test.txt", "text/plain", inputStream);
    }
}
