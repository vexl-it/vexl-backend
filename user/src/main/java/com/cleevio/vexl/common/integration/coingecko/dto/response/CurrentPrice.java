package com.cleevio.vexl.common.integration.coingecko.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentPrice {

    private BigDecimal usd;

    private BigDecimal czk;

    private BigDecimal eur;
}
