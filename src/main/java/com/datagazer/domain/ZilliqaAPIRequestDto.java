package com.datagazer.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ZilliqaAPIRequestDto {
    private String id;
    private String jsonrpc;
    private String method;
    private Object params;
}
