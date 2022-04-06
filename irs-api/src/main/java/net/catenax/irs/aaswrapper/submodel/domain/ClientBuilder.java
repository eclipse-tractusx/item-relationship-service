//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.concurrent.TimeUnit;

import feign.Feign;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import net.catenax.irs.configuration.ClientConfiguration;
import org.springframework.stereotype.Component;

/**
 * A Fein client builder with retrying feature
 */

@Component
@RequiredArgsConstructor
public class ClientBuilder {
    private final ClientConfiguration clientConfiguration;

    public <T> T createClient(final Class<T> type, final String uri) {
        final OkHttpClient client = this.clientConfiguration.submodelHttpClient();
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
