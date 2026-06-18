package mn.astvision.ard.service;

import mn.astvision.ard.data.User;
import mn.astvision.ard.enums.AccountCategory;
import mn.astvision.ard.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import mn.astvision.ard.data.Account;
import mn.astvision.ard.repo.AccountRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Random;


@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    //CRUD
    //Create
    public Account save(Account account) {
        if(account.getUserId() == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account does not found");
        }
        boolean isUserExits = userRepository.existsById(account.getUserId());
        if(!isUserExits ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account does not exist");
        }
        String generated = generateAccountNumber(account.getAccountCategory());
        account.setAccountNumber(generated);
        return accountRepository.save(account);
    }
    //accoundnum gener
    private String generateAccountNumber(AccountCategory type) {
        String bankId = "36";
        String accTypeNum = "";
        switch (type){
            case AccountCategory.Saving -> {
                accTypeNum="1";
            }
            case  AccountCategory.Checking ->{
                accTypeNum="2";
            }
            case AccountCategory.Term_Deposit -> {
                accTypeNum = "3";
            }
            case AccountCategory.Demand_Deposit -> {
                accTypeNum = "4";
            }
        }
        boolean accSerExists = true;
        String accNum = "";

        while(accSerExists){
            int AccountSerial = new Random().nextInt(9000000) + 1000000;
            accNum = bankId+accTypeNum+AccountSerial;
            accSerExists = accountRepository.existsByAccountNumber(accNum);
        }
        return accNum ;
    }
    //Update
    public Account update(Account account) {
        if(account.getUserId() ==null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account does not found");
        }

        boolean isExists = accountRepository.existsById(account.getUserId());
        if(!isExists){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Account doesn't exists");
        }
        return accountRepository.save(account);
    }
    //Delete
    public void delete(String id) {
        if(id ==null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account not found");
        }

        boolean isExists = accountRepository.existsById(id);
        if(!isExists){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"Account doesn't exists");
        }
        accountRepository.deleteById(id);
    }
    //Read
   public Page<Account> getAllAcc(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Account getById(String userId) {
        boolean isExists = accountRepository.existsById(userId);

        if(!isExists)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Optional<Account> byId = accountRepository.findById(userId);
        return byId.orElseThrow(IllegalStateException::new);
    }

    public Account getByAccNum(String accNu) {
        boolean isExists = accountRepository.existsByAccountNumber(accNu);
        if(!isExists){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Optional<Account> byAccNu = accountRepository.findByAccountNumber(accNu);


        return byAccNu.orElseThrow(IllegalStateException::new);
    }


}

