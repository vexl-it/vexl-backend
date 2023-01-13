package com.cleevio.vexl.common;

import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.testing.FakeTicker;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CryptoCurrencyServiceIntegrationTestConfiguration {

	@Bean
	FakeTicker fakeTicker() {
		return new FakeTicker();
	}

	@Bean
	Ticker ticker(FakeTicker fakeTicker) {
		return fakeTicker::read;
	}
}
