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
    public void fetchListOfHashes() {
        List<String> transactionList = zilliqaAPIFetcherService.fetchTransactionList();
        assertFalse(transactionList.isEmpty());
    }

    @Test
    public void fetchTransactionDetails() throws IOException {
        List<String> transactionList = zilliqaAPIFetcherService.fetchTransactionList();

        String transactionDetails = zilliqaAPIFetcherService.fetchTransactionDetails(transactionList.get(0));
        final ObjectNode node = new ObjectMapper().readValue(transactionDetails, ObjectNode.class);
        String returnedId = node.get("result").get("ID").textValue();
        assertEquals(transactionList.get(0),returnedId);
    }

    @Test
    public void saveDetails() throws IOException {
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
        List<String> transactionList = zilliqaAPIFetcherService.fetchTXBlockList();

        String transactionDetails = zilliqaAPIFetcherService.fetchTxBlockDetails(Integer.parseInt(transactionList.get(0)));
        final ObjectNode node = new ObjectMapper().readValue(transactionDetails, ObjectNode.class);
        String returnedId = node.get("BlockNum").textValue();
        assertEquals(transactionList.get(0),returnedId);
    }

    @Test
    public void saveTxBlockDetails() throws IOException {
        jdbcTemplate.execute("delete from txblocks");
        zilliqaAPIFetcherService.saveTxBlockDetails();
        Integer size = jdbcTemplate.queryForObject("select count(1) from txblocks", Integer.class);
        assertTrue(size > 0);
    }
}
