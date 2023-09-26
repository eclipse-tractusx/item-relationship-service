/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.util;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTags;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

/**
 * This class provides a custom prometheus tag configuration for the request buckets.
 * The default configuration includes the query string in the URI tag. This leads
 * to lots of metric entries for each different call.
 */
@Service
public class CustomUriTagProvider implements RestTemplateExchangeTagsProvider {
    private static final String GLOBAL_ASSET_ID_REGEX = "urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    @Override
    public Iterable<Tag> getTags(final String urlTemplate, final HttpRequest request,
            final ClientHttpResponse response) {
        final List<Tag> tags = new ArrayList<>();
        // build default tags
        tags.add(RestTemplateExchangeTags.clientName(request));
        tags.add(RestTemplateExchangeTags.outcome(response));
        tags.add(RestTemplateExchangeTags.method(request));
        tags.add(RestTemplateExchangeTags.status(response));

        // only include path in the URI tag, not the query string
        final String path = request.getURI()
                             .getPath()
                             .replaceAll(GLOBAL_ASSET_ID_REGEX,
                                     "{uuid}");
        tags.add(RestTemplateExchangeTags.uri(path));

        return tags;
    }
}
