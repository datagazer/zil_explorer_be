package com.datagazer.domain;


import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MainPageValuesDto {
    private Double transactionRate;
    private String price;
    private String miningDifficulty;
    private String totalZilSupply;
    private String capitalization;
}
