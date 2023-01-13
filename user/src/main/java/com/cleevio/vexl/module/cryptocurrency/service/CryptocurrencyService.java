package com.cleevio.vexl.module.cryptocurrency.service;

import com.cleevio.vexl.common.integration.coingecko.CoingeckoConnector;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoMarketResponse;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoResponse;
import com.cleevio.vexl.module.cryptocurrency.constant.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.cleevio.vexl.common.config.CacheConfig.COIN_PRICE;
import static com.cleevio.vexl.common.config.CacheConfig.MARKET_CHART;
import static com.cleevio.vexl.common.util.DurationUtils.createFromUnixTimestamp;

/**
 * Service for getting information about cryptocurrency.
 */
@Service
@RequiredArgsConstructor
public class CryptocurrencyService {

    private final CoingeckoConnector coingeckoConnector;

    @Cacheable(cacheNames = COIN_PRICE, key = "#coin", sync = true)
    public CoingeckoResponse retrieveCoinPrice(String coin) {
        return this.coingeckoConnector.retrieveCoinPrice(coin);
    }

    @Cacheable(cacheNames = MARKET_CHART, key = "#duration.name().concat(\"-\").concat(#currency)", sync = true)
    public CoingeckoMarketResponse retrieveMarketChart(Duration duration, String currency) {
        final String to = String.valueOf(Instant.now().getEpochSecond());
        final String from = String.valueOf(createFromUnixTimestamp(duration));

        return this.coingeckoConnector.retrieveMarketChart(from, to, currency);
    }
}
