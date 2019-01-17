package com.datagazer.service;

import com.datagazer.app.ZilliqaBeApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZilliqaBeApp.class)
public class CoinMarketCapAPIFetcherServiceTest {

    @Autowired
    private CoinMarketCapAPIFetcherService coinMarketCapAPIFetcherService;

    @Test
    public void getMarketCap() {
       assertTrue(coinMarketCapAPIFetcherService.getMarketCap()>0);
    }
}