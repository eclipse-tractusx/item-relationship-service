//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.configuration;

import java.net.URL;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IRS configuration settings. Automatically populated by Spring from application.yml
 * and other configuration sources.
 */
@Component
@ConfigurationProperties(prefix = "irs")
@Data
public class IrsConfiguration {

    /**
     * The Base URL at which the API is externally accessible. Used in generated OpenAPI definition.
     */
    private URL apiUrl;

}
