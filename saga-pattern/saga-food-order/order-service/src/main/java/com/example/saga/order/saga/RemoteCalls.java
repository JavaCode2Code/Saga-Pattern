package com.example.saga.order.saga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** HTTP helper that turns every failure (4xx, 5xx, timeout, connection refused) into StepFailedException. */
@Component
public class RemoteCalls {
    private final RestClient http;
    private final ObjectMapper mapper;

    public RemoteCalls(ObjectMapper mapper) {
        this.mapper = mapper;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(5000);
        this.http = RestClient.builder().requestFactory(f).build();
    }

    public JsonNode post(String url, Object body) {
        try {
            return http.post().uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body == null ? Map.of() : body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new StepFailedException(messageOf(e));
        } catch (RestClientException e) {
            throw new StepFailedException("Service unavailable: " + e.getMessage());
        }
    }

    private String messageOf(RestClientResponseException e) {
        try {
            String m = mapper.readTree(e.getResponseBodyAsString()).path("message").asText("");
            if (!m.isBlank()) return m;
        } catch (Exception ignored) {
            // fall through
        }
        return e.getStatusCode() + " " + e.getStatusText();
    }
}
