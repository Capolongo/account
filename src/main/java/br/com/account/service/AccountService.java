package br.com.account.service;

import br.com.account.dto.request.AccountRequest;
import br.com.account.dto.request.AccountSituationRequest;
import br.com.account.dto.response.AccountResponse;
import br.com.account.dto.response.MessageResponse;
import br.com.account.dto.response.PaginationAccountResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface AccountService {
    AccountResponse createAccount(AccountRequest request);
    AccountResponse updateAccount(AccountRequest request, Long id) throws Exception;
    AccountResponse updateSituationAccount(AccountSituationRequest request, Long id) throws Exception;
    AccountResponse getAccountById(Long id) throws Exception;
    PaginationAccountResponse getAccountsPay(LocalDate startDueDate, LocalDate endDueDate,
                                                           String descricao, int page, int size) throws Exception;
    PaginationAccountResponse getFindAllAccounts(int page, int size) throws Exception;
    MessageResponse batchAccounts(MultipartFile file) throws Exception;
}
