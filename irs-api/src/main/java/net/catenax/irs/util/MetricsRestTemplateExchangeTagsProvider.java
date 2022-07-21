package net.catenax.irs.util;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTags;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

@Service
public class MetricsRestTemplateExchangeTagsProvider implements RestTemplateExchangeTagsProvider {

    @Override
    public Iterable<Tag> getTags(final String urlTemplate, final HttpRequest request,
            final ClientHttpResponse response) {
        List<Tag> tags = new ArrayList<>();
        tags.add(RestTemplateExchangeTags.clientName(request));
        tags.add(RestTemplateExchangeTags.outcome(response));
        tags.add(RestTemplateExchangeTags.method(request));
        tags.add(RestTemplateExchangeTags.status(response));
        tags.add(RestTemplateExchangeTags.uri(request.getURI().getPath()));
        return tags;
    }
}
