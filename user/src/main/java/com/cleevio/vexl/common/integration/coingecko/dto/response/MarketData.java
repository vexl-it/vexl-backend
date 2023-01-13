package com.cleevio.vexl.common.integration.coingecko.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketData {

    @JsonProperty("current_price")
    private CurrentPrice currentPrice;

    @JsonProperty("price_change_percentage_24h")
    private BigDecimal priceChangePercentage24h;

    @JsonProperty("price_change_percentage_7d")
    private BigDecimal priceChangePercentage7d;

    @JsonProperty("price_change_percentage_14d")
    private BigDecimal priceChangePercentage14d;

    @JsonProperty("price_change_percentage_30d")
    private BigDecimal priceChangePercentage30d;

    @JsonProperty("price_change_percentage_60d")
    private BigDecimal priceChangePercentage60d;

    @JsonProperty("price_change_percentage_200d")
    private BigDecimal priceChangePercentage200d;

    @JsonProperty("price_change_percentage_1y")
    private BigDecimal priceChangePercentage1y;

    @JsonProperty("last_updated")
    private ZonedDateTime lastUpdated;

}


