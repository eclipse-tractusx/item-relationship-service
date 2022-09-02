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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Config values for blobstore
 */
@Configuration
@ConfigurationProperties(prefix = "blobstore")
@Getter
@Setter
public class BlobstoreConfiguration {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
