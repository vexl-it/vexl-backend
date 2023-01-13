package com.cleevio.vexl.common.integration.sentry;

import com.cleevio.vexl.common.exception.ApiException;
import io.sentry.SentryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SentryEventProcessor {

	@Bean
	public SentryOptions.BeforeSendCallback customBeforeSendCallback() {
		return (event, hint) -> {
			Throwable rootCause = event.getThrowable();

			if (rootCause == null) {
				return event;
			}

			while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
				if (rootCause.getMessage() != null && rootCause.getMessage().contains("Broken pipe")) {
					return null;
				}

				rootCause = rootCause.getCause();
			}

			if (rootCause instanceof ApiException apiException && !apiException.shouldBeLogged()) {
				return null;
			}

			return event;
		};
	}
}

