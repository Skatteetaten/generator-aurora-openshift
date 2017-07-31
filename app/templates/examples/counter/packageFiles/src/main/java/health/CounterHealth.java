package <%=packageName%>.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import <%=packageName%>.service.CounterDatabaseService;

/**
 * A sample custom health check. You can add your own health checks that verifies the proper operational status of your
 * application.
 */
@Component
public class CounterHealth extends AbstractHealthIndicator {

    private final CounterDatabaseService counterDatabaseService;

    public CounterHealth(CounterDatabaseService counterDatabaseService) {
        this.counterDatabaseService = counterDatabaseService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        int currentValue = counterDatabaseService.getCounter();

        if (currentValue % 2 == 0) {
            builder.status("OBSERVE").withDetail("message", "Partall antall i teller")
                .withDetail("Antall", currentValue);
        } else {
            builder.up()
                .withDetail("Antall", currentValue);
        }
    }
}
