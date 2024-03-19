package com.example.apigateway;

import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class GatewayController {


	private final EurekaClient eurekaClient;

	public GatewayController(EurekaClient eurekaClient) {
		this.eurekaClient = eurekaClient;
	}

	@GetMapping("/persons")
	public Flux<String> fallback() {
		var services = eurekaClient.getApplication("echo")
				.getInstances();

		var identity = Flux.just("");

		return services.stream()
				.peek(service -> log.info("Service: {}", service))
				.map(service -> "http://" + service.getHostName() + ":" + service.getPort() + "/persons")
				.map(url -> WebClient.create().get().uri(url).retrieve().bodyToMono(String.class))
				.reduce(
						identity,
						Flux::mergeWith,
						Flux::mergeWith
				);
	}
}
