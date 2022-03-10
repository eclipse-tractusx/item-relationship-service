//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@SpringBootApplication
public class BrokerProxyApplication {

    /**
     * The BrokerProxy API version.
     */
    public static final String API_VERSION = "v0.1";

    /**
     * The URL prefix for BrokerProxy API URLs.
     */
    public static final String API_PREFIX = "broker-proxy/" + API_VERSION;

    /**
     * Entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(BrokerProxyApplication.class, args);
    }
}
