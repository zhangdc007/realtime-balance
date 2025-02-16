package com.mybank.balance.transaction;

import com.mybank.balance.transaction.dao.TransactionDao;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.model.Page;
import com.mybank.balance.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
public class TransactionDaoTest extends BaseTest {

    @Autowired
    private TransactionDao transactionDao;

    @Test
    public void testAddTransaction() {

        // 创建测试用的 Transaction 对象
        Transaction transaction = generateRandomTransaction(null);

        // 调用 addTransaction 方法添加交易记录
        Transaction addedTransaction = transactionDao.addTransaction(transaction);

        // 验证添加成功后返回的交易记录与传入的交易记录相同
        Assertions.assertEquals(transaction, addedTransaction);

        // 验证交易记录已添加到 transIdMap 和 transBizIdMap 中
        Assertions.assertTrue(transactionDao.getTransBizIdMap().containsKey(transaction.bizId()));
    }

    @Test
    public void testAddTransactionDuplicate() {
        // 创建测试用的 Transaction 对象
        Transaction transaction = generateRandomTransaction(null);

        // 第一次添加
        transactionDao.addTransaction(transaction);
        // 再次添加相同的事务，应抛出异常
        Assertions.assertThrows(BizException.class, () -> {
            transactionDao.addTransaction(transaction);
        });
    }

    @Test
    public void testGetTransBizIdMap() {
        // 创建测试用的 Transaction 对象并添加到 TransactionDao 中
        Transaction transaction = generateRandomTransaction(null);
        transaction = transactionDao.addTransaction(transaction);
        Assertions.assertSame(transactionDao.getTransaction(transaction.bizId()),transaction);
    }

    @Test
    public void testDeleteTransaction() {

        // 创建测试用的 Transaction 对象并添加到 TransactionDao 中
        Transaction transaction = generateRandomTransaction(null);
        transaction = transactionDao.addTransaction(transaction);

        // 调用 deleteTransaction 方法删除交易记录
        boolean result = transactionDao.deleteTransaction(transaction);

        // 验证删除成功
        Assertions.assertTrue(result);

        // 验证交易记录已从 transIdMap 和 transBizIdMap 中删除
        Assertions.assertFalse(transactionDao.getTransBizIdMap().containsKey(transaction.bizId()));
    }

    @Test
    public void testDeleteTransactionById() {

        // 创建测试用的 Transaction 对象并添加到 TransactionDao 中
        Transaction transaction = generateRandomTransaction(null);
        transactionDao.addTransaction(transaction);

        // 调用 deleteTransactionById 方法删除交易记录
        boolean result = transactionDao.deleteTransactionById(transaction.id());

        // 验证删除成功
        Assertions.assertTrue(result);

        // 验证交易记录已从 transIdMap 中删除
        Assertions.assertFalse(transactionDao.getTransIdMap().containsKey(transaction.id()));
    }

    @Test
    public void testDeleteTransactionByBizId() {

        // 创建测试用的 Transaction 对象并添加到 TransactionDao 中
        Transaction transaction = generateRandomTransaction(null);
        transactionDao.addTransaction(transaction);

        // 调用 deleteTransactionByBizId 方法删除交易记录
        boolean result = transactionDao.deleteTransactionByBizId(transaction.bizId());

        // 验证删除成功
        Assertions.assertTrue(result);

        // 验证交易记录已从 transBizIdMap 中删除
        Assertions.assertFalse(transactionDao.getTransBizIdMap().containsKey(transaction.bizId()));
    }

    @Test
    public void testUpdateTransaction() {
        Transaction transaction = generateRandomTransaction(null);

        transactionDao.addTransaction(transaction);
        // 创建更新后的 Transaction 对象
        Transaction updatedTransaction = generateRandomTransaction(transaction.bizId(),transaction.id());

        // 调用 updateTransaction 方法更新交易记录
        Transaction updated = transactionDao.updateTransaction(transaction.bizId(), updatedTransaction);

        // 验证更新成功后返回的交易记录与更新后的交易记录相同
        Assertions.assertEquals(updatedTransaction, updated);

        // 验证交易记录已在 transBizIdMap 和 transIdMap 中更新
        Assertions.assertEquals(updatedTransaction, transactionDao.getTransBizIdMap().get(transaction.bizId()));
        Assertions.assertEquals(updatedTransaction, transactionDao.getTransIdMap().get(transaction.id()));
    }

    private List<Transaction> fullfillTransaction(int size) {
        List<Transaction> data = new ArrayList<>();
        //清理数据
        transactionDao.getTransIdMap().clear();
        transactionDao.getTransBizIdMap().clear();
        for(int i=0;i<size;i++)
        {
            data.add(transactionDao.addTransaction(generateRandomTransaction(null)));
        }
        return data;
    }
    @Test
    public void testGetPageTransactionsAll() {
        int total = 100,pageSize = 10,pageNo = 5;
        List<Transaction> data = fullfillTransaction(total);

        // 调用 getPageTransactions 方法获取所有交易记录
        Page<Transaction> page = transactionDao.getPageTransactions(true, 0, pageSize, "amount", "asc");

        // 验证获取到的交易记录数量与添加的交易记录数量相同
        Assertions.assertEquals(total, page.totalNum());

        // 验证获取到的交易记录amount顺序
        List<Transaction> transactions = page.data();
        Assertions.assertTrue(transactions.get(0).amount().compareTo(transactions.get(2).amount()) == -1);
    }

    @Test
    public void testGetPageTransactionsPaged() {
        int total = 100,pageSize = 10,pageNo = 5;
        List<Transaction> data = fullfillTransaction(total);

        // 调用 getPageTransactions 方法获取第一页交易记录，每页大小为 2
        Page<Transaction> page = transactionDao.getPageTransactions(false, pageNo, pageSize, "amount", "desc");

        // 验证获取到的交易记录数量
        Assertions.assertEquals(pageSize, page.data().size());

        // 验证获取到的交易记录顺序与
        List<Transaction> transactions = page.data();
        Assertions.assertTrue(transactions.get(0).amount().compareTo(transactions.get(2).amount()) == 1);
    }

    @Test
    public void testGetPageTransactionsInvalidOrderBy() {
        int total = 100,pageSize = 10,pageNo = 5;
        List<Transaction> data = fullfillTransaction(100);

        // 调用 getPageTransactions 方法，传入无效的排序字段
        Page<Transaction> page = transactionDao.getPageTransactions(false, 1, 10, "invalidOrderBy", "asc");

        // 验证获取到的交易记录数量
        Assertions.assertEquals(pageSize, page.data().size());

        // 验证获取到的交易记录顺序与
        List<Transaction> transactions = page.data();
        Assertions.assertTrue(transactions.get(0).dateTime().isBefore(transactions.get(2).dateTime()));
    }

    @Test
    public void testGetPageTransactionsInvalidSort() {
        int total = 100,pageSize = 10,pageNo = 5;
        List<Transaction> data = fullfillTransaction(100);

        // 调用 getPageTransactions 方法，传入无效的排序方式
        Page<Transaction> page = transactionDao.getPageTransactions(false, 1, 10, "amount", "invalidSort");

        // 验证获取到的交易记录数量
        Assertions.assertEquals(pageSize, page.data().size());

        // 验证获取到的交易记录顺序 默认的asc
        List<Transaction> transactions = page.data();
        Assertions.assertTrue(transactions.get(0).amount().compareTo(transactions.get(2).amount()) == -1);
    }

}
