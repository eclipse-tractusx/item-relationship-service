//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Application entry point.
 */
@SpringBootApplication
@EnableFeignClients
public class IrsApplication {

    /** The IRS API version. */
    public static final String API_VERSION = "v0.1";

    /** The URL prefix for IRS API URLs. */
    public static final String API_PREFIX = "api/" + API_VERSION;

    /**
     * Entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(IrsApplication.class, args);
    }
}
