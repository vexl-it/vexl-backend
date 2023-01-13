package com.cleevio.vexl.common.integration.coingecko.config;

import com.cleevio.vexl.common.integration.coingecko.CoingeckoConnector;
import com.cleevio.vexl.common.integration.coingecko.constant.CoingeckoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class CoingeckoConfig {

    private final CoingeckoProperties coingeckoProperties;

    private final WebClient.Builder builder;

    @Bean
    public CoingeckoConnector webClient() {
        return new CoingeckoConnector(
                builder
                        .baseUrl(coingeckoProperties.getUrl())
                        .build(),
                coingeckoProperties
        );
    }
}
