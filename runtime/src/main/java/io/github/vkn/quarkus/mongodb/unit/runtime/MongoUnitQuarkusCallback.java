package io.github.vkn.quarkus.mongodb.unit.runtime;

import io.quarkus.test.junit.callback.QuarkusTestAfterTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import jakarta.enterprise.inject.spi.CDI;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Implementation for quarkus test callbacks
 * Retrieves MongoDbUnitCommandListener from CDI container
 * and uses it to get executed mongodb commands
 */
public class MongoUnitQuarkusCallback implements QuarkusTestBeforeTestExecutionCallback, QuarkusTestAfterTestExecutionCallback {

    MongoDbUnitCommandListener mongoDbUnitCommandListener;
    private static final ConcurrentHashMap<String, MongoDbUnitCommandListener> STORE = new ConcurrentHashMap<>();

    private static List<MongoDbQueryTest> getAnnotations(QuarkusTestMethodContext context) {
        return Optional.ofNullable(context.getTestMethod()
                .getAnnotationsByType(MongoDbQueryTest.class))
                .map(a -> Stream.of(a).toList())
                .orElse(List.of());
    }

    @Override
    public void beforeTestExecution(QuarkusTestMethodContext context) {
        mongoDbUnitCommandListener = getListener();
        mongoDbUnitCommandListener.start();
        STORE.put(context.getTestMethod().getName(), mongoDbUnitCommandListener);
    }

    MongoDbUnitCommandListener getListener() {
        return CDI.current().select(MongoDbUnitCommandListener.class).get();
    }

    @Override
    public void afterTestExecution(QuarkusTestMethodContext context) {
        MongoDbUnitCommandListener listener = STORE.remove(context.getTestMethod().getName());
        for (MongoDbQueryTest annotation : getAnnotations(context)) {
            var cfg = new Config(annotation);
            long commandCount = listener.getCommands()
                    .stream()
                    .filter(cfg::matchesDb)
                    .filter(cfg::matchesCollection)
                    .filter(cfg::matchesName)
                    .count();
            assertCounts(annotation, commandCount, cfg);
        }

        listener.stop();

    }

    private static void assertCounts(MongoDbQueryTest annotation, long commandCount, Config cfg) {
        int atLeast = annotation.atLeast();
        int atMost = annotation.atMost();
        long exactly = annotation.exactly();
        assertCounts(atLeast, cnt -> cnt < atLeast, "at least", commandCount, cfg);
        assertCounts(atMost, cnt -> cnt > atMost, "at most", commandCount, cfg);
        assertCounts(exactly, cnt -> cnt != exactly, "exactly", commandCount, cfg);
    }

    private static void assertCounts(Number expected, Predicate<? super Long> isViolated, String errorType, long commandCount, Config cfg) {
        if (expected != null && expected.intValue() > -1 && isViolated.test(commandCount)) {
            String constrains = cfg.constrains().isEmpty() ? "" : " for " + cfg.constrains();
            throw new AssertionError("Mongodb extension requires %s %d commands%s, but count is %d".formatted(errorType,
                    expected.longValue(), constrains, commandCount));
        }
    }
}
