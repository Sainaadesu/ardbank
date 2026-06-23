package mn.astvision.ard.service;

import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.data.Account;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.repo.AccountRepository;
import mn.astvision.ard.repo.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
        Pattern accTypePattern = Pattern.compile("^..3.*");
        Matcher accIsTermDeposit = accTypePattern.matcher(transaction.fromAccountNumber());
        log.info("тухайн дасны төрлийг шалгаад: {}",accIsTermDeposit.matches());
        if(accIsTermDeposit.matches()){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Account type is term deposit");
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
    //тодорхой хугацаанд хийгдсэн хуулга авах
    public List<Transaction> getByDate(LocalDate startDate, LocalDate endDate, String accountId) {
        if(startDate.isAfter(endDate)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"date");
        }
        List<Transaction> transactions = transferService.history(accountId);
        log.info("энэ бол ажиллаж байна");
        List<Transaction> result =filterByRange(startDate, endDate, transactions);
        return result;
    }
    private List<Transaction> filterByRange(LocalDate startDate, LocalDate endDate, List<Transaction> transactions){
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23,59,59);
        log.info("эхдэх өдөр нь {} дуусах өдөр нь {}",start,end);
        List<Transaction> filtered = new java.util.ArrayList<>();
        for (Transaction transaction : transactions) {
            LocalDateTime completedAt = transaction.getCompletedAt();
            if(!completedAt.isBefore(start) && !completedAt.isAfter(end)){
                log.info("нөхцөл биеллэ");
                filtered.add(transaction);
            }
            else {

                log.info("Нөхцөл нь биелэхгүй байна. тухайн хуулгын өдөр нь {}",completedAt);
            }
        }
        return filtered;
    }
}
