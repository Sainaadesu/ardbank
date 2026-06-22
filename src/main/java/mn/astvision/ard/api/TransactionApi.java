package mn.astvision.ard.api;

import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.service.BalanceService;
import mn.astvision.ard.service.TransactionService;
import mn.astvision.ard.service.TransferService;
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
    @Autowired
    private TransferService transferService;

    //шилжүүлэг хийх
    @PostMapping
    public Transaction transferCreate(@RequestBody TransferRequest transaction){
        return transactionService.transferCreate(transaction);
    }
    //тухайн данснаас хийсэн бүх гүйлгээний хуулга авах
    @GetMapping("/list/{accountId}")
    public List<Transaction> transactionsList(@PathVariable String accountId){
        return transferService.history(accountId);
    }
    //тухайн данснаас accountId-гаар нь сүүлд хийгдсэн хуулга авах
    @GetMapping("/last/{accountId}")
    public  Transaction transactionLast(@PathVariable String accountId){
        return transactionService.transactionLast(accountId);
    }
    //account-ийн id-гаар нь орлого зарлагын нийлбэр олох
    @GetMapping("/inOutByAccId/{accountId}")
    public BigDecimal inOut(@PathVariable String accountId){
        return transactionService.inOut(accountId);
    }
}
