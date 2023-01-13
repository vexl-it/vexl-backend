package com.cleevio.vexl.common.integration.coingecko;

import com.cleevio.vexl.common.integration.coingecko.constant.CoingeckoProperties;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoResponse;
import com.cleevio.vexl.common.integration.coingecko.dto.response.CoingeckoMarketResponse;
import com.cleevio.vexl.common.util.ErrorHandlerUtil;
import com.cleevio.vexl.module.cryptocurrency.exception.CoinException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
public class CoingeckoConnector {

    public static final String VS_CURRENCY = "vs_currency";
    public static final String FROM = "from";
    public static final String TO = "to";
    private final WebClient webClient;
    private final CoingeckoProperties coingeckoProperties;

    public CoingeckoResponse retrieveCoinPrice(final String coin) {
        return webClient.get()
                .uri(coingeckoProperties.getCoin(), coin)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> ErrorHandlerUtil.defaultHandler(clientResponse, coingeckoProperties.getCoin()))
                .bodyToMono(CoingeckoResponse.class)
                .blockOptional()
                .orElseThrow(CoinException::new);
    }

    public CoingeckoMarketResponse retrieveMarketChart(String from, String to, String currency) {
        final URI targetUrl = UriComponentsBuilder.fromUriString(coingeckoProperties.getUrl())
                .path(coingeckoProperties.getMarketChart())
                .queryParam(VS_CURRENCY, currency)
                .queryParam(FROM, from)
                .queryParam(TO, to)
                .build()
                .encode()
                .toUri();

        return webClient.get()
                .uri(targetUrl)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> ErrorHandlerUtil.defaultHandler(clientResponse, coingeckoProperties.getMarketChart()))
                .bodyToMono(CoingeckoMarketResponse.class)
                .blockOptional()
                .orElseThrow(CoinException::new);
    }
}
