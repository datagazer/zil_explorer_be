package com.datagazer.service;

import com.datagazer.app.ZilliqaBeApp;
import com.datagazer.domain.BlockchainSummaryDto;
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
        BlockchainSummaryDto blockchainSummaryDto = zilliqaAPIFetcherService.fetchBlockChainInfo();
        Double rate = blockchainSummaryDto.getTransactionRate();
        Double numTxBlocks = blockchainSummaryDto.getTxBlockNum();

        assertTrue(rate >= 0);
        assertTrue(numTxBlocks >= 0);
        assertTrue(zilliqaAPIFetcherService.fetchBlockChainInfo().getTransactionNum() >= 0);

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

    @Test
    public void saveBlockChainSummary() throws IOException {
        jdbcTemplate.execute("delete from blockchain_summary");
        zilliqaAPIFetcherService.saveBlockchainSummary();
        Integer size = jdbcTemplate.queryForObject("select count(1) from blockchain_summary", Integer.class);
        assertTrue(size > 0);
    }

    @Test
    public void getBlockchainSummary() throws IOException {
        jdbcTemplate.execute("insert into blockchain_summary (transaction_num,transaction_rate,tx_block_num,zil_price) values(100,123.23,3444,0.00022)");
        List<BlockchainSummaryDto> blockchainSummaryList = zilliqaAPIFetcherService.getBlockchainSummaryList();
        log.info(blockchainSummaryList.get(0).toString());
        assertTrue(blockchainSummaryList.size() > 0);
    }

    @Test
    public void getTxBlockDetailsForADSBlock() throws IOException {
        zilliqaAPIFetcherService.saveTxBlockDetails();
        Integer dsblockNum = jdbcTemplate.queryForObject("select dsblock_num from txblocks limit 1", Integer.class);
        List<String> txBlockList = zilliqaAPIFetcherService.getTxBlockDetailsForADSBlock(dsblockNum);
        log.info(""+txBlockList);
        assertTrue(txBlockList.size() > 0);
    }

    @Test
    public void getMiningDifficulty() throws IOException {
        zilliqaAPIFetcherService.saveDSBlockDetails();
        String miningDifficulty = zilliqaAPIFetcherService.getMiningDifficulty();
        assertTrue(Integer.valueOf(miningDifficulty) > 0);
    }

}
