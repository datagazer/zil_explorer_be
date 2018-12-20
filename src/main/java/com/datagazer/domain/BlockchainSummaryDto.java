package com.datagazer.domain;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainSummaryDto {
    private Double transactionRate;
    private Double txBlockNum;
    private Double zilPrice;
    private Double transactionNum;
    private LocalDate dayAdded;
    private Double dsMiningDifficulty;
    private Double txMiningDifficulty;
}
