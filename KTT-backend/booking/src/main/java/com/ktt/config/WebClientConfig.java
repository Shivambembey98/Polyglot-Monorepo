package com.ktt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.Base64;

	@Configuration
	public class WebClientConfig {

		private final String apiUrl = "https://apac.universal-api.pp.travelport.com/B2BGateway/connect/uAPI/AirService";
		private final String username = "Universal API/uAPI8523180071-c5c2b21b";  // Replace with your actual username
		private final String password = "t{4W7A}z%q";  // Replace with your actual password

		String credentials = username + ":" + password;
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

		@Bean
		public WebClient.Builder webClientBuilder() {
			return WebClient.builder()
					       .baseUrl(apiUrl)
					       .exchangeStrategies(ExchangeStrategies
							                           .builder()
							                           .codecs(codecs -> codecs
									                                             .defaultCodecs()
									                                             .maxInMemorySize(50 * 1024 * 1024))
							                           .build())
					       .defaultHeader("Content-Type", "text/xml")
					       .defaultHeader("Accept", "application/soap+xml")
					       .defaultHeader("Authorization", "Basic "+encodedCredentials)
					       .filter(logRequest());
		}

		private ExchangeFilterFunction logRequest() {
			return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
				System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
				clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
				return Mono.just(clientRequest);
			});
		}
	}

