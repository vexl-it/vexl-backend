package com.cleevio.vexl.common.integration.coingecko.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoingeckoMarketResponse(

        @JsonProperty("prices")
        List<List<BigDecimal>> prices

) {
}
