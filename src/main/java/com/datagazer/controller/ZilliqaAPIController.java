package com.datagazer.controller;

import com.datagazer.service.ZilliqaAPIFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ZilliqaAPIController {

    @Autowired
    private ZilliqaAPIFetcherService zilliqaAPIFetcherService;

//    @GetMapping
//    public ResponseEntity<List<String>> getTransactions() {
//        return ResponseEntity.ok(zilliqaAPIFetcherService.getTransactions());
//    }

    @GetMapping(value = "/transactions")
    public ResponseEntity<String> getTransactions() {

        return wrapResponse(zilliqaAPIFetcherService.getTransactions());
    }

    @GetMapping(value = "/txblocks")
    public ResponseEntity<String> getTxBlocks() {
        return wrapResponse(zilliqaAPIFetcherService.getTxBlocks());
    }

    @GetMapping(value = "/dsblocks")
    public ResponseEntity<String> getDSBlocks() {
        return wrapResponse(zilliqaAPIFetcherService.getDSBlocks());
    }

    private ResponseEntity<String> wrapResponse(Collection<String> col){
        //TODO temporary workaround.
        return ResponseEntity.ok("[" + col.stream().collect(Collectors.joining(","))+"]");
    }
}
