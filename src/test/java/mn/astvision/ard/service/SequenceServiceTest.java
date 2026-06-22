package mn.astvision.ard.service;

import lombok.extern.slf4j.Slf4j;
import mn.astvision.ard.enums.AccountCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
class SequenceServiceTest {

    @Autowired
    private AccountService accountService;


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void sequenceInc() {
    }

    @Test
    void isRun(){
//        List<Long> myList = new ArrayList<>();
//        int n = 0;
//        while(n < 100){
//            n++;
//            Long result = Long.parseLong(accountService.generateAccountNumber(AccountCategory.Checking));
//            myList.add(result);
//        }
//        myList.forEach(l ->{
//            log.info(String.valueOf(l));
//        });
        Long result = Long.parseLong(accountService.generateAccountNumber(AccountCategory.Checking));
        assertEquals(3621000001L, result);
    }


//    @Test
//    void isRun2(){
//        log.info("gegegegegeg");
//
//        List<Long> nyList = new ArrayList<>();
//        int n = 0;
//        while(n < 100){
//            n++;
//            nyList.add(underTest.sequenceInc());
//        }
//
//        nyList.forEach(l ->{
//            log.info(String.valueOf(l));
//        });
//    }
}