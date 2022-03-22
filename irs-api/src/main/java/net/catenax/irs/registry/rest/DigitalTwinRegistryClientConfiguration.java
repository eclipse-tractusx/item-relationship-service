//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.registry.rest;

import feign.RequestInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/**
 *
 */
class DigitalTwinRegistryClientConfiguration {

   /**
    *
    * @return asd
    */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {
        return requestTemplate -> requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken());
    }

    private String getAccessToken() {
        // TODO: check how authorization against AAS Wrapper is gonna be implemented, right now (22.03) its not secured
        return StringUtils.EMPTY;
    }
}
