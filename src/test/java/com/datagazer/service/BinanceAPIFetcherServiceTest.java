package com.datagazer.service;

import com.datagazer.app.ZilliqaBeApp;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZilliqaBeApp.class)
public class BinanceAPIFetcherServiceTest {

    @Test
    public void readGetZilPrice() {
        String apiPath = "https://api.binance.com";
        String pingApiPath = "https://api.binance.com/api/v1/ping";
//        String apiPath = "https://scilla-test-api.aws.z7a.xyz";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> entity = restTemplate.getForEntity(pingApiPath, String.class);
        Assert.assertNotNull(entity);
    }
}
