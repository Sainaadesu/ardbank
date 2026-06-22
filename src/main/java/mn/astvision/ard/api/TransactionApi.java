package mn.astvision.ard.api;

import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.service.BalanceService;
import mn.astvision.ard.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/v1/transaction")
public class TransactionApi {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BalanceService balanceService;

    //шилжүүлэг хийх
    @PostMapping
    public Transaction transferCreate(@RequestBody TransferRequest transaction){
        return transactionService.transferCreate(transaction);
    }
    //тухайн данснаас хийсэн бүх гүйлгээний хуулга авах
    @GetMapping("/list/{accountId}")
    public List<Transaction> transactionsList(@PathVariable String accountId){
        return transactionService.transactionList(accountId);
    }
    //тухайн данснаас сүүлд хийгдсэн хуулга авах
    @GetMapping("/last/{accountNumber}")
    public  Transaction transactionLast(@PathVariable String accountNumber){
        return transactionService.transactionLast(accountNumber);
    }
    @GetMapping("/inOut/{accountNumber}")
    public BigDecimal inOut(@PathVariable String accountNumber){
        return transactionService.inOut(accountNumber);
    }
}
