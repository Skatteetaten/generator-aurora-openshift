package <%=packageName%>.controllers;

import static no.skatteetaten.aurora.prometheus.collector.Operation.withMetrics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * An example controller that shows how to do a REST call and how to do an operation with a operations metrics
 * There should be a metric called http_client_requests http_server_requests and operations
 */
@RestController
public class ExampleController {

    private static final int SECOND = 1000;

    private RestTemplate restTemplate;

    public ExampleController(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/example/ip")
    public String ip() {
        JsonNode forEntity = restTemplate.getForObject("http://httpbin.org/ip", JsonNode.class);
        return forEntity.get("origin").textValue();
    }

    @GetMapping("/api/example/sometimes")
    public String example() {
        return withMetrics("sometimes", () -> {
            long sleepTime = (long) (Math.random() * SECOND);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Sleep interupted", e);
            }

            if (sleepTime % 2 == 0) {
                return "sometimes i succeed";
            } else {
                throw new RuntimeException("Sometimes i fail");
            }
        });
    }
}

