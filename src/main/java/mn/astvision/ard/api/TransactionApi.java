package mn.astvision.ard.api;

import mn.astvision.ard.api.dto.TransactionRequest;
import mn.astvision.ard.data.Transaction;
import mn.astvision.ard.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/transaction")
public class TransactionApi {
    @Autowired
    private TransactionService transactionService;

    //шилжүүлэг хийх
    @PostMapping
    public Transaction transferCreate(@RequestBody TransactionRequest transaction){
        return transactionService.transferCreate(transaction);
    }
    //тухайн данснаас хийсэн бүх гүйлгээний хуулга авах
    @GetMapping
    public Page<Transaction> transactionsList(@RequestBody String accountNumber){
        return transactionService.transactionList(accountNumber);
    }
    //тухайн данснаас сүүлд хийгдсэн хуулга авах
    @GetMapping
    public  Transaction transactionLast(@RequestBody String accountNumber){
        return transactionService.transactionLast(accountNumber);
    }

}
