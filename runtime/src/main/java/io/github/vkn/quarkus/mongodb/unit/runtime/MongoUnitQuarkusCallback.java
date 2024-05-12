package io.github.vkn.quarkus.mongodb.unit.runtime;

import io.quarkus.test.junit.callback.QuarkusTestAfterTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Implementation for quarkus test callbacks
 * Retrieves MongoDbUnitCommandListener from CDI container
 * and uses it to get executed mongodb commands
 */
public class MongoUnitQuarkusCallback implements QuarkusTestBeforeTestExecutionCallback, QuarkusTestAfterTestExecutionCallback {

    MongoDbUnitCommandListener mongoDbUnitCommandListener;
    private static final ConcurrentHashMap<String, MongoDbUnitCommandListener> STORE = new ConcurrentHashMap<>();

    private static Optional<MongoDbQueryTest> getAnnotation(QuarkusTestMethodContext context) {
        return Optional.ofNullable(context.getTestMethod().getAnnotation(MongoDbQueryTest.class));
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
        String db = getAnnotation(context).map(MongoDbQueryTest::db).orElse(null);
        String collection = getAnnotation(context).map(MongoDbQueryTest::collection).orElse(null);
        String commandName = getAnnotation(context).map(MongoDbQueryTest::commandName).orElse(null);
        long commandCount = listener.getCommands()
                .stream()
                .filter(mc -> db == null || db.isBlank() || db.equals(mc.db()))
                .filter(mc -> collection == null || collection.isBlank() || collection.equals(mc.collection()))
                .filter(mc -> commandName == null || commandName.isBlank() || commandName.equals(mc.name()))
                .count();
        listener.stop();
        int atLeast = getAnnotation(context).map(MongoDbQueryTest::atLeast).orElse(-1);
        int atMost = getAnnotation(context).map(MongoDbQueryTest::atMost).orElse(-1);
        long exactly = Long.valueOf(getAnnotation(context).map(MongoDbQueryTest::exactly).orElse(-1));
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
