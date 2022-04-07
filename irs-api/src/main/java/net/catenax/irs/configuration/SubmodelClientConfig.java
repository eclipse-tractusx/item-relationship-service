//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import java.util.concurrent.TimeUnit;

import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import net.catenax.irs.aaswrapper.submodel.domain.CustomErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure the submodel httpClient
 */
@Configuration
public class SubmodelClientConfig {
    private static final long PERIOD = 1000L;
    private static final long DURATION = 1000L;
    private static final int MAXATTEMPTS = 3;

    @Bean
    public OkHttpClient submodelHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(PERIOD, TimeUnit.SECONDS.toMillis(DURATION), MAXATTEMPTS);
    }

    @Bean
    public Encoder encoder() {
        return new GsonEncoder();
    }

    @Bean
    public Decoder decoder() {
        return new GsonDecoder();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
