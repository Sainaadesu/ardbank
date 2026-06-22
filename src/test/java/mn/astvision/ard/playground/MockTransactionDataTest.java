package mn.astvision.ard.playground;

import mn.astvision.ard.api.dto.TransferRequest;
import mn.astvision.ard.enums.TransactionType;
import mn.astvision.ard.service.TransactionService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@SpringBootTest
public class MockTransactionDataTest {
    @Autowired
    private TransactionService transactionService;

    @Test
    @Disabled
    void mockData() {
        Random random = new Random();
        List<String> fromAccountNumber = Arrays.asList("3611000016", "3611000015", "3621000014", "3621000013", "3621000012", "3611000011", "3611000010", "3611000009", "3611000008", "3611000007", "3621000000");
        List<String> toAccountNumber = Arrays.asList("3611000016", "3611000015", "3621000014", "3621000013", "3621000012", "3611000011", "3611000010", "3611000009", "3611000008", "3611000007", "3621000000");

        TransactionType[] type = TransactionType.values();
        List<String> description = Arrays.asList(
                "Хоол хүнсний төлбөр",
                "Онлайн худалдаа",
                "Цалин шилжүүлэг",
                "Тог цахилгааны төлбөр",
                "Утасны нэгж цэнэглэлт",
                "Банкны үйлчилгээний шимтгэл",
                "Зээлийн эргэн төлөлт",
                "Бэлэгний шилжүүлэг",
                "Данс хоорондын шилжүүлэг",
                "Бэлэн мөнгө авах",
                "Интернет төлбөр",
                "Ус дулааны төлбөр",
                "Кафе, ресторан төлбөр",
                "Дэлгүүрийн худалдан авалт",
                "Такси үйлчилгээний төлбөр",
                "Тээврийн карт цэнэглэлт",
                "Даатгалын төлбөр",
                "Татварын төлбөр",
                "Тогтмол үйлчилгээний хураамж",
                "Сургалтын төлбөр",
                "Найз руу шилжүүлэг",
                "Гэр бүлийн дэмжлэг",
                "Гар утасны дата багц",
                "Онлайн тоглоом худалдан авалт",
                "Аппликейшн худалдан авалт",
                "Валютын шилжүүлэг",
                "Кредит картын төлбөр",
                "Онлайн захиалга",
                "Банк хоорондын шилжүүлэг",
                "Ажил хэргийн төлбөр",
                "Үйлчилгээний төлбөр"
        );
        fromAccountNumber.forEach(l ->{
            toAccountNumber.forEach(i->{
                if( l.equals(i)){
                    return;
                }
                int a = random.nextInt(1000)+100;
                TransactionType s = type[random.nextInt(type.length)];
                String d = description.get(random.nextInt(description.size()));
                BigDecimal amount = new BigDecimal(String.valueOf(a));

                TransferRequest transfer = TransferRequest.builder()
                        .fromAccountNumber(l)
                        .toAccountNumber(i)
                        .amount(amount)
                        .type(s)
                        .description(d)
                        .build();
                transactionService.transferCreate(transfer);
            });
        });

    }

}
