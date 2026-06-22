package mn.astvision.ard.service;

import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.data.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class BalanceService {

    public void validate(String acc1, String acc2) {
        //TODD: validate account
        return;
    }

    public boolean isCredit(String acc, Transaction transaction) {
        String fromAccountNumber = transaction.getFromAccountNumber();
        return fromAccountNumber.equals(acc);
    }


    public boolean isDebit(String acc, Transaction transaction) {
        String toAccountNumber =transaction.getToAccountNumber();
        return toAccountNumber.equals(acc);
    }

    public BigDecimal calculate(String account, List<Transaction> transactions) {
        BigDecimal currentBalance = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            boolean credit = isCredit(account, transaction);
            if(credit){//надад мөнгө орсон
                currentBalance = currentBalance.subtract(transaction.getAmount());
                log.info("нэмэгдсэн дүн нь {} бас одоогийн мөнгө нь {}",transaction.getAmount(),currentBalance);
            }
            boolean debit = isDebit(account, transaction);
            if(debit) {//надаас мөнгө гарсан
                currentBalance = nz(currentBalance).add(nz(transaction.getAmount()));
                log.info("хасагдсан дүн нь {} ба одоогийн дүн нь {}",transaction.getAmount(),currentBalance);
            }
        }

        return currentBalance;
    }


    public BigDecimal nz(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

}
