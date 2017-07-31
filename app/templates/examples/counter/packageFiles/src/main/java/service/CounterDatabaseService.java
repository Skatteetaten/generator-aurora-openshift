package <%=packageName%>.service;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * An example service that demonstrates basic database operations.
 * For some reason if this is called CounterService it will not load?
 */
@Service
public class CounterDatabaseService {

    private final JdbcTemplate jdbcTemplate;

    public CounterDatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Map<String, Object> getAndIncrementCounter() {
        Map<String, Object> counter = jdbcTemplate.queryForMap("SELECT value FROM counter FOR UPDATE OF value");
        jdbcTemplate.update("UPDATE counter SET value=value+1");
        return counter;
    }

    public int getCounter() {
        Integer integer = jdbcTemplate.queryForObject("SELECT value FROM counter", Integer.class);
        if (integer == null) {
            return 0;
        }
        return integer;
    }
}
