package mn.astvision.ard.service;

import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.data.Sequence;
import mn.astvision.ard.data.User;
import mn.astvision.ard.enums.AccountCategory;
import mn.astvision.ard.repo.SequenceRepository;
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


@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SequenceRepository sequenceRepository;

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
    //accoundnum generate
    public String generateAccountNumber(AccountCategory type) {
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
        Sequence AccountSerial = sequenceRepository.findById("account_seq").orElseGet(()->buildSequence());
        Integer sequenceNum = AccountSerial.getSequenceNum();

        AccountSerial.setSequenceNum(sequenceNum+1);
        sequenceRepository.save(AccountSerial);

        String accNum = bankId+accTypeNum+sequenceNum;
        return accNum ;
    }
    //өгөгдлийн сангаас document олдохгүй бол шинээр үүсгэх
    private Sequence buildSequence() {
        Sequence sequence = Sequence.builder()
                .sequenceId("account_seq")
                .sequenceNum(100000)
                .build();
        sequenceRepository.save(sequence);
        return sequenceRepository.findById("account_seq").orElseThrow(IllegalStateException::new);
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
        boolean isExists = accountRepository.existsByUserId(userId);

        if(!isExists)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"account doesn't exist");

        Optional<Account> byId = accountRepository.findByUserId(userId);
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

