package com.cleevio.vexl.common.config;

import com.cleevio.vexl.common.integration.coingecko.CoingeckoConnector;
import com.cleevio.vexl.module.cryptocurrency.constant.Duration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.cleevio.vexl.common.util.DurationUtils.createFromUnixTimestamp;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

	public static final String MARKET_CHART = "market_chart";
	public static final String COIN_PRICE = "coin_price";
	public static final long CACHE_MAXIMUM_SIZE = 100;
	public static final TimeUnit CACHE_TIME_UNIT = TimeUnit.MINUTES;
	public static final long CACHE_REFRESH_LIMIT = 15;

	@Bean
	public CacheManager cacheManager(Cache<Object, Object> coinPriceCache, Cache<Object, Object> marketChartCache) {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.registerCustomCache(COIN_PRICE, coinPriceCache);
		cacheManager.registerCustomCache(MARKET_CHART, marketChartCache);

		return cacheManager;
	}

	@Bean
	public Cache<Object, Object> coinPriceCache(Ticker ticker, CoingeckoConnector coingeckoConnector) {
		return Caffeine.newBuilder()
				.refreshAfterWrite(CACHE_REFRESH_LIMIT, CACHE_TIME_UNIT)
				.maximumSize(CACHE_MAXIMUM_SIZE)
				.ticker(ticker)
				.build(key -> coingeckoConnector.retrieveCoinPrice(key.toString()));
	}

	@Bean
	public Cache<Object, Object> marketChartCache(Ticker ticker, CoingeckoConnector coingeckoConnector) {
		return Caffeine.newBuilder()
				.refreshAfterWrite(CACHE_REFRESH_LIMIT, CACHE_TIME_UNIT)
				.maximumSize(CACHE_MAXIMUM_SIZE)
				.ticker(ticker)
				.build(key -> {
					String[] cacheKey = key.toString().split("-");
					final String to = String.valueOf(Instant.now().getEpochSecond());
					final String from = String.valueOf(createFromUnixTimestamp(Duration.valueOf(cacheKey[0])));

					return coingeckoConnector.retrieveMarketChart(from, to, cacheKey[1]);
				});
	}

	@Bean
	public Ticker ticker() {
		return Ticker.systemTicker();
	}
}
