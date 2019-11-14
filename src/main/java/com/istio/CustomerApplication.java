package com.istio;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.MappedInterceptor;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootApplication
public class CustomerApplication
{
	private static final Logger logger = LoggerFactory.getLogger(CustomerApplication.class);

	public static void main(String[] args)
	{
		SpringApplication app = new SpringApplication(CustomerApplication.class);
		app.addListeners((e) ->
		{
			if (e instanceof ApplicationReadyEvent)
			{
				File file = new File(File.separatorChar + "var" + File.separatorChar + "ready");
				logger.info("Creating file at: " + file);
				if (!(file.exists()))
				{
					try
					{
						file.createNewFile();
						logger.info("created file: " + file);
					}
					catch (IOException e1)
					{
						logger.info("Creating file failed");
					}

				}
			}
		});
		app.run(args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder)
	{
		return restTemplateBuilder.build();
	}

	@Bean
	public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(MeterRegistry registry)
	{
		return registry1 ->
		{
			registry.config().commonTags("application", "customer-service");
		};
	}

	@Bean
	public MappedInterceptor metricInterceptor(MeterRegistry registry)
	{
		return new MappedInterceptor(new String[]
		{ "/customer/**" }, new ToDoMetricsInterceptor(registry));
	}

}
