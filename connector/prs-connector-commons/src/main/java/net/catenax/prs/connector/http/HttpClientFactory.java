//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.http;

import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import io.micrometer.jmx.JmxMeterRegistry;
import net.catenax.prs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * EDC core OkHttpClient does not have an event listener.
 * This factory class creates OkHttpClient with metric event listener.
 */
@ExcludeFromCodeCoverageGeneratedReport
public final class HttpClientFactory {

    /**
     * Default timeout for http client. Value is kept same as we have in edc core.
     */
    private static final int DEFAULT_TIMEOUT = 30;
    /**
     * HTTP client jmx metric name.
     */
    private static final String METRIC_NAME = "okhttp3.monitor";

    /**
     * Creates OkHttpClient with metric event listener.
     * @param meterRegistry Micrometer registry. See {@link JmxMeterRegistry}
     * @return see {@link OkHttpClient}.
     */
    public OkHttpClient okHttpClient(final JmxMeterRegistry meterRegistry) {

        return new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .eventListener(OkHttpMetricsEventListener
                        .builder(meterRegistry, METRIC_NAME)
                        .build())
                .build();
    }


}
