package com.mybank.balance.transaction;

import com.mybank.balance.transaction.controller.TransactionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mybank.balance.transaction.service.TransactionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;



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

}

