package com.mybank.balance.transaction;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.mybank.balance.transaction.model.Page;
import java.util.List;
import com.mybank.balance.transaction.service.TransactionService;
import com.mybank.balance.transaction.dao.TransactionDao;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest()
public class TransactionServiceTest extends BaseTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private TransactionDao transactionDao;

    @Test
    public void testAddTransaction() {
        Transaction transaction = generateRandomTransaction(null);
        Transaction result = transactionService.addTransaction(transaction);
        Assertions.assertNotNull(result);
        Mockito.verify(transactionDao, Mockito.times(1)).addTransaction(transaction);
    }

    @Test
    public void testDeleteTransaction() {
        Transaction transaction = generateRandomTransaction("123");
        Transaction result = transactionService.addTransaction(transaction);
        String bizId = "123";
        transactionService.deleteTransaction(bizId);
        Mockito.verify(transactionDao, Mockito.times(1)).deleteTransactionByBizId(bizId);
    }

    @Test
    public void testUpdateTransaction() {
        //初始化数据
        Transaction defaultData = generateRandomTransaction("456");
        Transaction add = transactionService.addTransaction(defaultData);
        //测试
        Transaction updatedTransaction = generateRandomTransaction("456");
        Transaction result = transactionService.updateTransaction(updatedTransaction);
        Mockito.verify(transactionDao, Mockito.times(1)).updateTransaction(updatedTransaction.bizId(), updatedTransaction);
    }

    @Test
    public void testUpdateTransactionWithBlankBizId() {
        Transaction blank = generateRandomTransaction("");
        Assertions.assertThrows(BizException.class, () -> {
            transactionService.updateTransaction(blank);
        });
    }

    @Test
    public void testGetPageTransactions() {
        fullfillTransaction(1000);
        Integer page = 1;
        Integer size = 10;
        String orderBy = "id";
        String sort = "asc";
        Page<Transaction> pageTransactions = transactionService.getPageTransactions(page, size, orderBy, sort);
        Assertions.assertNull(pageTransactions);
    }

    private int fullfillTransaction(int size) {
        for(int i=0;i<size;i++)
        {
            transactionService.addTransaction(generateRandomTransaction(null));
        }
        return size;
    }

    @Test
    public void testGetAllTransactions() {
        fullfillTransaction(1000);
        String orderBy = "amount";
        String sort = "desc";
        List<Transaction> allTransactions = transactionService.getAllTransactions(orderBy, sort);
        Assertions.assertNull(allTransactions);
    }

}
