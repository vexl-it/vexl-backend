package com.cleevio.vexl.module.cryptocurrency.controller;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.cryptocurrency.constant.Duration;
import com.cleevio.vexl.module.cryptocurrency.dto.response.CoinPriceResponse;
import com.cleevio.vexl.module.cryptocurrency.dto.response.MarketChartResponse;
import com.cleevio.vexl.module.cryptocurrency.exception.CoinException;
import com.cleevio.vexl.module.cryptocurrency.service.CryptocurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cryptocurrency", description = "For cryptocurrency data retrieval.")
@RestController
@RequestMapping(value = "/api/v1/cryptocurrencies")
@RequiredArgsConstructor
public class CryptocurrencyController {

    private final CryptocurrencyService cryptocurrencyService;

    @GetMapping("/{coin}")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "422 (101101)", description = "Coingecko was not able to process request.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtaining market data as price and percentage of the price change of the selected cryptocurrency",
            description = "For example, use 'bitcoin' for coin parameter if you want information about Bitcoin.")
    CoinPriceResponse retrieveCoinPrice(@PathVariable String coin)
            throws CoinException {
        return new CoinPriceResponse(this.cryptocurrencyService.retrieveCoinPrice(coin));
    }

    @GetMapping("/bitcoin/market_chart")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (101101)", description = "Coingecko was not able to process request.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get historical price.",
            description = """
                    Data granularity is automatic (cannot be adjusted)
                    - 1 day from current time = 5 minute interval data
                    - 1 - 90 days from current time = hourly data
                    - above 90 days from current time = daily data (00:00 UTC)
                    """)
    MarketChartResponse retrieveCoinPriceMarketChart(@RequestParam Duration duration,
                                                     @RequestParam(required = false, defaultValue = "USD") String currency)
            throws CoinException {
        return new MarketChartResponse(this.cryptocurrencyService.retrieveMarketChart(duration, currency));
    }
}
