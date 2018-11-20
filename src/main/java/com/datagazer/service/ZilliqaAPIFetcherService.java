package com.datagazer.service;

import com.datagazer.domain.ZilliqaAPIRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Service
@Slf4j
public class ZilliqaAPIFetcherService {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static String API_PATH = "https://api-scilla.zilliqa.com";
    public static HttpHeaders headers = new HttpHeaders();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";

    static {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", USER_AGENT);
    }

    public List<String> fetchTransactionList(){
        HttpEntity<ZilliqaAPIRequestDto> request = new HttpEntity<>(ZilliqaAPIRequestDto.builder().
                id("1").jsonrpc("2.0").method("GetRecentTransactions").params(Collections.singletonList("")).build(),headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(API_PATH,request,String.class);
        LinkedList<String> resultingHashes = new LinkedList<>();
        try {
            final ObjectNode node = new ObjectMapper().readValue(response.getBody(), ObjectNode.class);
            JsonNode txHashesNode = node.get("result").get("TxnHashes");
            if (txHashesNode.isArray()){
                for (final JsonNode objNode : txHashesNode) {
                     resultingHashes.add(objNode.asText());
                }
            }

        } catch (IOException e) {
            log.error("Cannot get list of transactions.Exception:" + e);
        }
        return resultingHashes;
    }

    public String fetchTransactionDetails(String transactionHash) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ZilliqaAPIRequestDto> request = new HttpEntity<>(ZilliqaAPIRequestDto.builder().
                id("1").jsonrpc("2.0").method("GetTransaction").params(Collections.singletonList(transactionHash)).build(), headers);

        String response = restTemplate.postForObject(API_PATH, request, String.class);
        try {
            final ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
            ((ObjectNode)node.get("result")).put("timestamp",System.currentTimeMillis());
            JsonNode result = node.get("result");
            return result.toString();
        }
        catch (IOException e) {
            log.error("Cannot transaction details .Exception:" + e);
            throw new RuntimeException("JSON format has changed");
        }
    }

    public List<String> getTransactions(){
        return jdbcTemplate.queryForList("select details from transactions order by time_added desc limit 100",String.class);
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveTransactionDetails(){

        Map<String,String> values = new LinkedHashMap<>();
        fetchTransactionList().forEach(tr -> {values.put(tr,fetchTransactionDetails(tr));});
        log.info("Saving transaction details.Number of transations queried:" + values.size() );

        for(Map.Entry<String,String> transation : values.entrySet()) {
            jdbcTemplate.batchUpdate("insert ignore into transactions (hash, details) values (?,?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i)
                        throws SQLException {
                    ps.setString(1, transation.getKey());
                    ps.setString(2, transation.getValue());
                }

                @Override
                public int getBatchSize() {
                    return values.size();
                }
            });
        }

    }

    public List<String> fetchTXBlockList(){
        LinkedList<String> resultingTXBlockHashes = new LinkedList<>();
        //todo read correct number of pages and take all blocks
        for (int i = 1;i<=5;i++){
            HttpEntity<ZilliqaAPIRequestDto> request = new HttpEntity<>(ZilliqaAPIRequestDto.builder().
                    id("1").jsonrpc("2.0").method("TxBlockListing").params(Collections.singletonList(i)).build(),headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(API_PATH,request,String.class);

            try {
                final ObjectNode node = new ObjectMapper().readValue(response.getBody(), ObjectNode.class);
                JsonNode txHashesNode = node.get("result").get("data");
                if (txHashesNode.isArray()){
                    for (final JsonNode objNode : txHashesNode) {
                        resultingTXBlockHashes.add(objNode.get("BlockNum").asText());
                    }
                }
            } catch (IOException e) {
                log.error("Cannot get list of tx blocks.Exception:" + e);
            }
        }

        return resultingTXBlockHashes;
    }

    public String fetchTxBlockDetails(Integer blockNum) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ZilliqaAPIRequestDto> request = new HttpEntity<>(ZilliqaAPIRequestDto.builder().
                id("1").jsonrpc("2.0").method("GetTxBlock").params(Collections.singletonList(String.valueOf(blockNum))).build(), headers);

        String response = restTemplate.postForObject(API_PATH, request, String.class);
        try {
            final ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
            return node.get("result").get("header").toString();
        }
        catch (IOException e) {
            log.error("Cannot get tx block details .Exception:" + e);
            throw new RuntimeException("JSON format has changed");
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveTxBlockDetails(){

        Map<Integer,String> values = new LinkedHashMap<>();
        fetchTXBlockList().forEach(tr -> {values.put(Integer.parseInt(tr),fetchTxBlockDetails(Integer.parseInt(tr)));});
        log.info("Saving transaction details.Number of transations queried:" + values.size() );

        for(Map.Entry<Integer,String> block : values.entrySet()) {
            jdbcTemplate.batchUpdate("insert ignore into txblocks (block_num, details) values (?,?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i)
                        throws SQLException {
                    ps.setInt(1, block.getKey());
                    ps.setString(2, block.getValue());
                }

                @Override
                public int getBatchSize() {
                    return values.size();
                }
            });
        }

    }

    public List<String> getTxBlocks(){
        return jdbcTemplate.queryForList("select details from txblocks order by time_added desc limit 100",String.class);
    }
}