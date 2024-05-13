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
        mongoDbUnitCommandListener = CDI.current().select(MongoDbUnitCommandListener.class).get();
        mongoDbUnitCommandListener.start();
        STORE.put(context.getTestMethod().getName(), mongoDbUnitCommandListener);
    }

    @Override
    public void afterTestExecution(QuarkusTestMethodContext context) {
        MongoDbUnitCommandListener listener = STORE.remove(context.getTestMethod().getName());
        for (MongoDbQueryTest annotation : getAnnotations(context)) {
            String db = annotation.db();
            var collection = annotation.collection();
            var commandName = annotation.commandName();
            long commandCount = listener.getCommands()
                    .stream()
                    .filter(mc -> db == null || db.isBlank() || db.equals(mc.db()))
                    .filter(mc -> collection == null || collection.isBlank() || collection.equals(mc.collection()))
                    .filter(mc -> commandName == null || commandName.isBlank() || commandName.equals(mc.name()))
                    .count();
            assertCounts(annotation, commandCount);
        }

        listener.stop();

    }

    private static void assertCounts(MongoDbQueryTest annotation, long commandCount) {
        int atLeast = annotation.atLeast();
        int atMost = annotation.atMost();
        long exactly = annotation.exactly();
        assertCounts(atLeast, cnt -> cnt < atLeast, "at least", commandCount);
        assertCounts(atMost, cnt -> cnt > atMost, "at most", commandCount);
        assertCounts(exactly, cnt -> cnt != exactly, "exactly", commandCount);
    }

    private static void assertCounts(Number expected, Predicate<? super Long> isViolated, String errorType, long commandCount) {
        if (expected != null && expected.intValue() > -1 && isViolated.test(commandCount)) {
            throw new AssertionError("Mongodb extension requires %s %d commands, but count is %d".formatted(errorType,
                    expected.longValue(), commandCount));
        }
    }
}
