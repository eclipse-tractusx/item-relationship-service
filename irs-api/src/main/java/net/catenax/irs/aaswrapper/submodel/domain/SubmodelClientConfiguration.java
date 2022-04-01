package net.catenax.irs.aaswrapper.submodel.domain;

import feign.codec.ErrorDecoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubmodelClientConfiguration {

   @Bean
   public ErrorDecoder errorDecoder() {
      return new CustomErrorDecoder();
   }
}
