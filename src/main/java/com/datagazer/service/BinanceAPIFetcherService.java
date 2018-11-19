package com.datagazer.service;

import com.datagazer.domain.ZilPriceDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
public class BinanceAPIFetcherService {

    private static String zilBTCEndpoint = "https://api.binance.com/api/v3/ticker/price?symbol=ZILBTC";
    private static String bTCUSDTEndpoint = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";

    private static long totalZilIssued = 12600000000L;


    public Double getZilPrice(){
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Double priceZilBTC = getPricePair(restTemplate, objectMapper,zilBTCEndpoint);
            Double priceBTCUSD = getPricePair(restTemplate, objectMapper,bTCUSDTEndpoint);
            return priceZilBTC * priceBTCUSD;
        } catch (IOException e) {
            log.error("Cannot query binance zil price data",e);
            throw new RuntimeException("Cannot query binance zil price data");
        }
    }

    private double getPricePair(RestTemplate restTemplate, ObjectMapper objectMapper, String endpoint) throws IOException {
        ObjectNode node = objectMapper.readValue(restTemplate.getForObject(endpoint, String.class), ObjectNode.class);
        return Double.parseDouble(node.get("price").asText());
    }

    public ZilPriceDto getPriceTriplet(){
        Double zilPrice = getZilPrice();
        return ZilPriceDto.builder().price(String.format("%.4f", zilPrice)).totalZilSupply(String.format("%.0fM", totalZilIssued/ 1000000.0)).
                capitalization(String.format("%.0fM", zilPrice*totalZilIssued/ 1000000.0)).build();
    }


}
