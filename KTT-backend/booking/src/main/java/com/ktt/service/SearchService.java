package com.ktt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import java.util.Base64;

@Service
public class SearchService {

	private final WebClient webClient;
	private final String apiUrl = "https://apac.universal-api.pp.travelport.com/B2BGateway/connect/uAPI/AirService";
	private final String universalRecordUrl = "https://apac.universal-api.pp.travelport.com/B2BGateway/connect/uAPI/UniversalRecordService";
	private final String username = "Universal API/uAPI8523180071-c5c2b21b";
	private final String password = "t{4W7A}z%q";

	@Autowired
	public SearchService(WebClient.Builder webClientBuilder) {
		// Create basic auth header
		String auth = username + ":" + password;
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		String authHeader = "Basic " + encodedAuth;

		this.webClient = webClientBuilder
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(configurer -> configurer.defaultCodecs()
								.maxInMemorySize(50 * 1024 * 1024))
						.build())
				.defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
				.build();
	}

	public Flux<String> streamSearchResponse(String soapRequestXml) {
		return webClient.post()
				.uri(apiUrl)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
				.bodyValue(soapRequestXml)
				.retrieve()
				.bodyToFlux(String.class)
				.doOnNext(chunk -> System.out.println("Received chunk: " + chunk))
				.doOnError(error -> System.err.println("Error: " + error.getMessage()));
	}

	public Flux<String> AirRetrieveSearchResponse(String soapRequestXml) {
		return webClient.post()
				.uri(universalRecordUrl)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_VALUE)
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
				.bodyValue(soapRequestXml)
				.retrieve()
				.bodyToFlux(String.class)
				.doOnNext(chunk -> System.out.println("Received chunk: " + chunk))
				.doOnError(error -> System.err.println("Error: " + error.getMessage()));
	}
}