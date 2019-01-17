package com.datagazer.service;

import com.datagazer.domain.ZilliqaAPIRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
public class CoinMarketCapAPIFetcherService {

    private static String apiEndpoint = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=ZIL&CMC_PRO_API_KEY=c981c25b-ab4b-4587-b5c0-46a2c2b9ec89";

    public static HttpHeaders headers = new HttpHeaders();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";

    static {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", USER_AGENT);
    }

    public Double getMarketCap(){
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        ResponseEntity<String> response = restTemplate.exchange(apiEndpoint, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        try {
            ObjectNode node = objectMapper.readValue(response.getBody(), ObjectNode.class);
            double marketCap = node.get("data").get("ZIL").get("quote").get("USD").get("market_cap").asDouble();
            return  marketCap;
        } catch (Exception e) {
            log.error("Cannot query CoinMarketCap data",e);
            throw new RuntimeException("Cannot query CoinMarketCap data");
        }
    }
}
