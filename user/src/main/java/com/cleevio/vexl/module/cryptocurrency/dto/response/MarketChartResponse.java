package com.cleevio.vexl.module.cryptocurrency.dto.response;

import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoMarketResponse;

import java.math.BigDecimal;
import java.util.List;

public record MarketChartResponse(

        List<List<BigDecimal>> prices

) {

    public MarketChartResponse(CoingeckoMarketResponse coingecko) {
        this(coingecko.prices());
    }
}
