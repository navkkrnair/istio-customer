package com.istio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import io.micrometer.core.annotation.Timed;
import io.opentracing.Tracer;

@RestController
@RequestMapping("/customer")
public class CustomerController
{
	private static final String RESPONSE_STRING_FORMAT = "customer => %s\n";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RestTemplate restTemplate;

	@Value("${preferences.api.url:http://preference:8080}")
	private String remoteURL;

	@Autowired
	private Tracer tracer;

	public CustomerController(RestTemplate restTemplate)
	{
		this.restTemplate = restTemplate;
	}

	@Timed(value = "customer.get.request", histogram = true, extraTags =
	{ "version", "1.0" }, percentiles =
	{ 0.95, 0.99 })

	@GetMapping("/get")
	public ResponseEntity<String> getCustomer(@RequestHeader("User-Agent") String userAgent,
			@RequestHeader(value = "user-preference", required = false) String userPreference)
	{
		logger.info(">> getCustomer() called");

		tracer.activeSpan().setBaggageItem("user-agent", userAgent);
		if (userPreference != null && !userPreference.isEmpty())
		{
			tracer.activeSpan().setBaggageItem("user-preference", userPreference);
		}
		logger.info(">> Calling {}", remoteURL);
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(remoteURL, String.class);
		String                 response       = responseEntity.getBody();
		return ResponseEntity.ok(String.format(RESPONSE_STRING_FORMAT, response.trim()));

	}

	@ExceptionHandler
	public ResponseEntity<String> handleException(HttpStatusCodeException ex)
	{
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body(
					String.format(
							RESPONSE_STRING_FORMAT,
								String.format("%d %s", ex.getRawStatusCode(), createHttpErrorResponseString(ex))));
	}

	private String createHttpErrorResponseString(HttpStatusCodeException ex)
	{
		return ex.getResponseBodyAsString().trim();

	}

}
