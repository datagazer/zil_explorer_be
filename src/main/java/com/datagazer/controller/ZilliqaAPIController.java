package com.datagazer.controller;

import com.datagazer.domain.BlockchainSummaryDto;
import com.datagazer.domain.MainPageValuesDto;
import com.datagazer.service.ZilliqaAPIFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
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

    @GetMapping(value = "/dsblocks/{blockNum}/txblocks")
    public ResponseEntity<String> getTXBlocksForDSBlock(@PathVariable Integer blockNum) {
        return wrapResponse(zilliqaAPIFetcherService.getTxBlockDetailsForADSBlock(blockNum));
    }

//    @GetMapping(value = "/address/{addressNumber}/transactions")
//    public ResponseEntity<String> getAddressTransactions(@PathVariable String addressNumber) {
//        return wrapResponse(zilliqaAPIFetcherService.getAddressTransactions(addressNumber));
//    }

    @GetMapping(value = "/charts")
    public ResponseEntity<List<BlockchainSummaryDto>> getChartData() {
        return ResponseEntity.ok(zilliqaAPIFetcherService.getBlockchainSummaryList());
    }

    @GetMapping(value = "/charts/full")
    public ResponseEntity<List<BlockchainSummaryDto>> getChartFullHistoryData() {
        return ResponseEntity.ok(zilliqaAPIFetcherService.getBlockchainSummaryFullHistoryList());
    }

    @GetMapping(value = "/mainpage")
    public ResponseEntity<MainPageValuesDto> getMainPageValuesData() {
        return ResponseEntity.ok(zilliqaAPIFetcherService.getMainPageValues());
    }

    private ResponseEntity<String> wrapResponse(Collection<String> col){
        //TODO temporary workaround.
        return ResponseEntity.ok("[" + col.stream().collect(Collectors.joining(","))+"]");
    }


}
