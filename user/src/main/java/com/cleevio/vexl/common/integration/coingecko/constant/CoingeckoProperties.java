package com.cleevio.vexl.common.integration.coingecko.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "coingecko")
@Validated
@Getter
@RequiredArgsConstructor
@ConstructorBinding
public class CoingeckoProperties {

    @NotBlank
    private final String url;

    @NotBlank
    private final String coin;

    @NotBlank
    private final String marketChart;
}
