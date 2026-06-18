package mn.astvision.ard.api;

import mn.astvision.ard.api.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import mn.astvision.ard.data.Account;
import mn.astvision.ard.service.AccountService;


@RestController
@RequestMapping("/v1/account")
public class AccountApi {
    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public Account create(@RequestBody Account account) {
        return accountService.save(account);
    }

    @GetMapping("/all")
    public Page<Account> getAllUser(Pageable pageable) {
        return accountService.getAllAcc(pageable);
    }

    //account Read By userID
    @GetMapping("by-id")
    public Account getById(@RequestBody Account account) {
        return accountService.getById(account.getUserId());
    }
    ////account read By Account Number
    @GetMapping("/by-acc")
    public Account getByAccNu(@RequestBody Account account){
        return accountService.getByAccNum(account.getAccountNumber());
    }

    @PutMapping
    public Account update(@RequestBody Account account) {
        return accountService.update(account);
    }
    //account delete By Account Number
    @DeleteMapping
    public void delete(@RequestBody Account account) {
        accountService.delete(account.getAccountNumber());
    }

}
