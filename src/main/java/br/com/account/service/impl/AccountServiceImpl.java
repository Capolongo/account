package br.com.account.service.impl;

import br.com.account.dto.request.AccountRequest;
import br.com.account.dto.request.AccountSituationRequest;
import br.com.account.dto.response.AccountResponse;
import br.com.account.dto.response.MessageResponse;
import br.com.account.dto.response.PaginationAccountResponse;
import br.com.account.entities.ContaEntity;
import br.com.account.exception.AccountException;
import br.com.account.mappers.AccountMapper;
import br.com.account.repository.ContaRepository;
import br.com.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static br.com.account.enuns.Situation.PENDING;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final ContaRepository contaRepository;

    private final AccountMapper accountMapper;

    @Override
    public AccountResponse createAccount(AccountRequest request) {
        try {
            log.info("AccountServiceImpl.createAccount() - Start - request: [{}]", request);
            var conta= accountMapper.accountRequestToContaCreate(request, PENDING.getDescription());
            var response = contaRepository.save(conta);
            var responseMapper = accountMapper.contaEntityToAccountResponse(response);
            log.info("AccountServiceImpl.createAccount() - End - response: [{}]", responseMapper);
            return responseMapper;
        } catch (Exception ex) {
            log.error("AccountServiceImpl.createAccount() - Error", ex);
            throw new AccountException("Error create account");
        }
    }

    @Override
    public AccountResponse updateAccount(AccountRequest request, Long id) throws Exception {
        try {
            log.info("AccountServiceImpl.updateAccount() - Start - request: [{}], id: [{}]", request, id);
            Optional<ContaEntity> conta = contaRepository.findById(id);

            if(conta.isEmpty()) {
                throw new AccountException("Not Found Id for update payment");
            }

            var pagamento= accountMapper.accountRequestToContaUpdate(request, conta.get(), id);
            var response = contaRepository.save(pagamento);
            var responseMapper = accountMapper.contaEntityToAccountResponse(response);
            log.info("AccountServiceImpl.updateAccount() - End - response: [{}]", responseMapper);
            return responseMapper;
        }  catch (AccountException ex) {
            log.error("AccountServiceImpl.updateAccount() - Error AccountException");
            throw ex;
        } catch (Exception ex) {
            log.error("AccountServiceImpl.updateAccount() - Error", ex);
            throw new Exception("Error updateAccount");
        }
    }

    @Override
    public AccountResponse updateSituationAccount(AccountSituationRequest request, Long id) throws Exception {
        try {
            log.info("AccountServiceImpl.updateSituationAccount() - Start - request: [{}], id: [{}]", request, id);
            Optional<ContaEntity> beneficiario = contaRepository.findById(id);

            if(beneficiario.isEmpty()) {
                throw new AccountException("Not Found id account");
            }

            var pagamento= accountMapper.accountSituationRequestToContaUpdate(beneficiario.get(), request.getSituation().getDescription(), id);
            var response = contaRepository.save(pagamento);
            var responseMapper = accountMapper.contaEntityToAccountResponse(response);
            log.info("AccountServiceImpl.updateSituationAccount() - End - response: [{}]", responseMapper);
            return responseMapper;
        }  catch (AccountException ex) {
            log.error("AccountServiceImpl.updateSituationAccount() - Error AccountException");
            throw ex;
        } catch (Exception ex) {
            log.error("AccountServiceImpl.updateSituationAccount() - Error", ex);
            throw new Exception("Error update situation account");
        }
    }

    @Override
    public AccountResponse getAccountById(Long id) throws Exception {
        try {
            log.info("AccountServiceImpl.getAccountById() - Start - id: [{}]", id);
            var response = contaRepository.findById(id);

            if(response.isEmpty()) {
                throw new AccountException("Not Found By Id account");
            }

            var responseMapper = accountMapper.contaEntityToAccountResponse(response.get());
            log.info("AccountServiceImpl.getAccountById() - End - response: [{}]", responseMapper);
            return responseMapper;
        }  catch (AccountException ex) {
            log.error("AccountServiceImpl.getAccountById() - Error AccountException");
            throw ex;
        } catch (Exception ex) {
            log.error("AccountServiceImpl.getAccountById() - Error", ex);
            throw new Exception("Error get By account");
        }
    }

    @Override
    public PaginationAccountResponse getFindAllAccounts(int page, int size) throws Exception {
        try {
            log.info("AccountServiceImpl.getAccountsAll() - Start - page: [{}], size: [{}]", page, size);

            Page<ContaEntity> responsePage = contaRepository.findAll(PageRequest.of(page, size));

            if(responsePage.isEmpty()) {
                throw new AccountException("Not Found find all accounts");
            }

            var response = accountMapper.contaEntityToPaginationAccountResponse(responsePage);

            log.info("AccountServiceImpl.getFindAllAccounts() - End - response: [{}]", response);
            return response;
        }  catch (AccountException ex) {
            log.error("AccountServiceImpl.getFindAllAccounts() - Error AccountException");
            throw ex;
        } catch (Exception ex) {
            log.error("AccountServiceImpl.getFindAllAccounts() - Error", ex);
            throw new Exception("Error get find all Accounts");
        }
    }

    @Override
    public PaginationAccountResponse getAccountsPay(LocalDate startDueDate, LocalDate endDueDate, String descripition, int page, int size) throws Exception {
        try {
            log.info("AccountServiceImpl.getAccountsPay() - Start - startDueDate: [{}], endDueDate: [{}], descripition: [{}], page: [{}], size: [{}]", startDueDate, endDueDate,descripition, page, size);

            Page<ContaEntity> responsePage = contaRepository.findByDataVencimentoBetweenOrDescricao(startDueDate, endDueDate, descripition, PageRequest.of(page, size));

            if(responsePage.isEmpty()) {
                throw new AccountException("Not Found By Accounts Pay");
            }
            var response = accountMapper.contaEntityToPaginationAccountResponse(responsePage);

            log.info("AccountServiceImpl.getAccountsPay() - End - response: [{}]", response);
            return response;
        }  catch (AccountException ex) {
            log.error("AccountServiceImpl.getAccountsPay() - Error AccountException");
            throw ex;
        } catch (Exception ex) {
            log.error("AccountServiceImpl.getAccountsPay() - Error", ex);
            throw new Exception("Error get By Accounts Pay");
        }
    }

    @Override
    public MessageResponse batchAccounts(MultipartFile file) throws Exception {
        try {
            log.info("AccountServiceImpl.batchAccounts() - Start");

            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (CSVRecord record : csvParser) {
                ContaEntity conta = new ContaEntity();
                conta.setDescricao(record.get("descricao"));
                conta.setValor(new BigDecimal(record.get("valor")));
                conta.setSituacao(PENDING.getDescription());
                conta.setDataPagamento(LocalDate.parse(record.get("dataPagamento"), formatter));
                conta.setDataVencimento(LocalDate.parse(record.get("dataVencimento"), formatter));
                contaRepository.save(conta);
            }
            return new MessageResponse(HttpStatus.OK.value(), "Lote Sucess");
        }  catch (Exception ex) {
            log.error("AccountServiceImpl.batchAccounts() - Error", ex);
            throw new Exception("Error batch accounts");
        }
    }
}
