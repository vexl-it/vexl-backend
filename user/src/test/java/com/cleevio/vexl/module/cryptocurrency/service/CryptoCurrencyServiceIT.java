package com.cleevio.vexl.module.cryptocurrency.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.common.integration.coingecko.CoingeckoConnector;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoMarketResponse;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoResponse;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CurrentPrice;
import com.cleevio.vexl.common.integration.coingecko.dto.response.MarketData;
import com.cleevio.vexl.module.cryptocurrency.exception.CoinException;
import com.google.common.testing.FakeTicker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

import static com.cleevio.vexl.common.config.CacheConfig.CACHE_REFRESH_LIMIT;
import static com.cleevio.vexl.common.config.CacheConfig.CACHE_TIME_UNIT;
import static com.cleevio.vexl.common.config.CacheConfig.COIN_PRICE;
import static com.cleevio.vexl.common.config.CacheConfig.MARKET_CHART;
import static com.cleevio.vexl.module.cryptocurrency.constant.Duration.DAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CryptoCurrencyServiceIT {


	private final CryptocurrencyService underTest;

	private final FakeTicker ticker;

	private final CacheManager cacheManager;

	@Autowired
	public CryptoCurrencyServiceIT(CryptocurrencyService underTest, FakeTicker ticker, CacheManager cacheManager) {
		this.underTest = underTest;
		this.ticker = ticker;
		this.cacheManager = cacheManager;
	}

	@MockBean
	CoingeckoConnector coingeckoConnector;

	@AfterEach
	void afterEach() {
		cacheManager.getCache(COIN_PRICE).invalidate();
		cacheManager.getCache(MARKET_CHART).invalidate();
	}

	@Test
	void retrieveMarketChart_shouldCallCoingeckoApi() {
		when(coingeckoConnector.retrieveMarketChart(anyString(), anyString(), eq("CZK"))).thenReturn(
				new CoingeckoMarketResponse(List.of(List.of(
						BigDecimal.valueOf(500),
						BigDecimal.valueOf(2500),
						BigDecimal.valueOf(6000)
				)))
		);
		CoingeckoMarketResponse actual = underTest.retrieveMarketChart(DAY, "CZK");

		verify(coingeckoConnector, times(1)).retrieveMarketChart(anyString(), anyString(), eq("CZK"));
		assertThat(actual.prices().get(0)).containsExactly(
				BigDecimal.valueOf(500),
				BigDecimal.valueOf(2500),
				BigDecimal.valueOf(6000)
		);

		CoingeckoMarketResponse cached = cacheManager.getCache(MARKET_CHART).get("DAY-CZK", CoingeckoMarketResponse.class);
		assertThat(actual.prices().get(0)).containsExactly(
				cached.prices().get(0).get(0),
				cached.prices().get(0).get(1),
				cached.prices().get(0).get(2)
		);
	}

	@Test
	void retrieveMarketChart_shouldGetValuesFromCache() {
		cacheManager.getCache("market_chart").put("DAY-USD", new CoingeckoMarketResponse(List.of(List.of(BigDecimal.ONE, BigDecimal.TEN))));

		CoingeckoMarketResponse actual = underTest.retrieveMarketChart(DAY, "USD");

		verify(coingeckoConnector, never()).retrieveMarketChart(anyString(), anyString(), eq("USD"));
		assertThat(actual.prices().get(0)).containsExactly(BigDecimal.ONE, BigDecimal.TEN);
	}

	@Test
	void retrieveMarketChart_shouldGetValuesFromCacheWhenConnectorThrowsException() {
		CoingeckoMarketResponse expected = new CoingeckoMarketResponse(List.of(List.of(BigDecimal.valueOf(200), BigDecimal.valueOf(150))));
		when(coingeckoConnector.retrieveMarketChart(anyString(), anyString(), eq("EUR"))).thenReturn(expected);
		underTest.retrieveMarketChart(DAY, "EUR");

		ticker.advance(CACHE_REFRESH_LIMIT + 2, CACHE_TIME_UNIT);

		doThrow(CoinException.class).when(coingeckoConnector).retrieveMarketChart(anyString(), anyString(), eq("EUR"));
		CoingeckoMarketResponse actual = underTest.retrieveMarketChart(DAY, "EUR");

		verify(coingeckoConnector, times(2)).retrieveMarketChart(any(), any(), any());
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void retrieveCoinPrice_shouldCallCoingeckoApi() {
		CoingeckoResponse expected = getCoinResponse();
		when(coingeckoConnector.retrieveCoinPrice("BTC")).thenReturn(expected);
		CoingeckoResponse actual = underTest.retrieveCoinPrice("BTC");

		verify(coingeckoConnector, times(1)).retrieveCoinPrice("BTC");
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void retrieveCoinPrice_shouldGetValuesFromCache() {
		CoingeckoResponse expected = getCoinResponse();
		cacheManager.getCache("coin_price").put("BTB", expected);

		CoingeckoResponse actual = underTest.retrieveCoinPrice("BTB");

		verify(coingeckoConnector, never()).retrieveCoinPrice("BTB");
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void retrieveCoinPrice_shouldGetValuesFromCacheWhenConnectorThrowsException() {
		CoingeckoResponse expected = getCoinResponse();
		when(coingeckoConnector.retrieveCoinPrice("WAT")).thenReturn(expected);
		underTest.retrieveCoinPrice("WAT");

		ticker.advance(CACHE_REFRESH_LIMIT + 2, CACHE_TIME_UNIT);

		doThrow(CoinException.class).when(coingeckoConnector).retrieveCoinPrice("WAT");
		CoingeckoResponse actual = underTest.retrieveCoinPrice("WAT");

		verify(coingeckoConnector, times(2)).retrieveCoinPrice("WAT");
		assertThat(actual).isEqualTo(expected);
	}

	private CoingeckoResponse getCoinResponse() {
		Random random = new Random();

		MarketData marketData = new MarketData();
		CurrentPrice currentPrice = new CurrentPrice();
		currentPrice.setCzk(BigDecimal.valueOf(random.nextLong()));
		currentPrice.setEur(BigDecimal.valueOf(random.nextLong()));
		currentPrice.setUsd(BigDecimal.valueOf(random.nextLong()));

		marketData.setCurrentPrice(currentPrice);
		marketData.setPriceChangePercentage24h(BigDecimal.valueOf(random.nextLong()));
		marketData.setPriceChangePercentage7d(BigDecimal.valueOf(random.nextLong()));
		marketData.setPriceChangePercentage14d(BigDecimal.valueOf(random.nextLong()));
		marketData.setPriceChangePercentage30d(BigDecimal.valueOf(random.nextLong()));
		marketData.setPriceChangePercentage60d(BigDecimal.valueOf(random.nextLong()));
		marketData.setPriceChangePercentage200d(BigDecimal.valueOf(random.nextLong()));
		marketData.setPriceChangePercentage1y(BigDecimal.valueOf(random.nextLong()));
		marketData.setLastUpdated(ZonedDateTime.now());


		CoingeckoResponse response = new CoingeckoResponse();
		response.setMarketData(marketData);

		return response;
	}

}
