package br.com.account.dto.response;

import br.com.account.dto.AccountDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginationAccountResponse {
  private List<AccountDTO> accounts;
  private int page;
  private int rows;
  private int total;
  private int totalPages;
}