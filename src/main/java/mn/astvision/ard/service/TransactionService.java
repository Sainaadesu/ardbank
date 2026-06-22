package mn.astvision.ard.service;

import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.data.Account;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.repo.AccountRepository;
import mn.astvision.ard.repo.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransferService transferService;
    @Autowired
    private BalanceService balanceService;

    //Шилжүүлэг хийх
    public Transaction transferCreate(TransferRequest transaction) {
        boolean fromAccExist = accountRepository.existsByAccountNumber(transaction.fromAccountNumber());
        if(!fromAccExist){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account doesn't exist 1");
        }
        boolean toAccExist = accountRepository.existsByAccountNumber(transaction.toAccountNumber());
        if(!toAccExist){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account doesn't exist 2");
        }
        return transferService.transfer(transaction);
    }
    //Сүүлд хийсэн хуулга харах
    public Transaction transactionLast(String accountId) {
        List<Transaction> transactions = transferService.history(accountId);
        return transactions.getFirst();
    }
    //орлого зарлагын нийлбэрийг олох
    public BigDecimal inOut(String accountId) {
        List<Transaction> transactions = transferService.history(accountId);
        return balanceService.calculate(accountId, transactions );
    }
}
