package com.datagazer.controller;

import com.datagazer.service.ZilliqaAPIFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value = "/transactions")
public class TransactionListController {

    @Autowired
    private ZilliqaAPIFetcherService zilliqaAPIFetcherService;

//    @GetMapping
//    public ResponseEntity<List<String>> getTransactions() {
//        return ResponseEntity.ok(zilliqaAPIFetcherService.getTransactions());
//    }

    @GetMapping
    public ResponseEntity<String> getTransactions() {
        //TODO temporary workaround.
        return ResponseEntity.ok("[" + zilliqaAPIFetcherService.getTransactions().stream().collect(Collectors.joining(","))+"]");
    }
}
