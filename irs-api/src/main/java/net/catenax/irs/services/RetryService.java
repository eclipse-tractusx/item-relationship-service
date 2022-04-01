package net.catenax.irs.services;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import feign.Response;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.catenax.irs.aaswrapper.submodel.domain.AssemblyPartRelationship;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClient;

import net.catenax.irs.exceptions.BadRequestException;
import net.catenax.irs.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for retrying retrieving the submodel json
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {
   private final SubmodelClient client;

   public AssemblyPartRelationship retrieveSubmodel() {
      final Retry retry = this.getConfiguration();

      return Retry.decorateCheckedSupplier(
            retry,
            () -> client.getSubmodel("", "", "")
      )
            .unchecked()
            .apply();
   }

   private Retry getConfiguration() {
      final RetryConfig config = RetryConfig.<Response>custom()
                                            .maxAttempts(3)
                                            .waitDuration(Duration.ofMillis(1000))
                                            .retryOnResult(response -> response.status() == 500)
                                            .retryOnException(e -> e instanceof BadRequestException)
                                            .retryOnException(e -> e instanceof NotFoundException)
                                            .retryExceptions(IOException.class, TimeoutException.class)
                                            .ignoreExceptions()
                                            .failAfterMaxAttempts(true)
                                            .build();

      final RetryRegistry registry = RetryRegistry.of(config);

      return registry.retry("submodelRetrying", config);
   }
}
