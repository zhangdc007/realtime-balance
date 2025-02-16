package com.mybank.balance.transaction;
import com.mybank.balance.transaction.dao.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.mybank.balance.transaction.service.TransactionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest()
public class TransactionServiceTest extends BaseTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private TransactionRepository   transactionDao;


}
