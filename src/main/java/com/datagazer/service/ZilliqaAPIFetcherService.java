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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    public String fetchTransactionDetails(String transactionHash){
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<ZilliqaAPIRequestDto> request = new HttpEntity<>(ZilliqaAPIRequestDto.builder().
                id("1").jsonrpc("2.0").method("GetTransaction").params(Collections.singletonList(transactionHash)).build(),headers);
        return restTemplate.postForObject(API_PATH,request,String.class);
    }

    public List<String> getTransactions(){
        return jdbcTemplate.queryForList("select details from transactions order by time_added desc limit 100",String.class);
    }

    public void saveTransactionDetails(){
        fetchTransactionList().forEach(tr -> {
            String details = fetchTransactionDetails(tr);
            //TODO do not add what is already there
            //TODO BULK INSERT
            jdbcTemplate.execute(String.format("insert into transactions (hash, details) values ('%s','%s')",tr, details));
        });
    }
}
//{"id":"1","jsonrpc":"2.0","result":{"TxnHashes":["5cea701aaf4afb65261bac7833fa135fe00f004ea53c136f264192a841a8a4b1","38301b65763f69590edbb9b6097438a05e37224e8533f03907fb613220ac5daf","7e99abb3ad8ee867ac5015311514e573c6c1c2590a4425b1bbc2dacffa0c1f1f","156848effd130b7913be42d5844ffbf3ee3363ab77f8d5a08b4306ee96021c48","88fca6f55c072ef329530a289297933f114ff2dbe470f93045663ec2b9e95e28","39156a2340fd139bda336422e7cbb52d861c231dc295c7d82c6f8aa2dd4c7b5b","97119a69e510c5fc6bf6af060231b0bf09bfe3e9ca9b4c5387f2a217d0dfe451","d2c237dfb20dd26f24be771ccdc37713c84eeebfff4b5268af49ad7b8f36312c","66f408ef94c976b4310f5e47961391e10844c673c3c4fcc9bd2c1596fd3a6792","7f65a65f8f593f277c7214c57381fe77a9e81e1a70cb381ad577e6e85fea0eea","1e316974069dbb6dacf716092090f71fe920e752bb5b918c9276b256b079351c","cac38ae7236eda244c736b986ce0b86ce45fc5b35b18d2ea5d9351795720f20d"],"number":12}}