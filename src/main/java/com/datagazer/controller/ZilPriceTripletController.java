package com.datagazer.controller;

import com.datagazer.domain.ZilPriceDto;
import com.datagazer.service.BinanceAPIFetcherService;
import com.datagazer.service.ZilliqaAPIFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value = "/zilprice")
public class ZilPriceTripletController {

    @Autowired
    private BinanceAPIFetcherService binanceAPIFetcherService;

    @GetMapping
    public ResponseEntity<ZilPriceDto> getTransactions() {
        return ResponseEntity.ok(binanceAPIFetcherService.getPriceTriplet());
    }

}
