package mn.astvision.ard.service;

import mn.astvision.ard.api.dto.RegisterRequest;
import mn.astvision.ard.api.dto.TransactionRequest;
import mn.astvision.ard.data.Account;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.repo.AccountRepository;
import mn.astvision.ard.repo.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;

    //Шилжүүлэг хийх
    public Transaction transferCreate(TransactionRequest transaction) {
        boolean fromAccExist = transactionRepository.existsById(transaction.fromAccount());
        if(!fromAccExist){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account doesn't exist 1");
        }
        boolean toAccExist = transactionRepository.existsById(transaction.toAccount());
        if(!toAccExist){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account doesn't exist 2");
        }
        Account fromAccount = accountRepository.findByAccountNumber(transaction.fromAccount()).orElseThrow(IllegalAccessError::new);
        BigDecimal remain = fromAccount.getCurrentBalance().subtract(transaction.Amount());
        if(remain)
        return null;
    }
    //Бүх хуулга харах
    public Page<Transaction> transactionList(String accountNumber) {
        return null;
    }
    //Сүүлд хийсэн хуулга харах
    public Transaction transactionLast(String accountNumber) {
        return null;
    }
}
