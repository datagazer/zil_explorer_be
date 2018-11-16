package com.datagazer.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class DestinationDto {

    private String place;
    private Double price;
}
