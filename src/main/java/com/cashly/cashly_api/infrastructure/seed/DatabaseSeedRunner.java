package com.cashly.cashly_api.infrastructure.seed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(name = "cashly.seed.enabled", havingValue = "true")
public class DatabaseSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory
            .getLogger(DatabaseSeedRunner.class);

    private final List<SeedStep> steps;
    private final TransactionTemplate transactions;

    public DatabaseSeedRunner(List<SeedStep> steps,
            TransactionTemplate transactions) {
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(SeedStep::version)).toList();
        this.transactions = transactions;
    }

    @Override
    public void run(String... args) {
        List<String> failures = new ArrayList<>();
        for (SeedStep step : steps) {
            String label = String.format("V%02d", step.version());
            log.info("Running database seed step {}", label);
            try {
                transactions.executeWithoutResult(status -> step.seed());
            } catch (RuntimeException exception) {
                failures.add(label);
                log.error("Database seed step {} failed; continuing with the "
                        + "remaining steps", label, exception);
            }
        }
        if (failures.isEmpty()) {
            log.info("Database seed completed ({} steps)", steps.size());
        } else {
            log.error("Database seed finished with {} failed step(s): {}",
                    failures.size(), String.join(", ", failures));
        }
    }
}
