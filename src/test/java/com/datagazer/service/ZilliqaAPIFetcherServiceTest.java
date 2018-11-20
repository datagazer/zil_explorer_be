package com.datagazer.service;

import com.datagazer.app.ZilliqaBeApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZilliqaBeApp.class)
@Slf4j
@Transactional
public class ZilliqaAPIFetcherServiceTest {

    @Autowired
    private ZilliqaAPIFetcherService zilliqaAPIFetcherService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void fetchTransactionHashes() {
        List<String> transactionList = zilliqaAPIFetcherService.fetchTransactionList();
        assertFalse(transactionList.isEmpty());
    }

    @Test
    public void fetchTransactionRate() {
        Double rate = zilliqaAPIFetcherService.fetchTransactionRateAndNumTxBlocks().getLeft();
        Integer numTxBlocks = zilliqaAPIFetcherService.fetchTransactionRateAndNumTxBlocks().getRight();
        assertTrue(rate >= 0);
        assertTrue(numTxBlocks >= 0);
    }

    @Test
    public void fetchTransactionDetails() throws IOException {
        List<String> transactionList = zilliqaAPIFetcherService.fetchTransactionList();

        String transactionDetails = zilliqaAPIFetcherService.fetchTransactionDetails(transactionList.get(0));
        final ObjectNode node = new ObjectMapper().readValue(transactionDetails, ObjectNode.class);
        String returnedId = node.get("ID").textValue();
        assertEquals(transactionList.get(0),returnedId);
    }

    @Test
    public void saveTransactionDetails() throws IOException {
        jdbcTemplate.execute("delete from transactions");
        zilliqaAPIFetcherService.saveTransactionDetails();
        Integer size = jdbcTemplate.queryForObject("select count(1) from transactions", Integer.class);
        assertTrue(size > 0);
    }

    @Test
    public void fetchListOfTXBlockNums() {
        List<String> blockList = zilliqaAPIFetcherService.fetchTXBlockList();
        log.info(blockList.toString());
        assertFalse(blockList.isEmpty());
    }

    @Test
    public void fetchTxBlockDetails() throws IOException {
        List<String> blockList = zilliqaAPIFetcherService.fetchTXBlockList();
        String blockDetails = zilliqaAPIFetcherService.fetchTxBlockDetails(Integer.parseInt(blockList.get(0)));
        final ObjectNode node = new ObjectMapper().readValue(blockDetails, ObjectNode.class);
        String returnedId = node.get("BlockNum").textValue();
        assertEquals(blockList.get(0),returnedId);
    }

    @Test
    public void saveTxBlockDetails() throws IOException {
        jdbcTemplate.execute("delete from txblocks");
        zilliqaAPIFetcherService.saveTxBlockDetails();
        Integer size = jdbcTemplate.queryForObject("select count(1) from txblocks", Integer.class);
        assertTrue(size > 0);
    }

    @Test
    public void fetchListOfDSBlockNums() {
        List<String> blockList = zilliqaAPIFetcherService.fetchDSBlockList();
        log.info(blockList.toString());
        assertFalse(blockList.isEmpty());
    }

    @Test
    public void fetchDSBlockDetails() throws IOException {
        List<String> blockList = zilliqaAPIFetcherService.fetchDSBlockList();
        String blockDetails = zilliqaAPIFetcherService.fetchDSBlockDetails(Integer.parseInt(blockList.get(0)));
        final ObjectNode node = new ObjectMapper().readValue(blockDetails, ObjectNode.class);
        String returnedId = node.get("BlockNum").textValue();
        assertEquals(blockList.get(0),returnedId);
    }

    @Test
    public void saveDSBlockDetails() throws IOException {
        jdbcTemplate.execute("delete from dsblocks");
        zilliqaAPIFetcherService.saveDSBlockDetails();
        Integer size = jdbcTemplate.queryForObject("select count(1) from dsblocks", Integer.class);
        assertTrue(size > 0);
    }
}
