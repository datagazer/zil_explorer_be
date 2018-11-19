package com.datagazer.service;

import com.datagazer.app.ZilliqaBeApp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZilliqaBeApp.class)
public class BinanceAPIFetcherServiceTest {

    @Autowired
    private BinanceAPIFetcherService binanceAPIFetcherService;

    @Test
    public void readGetZilPrice() throws IOException {
        Assert.assertNotNull(binanceAPIFetcherService.getZilPrice());
    }
}
