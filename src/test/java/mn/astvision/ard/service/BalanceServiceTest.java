package mn.astvision.ard.service;

import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.repo.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Slf4j
class BalanceServiceTest {
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private TransactionRepository transactionRepository;
    @Test
    void run(){
        BigDecimal result = balanceService.calculate("6a38bf402bfd18e27391beba",transactionRepository.findAll());
        log.info("test-ийн үр дүн {}",result.toString());
    }

}