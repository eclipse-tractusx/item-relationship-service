package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.concurrent.TimeUnit;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.Retryer;
import feign.okhttp.OkHttpClient;

import net.catenax.irs.configuration.ClientConfiguration;

/**
 * A Fein client builder with retrying feature
 */
public class ClientBuilder {
   public static <T> T createClient(Class<T> type, String uri) {
      final ClientConfiguration clientConfiguration = new ClientConfiguration();
      final OkHttpClient client = clientConfiguration.client();
      final Retryer retryer = new Retryer.Default(1000L, TimeUnit.SECONDS.toMillis(1000L), 3);

      return Feign.builder()
                  .client(client)
                  .encoder(new GsonEncoder())
                  .decoder(new GsonDecoder())
                  .retryer(retryer)
                  .errorDecoder(new CustomErrorDecoder())
                  .target(type, uri);
   }
}
