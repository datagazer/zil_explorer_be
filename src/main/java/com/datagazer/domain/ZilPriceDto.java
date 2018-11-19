package com.datagazer.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
public class ZilPriceDto {
    private String price;
    private String totalZilSupply;
    private String capitalization;
}
