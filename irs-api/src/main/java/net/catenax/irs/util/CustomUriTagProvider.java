//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.util;

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
