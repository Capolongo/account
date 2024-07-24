package br.com.account.mapper;

import br.com.account.dto.AccountDTO;
import br.com.account.dto.response.AccountResponse;
import br.com.account.entities.ContaEntity;
import br.com.account.enuns.Situation;
import br.com.account.mappers.AccountMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static br.com.account.mockBuilder.AccountMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountMapperTest {

    AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Test
    public void testAccountRequestToContaCreateSucess() {
        ContaEntity conta = accountMapper.accountRequestToContaCreate(builderAccountRequest(), Situation.COMPLETED.getDescription());
        assertNotNull(conta);
    }

    @Test
    public void testAccountRequestToContaUpdateSucess() {
        ContaEntity response = accountMapper.accountRequestToContaUpdate(builderAccountRequest(), builderContaEntity(), 1L);
        assertNotNull(response);
    }

    @Test
    public void testContaEntityToAccountResponseSucess() {
        AccountResponse accountResponse = accountMapper.contaEntityToAccountResponse(builderContaEntity());
        assertNotNull(accountResponse);
    }

    @Test
    public void testContaToAccountSucess() {
        AccountDTO accountDTO = accountMapper.contaEntityToAccountDTO(builderContaEntity());
        assertNotNull(accountDTO);
    }
}
