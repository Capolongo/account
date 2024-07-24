package br.com.account.controller;

import br.com.account.dto.request.AccountRequest;
import br.com.account.dto.request.AccountSituationRequest;
import br.com.account.dto.response.AccountResponse;
import br.com.account.dto.response.MessageResponse;
import br.com.account.dto.response.PaginationAccountResponse;
import br.com.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest request) throws Exception {
        log.info("AccountController.createAccount() - Start - request: [{}]", request);
        var response = accountService.createAccount(request);
        log.info("AccountController.createAccount() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable("id") Long id, @RequestBody AccountRequest request) throws Exception {
        log.info("AccountController.updateAccount() - Start - request: [{}]", request);
        var response = accountService.updateAccount(request, id);
        log.info("AccountController.updateAccount() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PatchMapping("/update/situation/{id}")
    public ResponseEntity<AccountResponse> updateSituationAccount(@PathVariable("id") String id, @RequestBody AccountSituationRequest request) throws Exception {
        log.info("AccountController.updateSituationAccount() - Start - request: [{}]", request);
        var response = accountService.updateSituationAccount(request, 1L);
        log.info("AccountController.updateSituationAccount() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable("id") Long id) throws Exception {
        log.info("AccountController.getAccountById() - Start - id: [{}]", id);
        var response = accountService.getAccountById(id);
        log.info("AccountController.getAccountById() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @GetMapping("/accounts-pay")
    public ResponseEntity<PaginationAccountResponse> getAccountsPay(
            @RequestParam LocalDate startDueDate,
            @RequestParam LocalDate endDueDate,
            @RequestParam(required = false) String description,
            @RequestParam int page,
            @RequestParam int size) throws Exception {
        log.info("AccountController.getAccountsPay() - Start - startDueDate: [{}], endDueDate: [{}], description: [{}], page: [{}],size: [{}]", startDueDate, endDueDate, description, page, size);
        var response = accountService.getAccountsPay(startDueDate, endDueDate, description, page, size);
        log.info("AccountController.getAccountsPay() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @GetMapping()
    public ResponseEntity<PaginationAccountResponse> getFindAllAccounts(
            @RequestParam int page,
            @RequestParam int size) throws Exception {
        log.info("AccountController.getFindAllAccounts() - Start - page: [{}],size: [{}]", page, size);
        var response = accountService.getFindAllAccounts(page, size);
        log.info("AccountController.getAccountsPay() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PostMapping("/batch-accounts")
    public ResponseEntity<MessageResponse> batchAccounts(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("AccountController.batchAccounts() - Start - file: [{}]", file);
        var response = accountService.batchAccounts(file);
        log.info("AccountController.batchAccounts() - End - response: [{}]", response);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }
}
