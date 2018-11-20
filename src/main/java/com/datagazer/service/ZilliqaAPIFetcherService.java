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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

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

    private String requestZilliqaApi(String methodName, Object params){
        HttpEntity<ZilliqaAPIRequestDto> request = new HttpEntity<>(ZilliqaAPIRequestDto.builder().
                id("1").jsonrpc("2.0").method(methodName).params(params).build(),headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(API_PATH,request,String.class);
    }

    public Double fetchTransactionRate(){
        String response = requestZilliqaApi("GetBlockchainInfo",Collections.singletonList(""));
        try {
            final ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
            return node.get("result").get("TransactionRate").asDouble();

        } catch (IOException e) {
            log.error("Cannot get list of transactions.Exception:" + e);
            throw new RuntimeException("Cannot get transaction rate. JSON format has changed");
        }
    }

//    public void saveTransactionRate

    public List<String> fetchTransactionList(){
        String response = requestZilliqaApi("GetRecentTransactions",Collections.singletonList(""));
        LinkedList<String> resultingHashes = new LinkedList<>();
        try {
            final ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
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
        String response = requestZilliqaApi("GetTransaction",Collections.singletonList(transactionHash));
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

    public String fetchTxBlockDetails(Integer blockNum) {
       return fetchBlockDetails(blockNum,"GetTxBlock");
    }

    public String fetchDSBlockDetails(Integer blockNum) {
        return fetchBlockDetails(blockNum,"GetDsBlock");
    }


    private String fetchBlockDetails(Integer blockNum, String apiMethodName) {
        String response = requestZilliqaApi(apiMethodName,Collections.singletonList(String.valueOf(blockNum)));
        try {
            final ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
            JsonNode jsonNode = node.get("result").get("header");
            //for DS blocks the field for block number is not capitalized
            if (jsonNode.has("blockNum")){
                String blockNumText = jsonNode.get("blockNum").asText();
                ((ObjectNode)jsonNode).put("BlockNum",blockNumText);
                ((ObjectNode)jsonNode).remove("blockNum");
            }
            return jsonNode.toString();
        }
        catch (IOException e) {
            log.error("Cannot get block details via " + apiMethodName + "Exception:" + e);
            throw new RuntimeException("JSON format has changed");
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveTxBlockDetails(){
        saveBlockDetails((s -> fetchTxBlockDetails(Integer.parseInt(s))),"txblocks");
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void saveDSBlockDetails(){
        saveBlockDetails((s -> fetchDSBlockDetails(Integer.parseInt(s))),"dsblocks");
    }

    private void saveBlockDetails(Function<String, String> detailsBlockFetchFunction, String tableName){

        Map<Integer,String> values = new LinkedHashMap<>();
        fetchTXBlockList().forEach(tr -> {values.put(Integer.parseInt(tr),detailsBlockFetchFunction.apply(tr));});
        log.info("Saving " + tableName+" details.Number of block queried:" + values.size() );

        for(Map.Entry<Integer,String> block : values.entrySet()) {
            jdbcTemplate.batchUpdate("insert ignore into "+ tableName+" (block_num, details) values (?,?)", new BatchPreparedStatementSetter() {
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
        return getBlocks("txblocks");
    }

    public List<String> getDSBlocks(){
        return getBlocks("dsblocks");
    }

    private List<String> getBlocks(String tableName){
        return jdbcTemplate.queryForList("select details from "+ tableName+" order by time_added desc limit 100",String.class);
    }

    public List<String> fetchTXBlockList(){
        return fetchBlockList("TxBlockListing");
    }

    public List<String> fetchDSBlockList(){
        return fetchBlockList("DSBlockListing");
    }

    private List<String> fetchBlockList(String apiMethodName){
        LinkedList<String> resultingBlockNums = new LinkedList<>();
        //todo read correct number of pages and take all blocks
        for (int i = 1;i<=5;i++){
            String response = requestZilliqaApi(apiMethodName,Collections.singletonList(i));
            try {
                final ObjectNode node = new ObjectMapper().readValue(response, ObjectNode.class);
                JsonNode txHashesNode = node.get("result").get("data");
                if (txHashesNode.isArray()){
                    for (final JsonNode objNode : txHashesNode) {
                        resultingBlockNums.add(objNode.get("BlockNum").asText());
                    }
                }
            } catch (IOException e) {
                log.error("Cannot get list of block via "+ apiMethodName + " .Exception:" + e);
            }
        }

        return resultingBlockNums;
    }
}