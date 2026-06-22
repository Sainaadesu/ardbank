package mn.astvision.ard.service;

import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.data.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class BalanceService {
    //орлого зарлагын нийлбэрийг олох
    public BigDecimal calculate(String accountId, List<Transaction> transactions) {
        BigDecimal currentBalance = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            boolean credit = isCredit(accountId, transaction);
            if(credit){//надад мөнгө орсон
                currentBalance = currentBalance.subtract(transaction.getAmount());
                log.info("нэмэгдсэн дүн нь {} бас одоогийн мөнгө нь {}",transaction.getAmount(),currentBalance);
            }
            boolean debit = isDebit(accountId, transaction);
            if(debit) {//надаас мөнгө гарсан
                currentBalance = nz(currentBalance).add(nz(transaction.getAmount()));
                log.info("хасагдсан дүн нь {} ба одоогийн дүн нь {}",transaction.getAmount(),currentBalance);
            }
        }
        return currentBalance;
    }
    //тухайн шилжүүлгийг орлог зарлаг эсхийг нь тооцох
    public boolean isCredit(String accId, Transaction transaction) {
        String fromAccountNumber = transaction.getFromAccountId();
        return fromAccountNumber.equals(accId);
    }


    public boolean isDebit(String accId, Transaction transaction) {
        String toAccountNumber =transaction.getToAccountId();
        return toAccountNumber.equals(accId);
    }
    //жижиг сажиг функц
    public void validate(String acc1, String acc2) {
        //TODD: validate account
        return;
    }
    public BigDecimal nz(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}
