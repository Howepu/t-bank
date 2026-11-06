package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.LinkResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LinkCounter {

    private final Map<String, Counter> linkCounters = new HashMap<>();

    public LinkCounter(MeterRegistry registry) {
        List.of("github", "stackoverflow").forEach(domain -> {
            linkCounters.put(
                    domain,
                    Counter.builder("links.count")
                            .tag("domain", domain)
                            .description("Total links count by domain")
                            .register(registry));
        });
    }

    public void updateLinkCounters(List<LinkResponse> links) {
        linkCounters.forEach((domain, counter) -> counter.increment(-counter.count()));

        links.forEach(link -> {
            String domain = extractDomain(link.url());
            linkCounters.getOrDefault(domain, linkCounters.get("other")).increment();
        });
    }

    private String extractDomain(String url) {
        if (url.contains("github.com")) return "github";
        if (url.contains("stackoverflow.com")) return "stackoverflow";
        return "other";
    }
}
