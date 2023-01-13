package com.cleevio.vexl.common.util;

import com.cleevio.vexl.common.exception.InvalidResponseFromIntegrationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorHandlerUtil {

	public static Mono<Throwable> defaultHandler(ClientResponse response, String from) {
		return defaultHandler(response, from, null);
	}

	public static Mono<Throwable> defaultHandler(ClientResponse response, String from, @Nullable Object input) {
		return response.bodyToMono(String.class)
				.defaultIfEmpty("[EMPTY BODY]")
				.flatMap(body -> {
					if (input != null) {
						log.warn("Received error from {}: {} Our input was: {}.", from, body, input);
					} else {
						log.warn("Received error from {}: {}.", from, body);
					}
					return Mono.error(() -> new InvalidResponseFromIntegrationException(body));
				});
	}
}
