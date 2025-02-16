package com.mybank.balance.transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mybank.balance.transaction.common.Constants;
import com.mybank.balance.transaction.common.Response;
import com.mybank.balance.transaction.model.Page;
import com.mybank.balance.transaction.controller.TransactionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.service.TransactionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zhangdaochuan
 * @time 2025/1/17 00:47
 */
@SpringBootTest
public class TransactionControllerTest extends BaseTest{

    @Autowired
    private TransactionController transactionController;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    public void testCreateTransaction() {
        // 创建交易对象
        Transaction transaction = generateRandomTransaction(null);

        // 模拟服务层添加交易成功
        when(transactionService.addTransaction(transaction)).thenReturn(transaction);

        // 调用控制器方法
        Response<Transaction> response = transactionController.createTransaction(transaction);

        // 验证响应状态码为 OK
        assertEquals(Constants.CODE_SUCCESS, response.getStatusCode());
    }

    @Test
    public void testDeleteTransaction() {
        // 交易 ID
        String id = "123";
        Transaction transaction = generateRandomTransaction(id);
        transactionController.createTransaction(transaction);
        // 调用控制器方法
        Response<Boolean> response = transactionController.deleteTransaction(id);

        // 验证响应状态码为 OK
        assertEquals(Constants.CODE_SUCCESS, response.getStatusCode());

        // 验证服务层方法被调用
        verify(transactionService, times(1)).deleteTransaction(id);
    }

    @Test
    public void testUpdateTransaction() {
        String id = "456";
        // 创建交易对象
        Transaction old = generateRandomTransaction(id);
        transactionController.createTransaction(old);
        Transaction update = generateRandomTransaction(id);
        // 模拟服务层更新交易成功并返回更新后的交易
        when(transactionService.updateTransaction(update)).thenReturn(update);

        // 调用控制器方法
        Response<Transaction> response = transactionController.updateTransaction(update);

        // 验证响应状态码为 OK
        assertEquals(Constants.CODE_SUCCESS, response.getStatusCode());

        // 验证返回的交易对象与更新后的交易对象相同
        assertEquals(update, response.getData());
    }

    @Test
    public void testGetPageTransactions() {
        fullfillTransaction(1000);
        // 分页参数
        int page = 0;
        int size = 10;
        String orderBy = "time";
        String sort = "asc";

        // 模拟服务层获取分页交易成功
        Page<Transaction> pageTransactions = transactionService.getPageTransactions(page, size, orderBy, sort);
        when(transactionService.getPageTransactions(page, size, orderBy, sort)).thenReturn(pageTransactions);

        // 调用控制器方法
        Response<Page<Transaction>> response = transactionController.getPageTransactions(page, size, orderBy, sort);

        // 验证响应状态码为 OK
        assertEquals(Constants.CODE_SUCCESS, response.getStatusCode());

        // 验证返回的分页交易对象与模拟的分页交易对象相同
        assertEquals(pageTransactions, response.getData());
    }

    private int fullfillTransaction(int size) {
        for(int i=0;i<size;i++)
        {
            transactionController.createTransaction(generateRandomTransaction(null));
        }
        return size ;
    }

    @Test
    public void testGetAllTransactions() {
        int size = 1000;
        fullfillTransaction(size);
        // 排序参数
        String orderBy = "time";
        String sort = "asc";

        // 模拟服务层获取所有交易成功
        List<Transaction> transactions = new ArrayList<>();
        when(transactionService.getAllTransactions(orderBy, sort)).thenReturn(transactions);

        // 调用控制器方法
        Response<List<Transaction>> response = transactionController.getAllTransactions(orderBy, sort);

        // 验证响应状态码为 OK
        assertEquals(Constants.CODE_SUCCESS, response.getStatusCode());

        // 验证返回的交易列表与模拟的交易列表相同
        assertEquals(transactions, response.getData());
    }
}

