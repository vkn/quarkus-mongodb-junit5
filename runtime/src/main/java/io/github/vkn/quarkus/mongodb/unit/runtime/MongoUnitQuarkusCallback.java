package io.github.vkn.quarkus.mongodb.unit.runtime;

import io.quarkus.test.junit.callback.QuarkusTestAfterTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import jakarta.enterprise.inject.spi.CDI;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for quarkus test callbacks
 * Retrieves MongoDbUnitCommandListener from CDI container
 * and uses it to get executed mongodb commands
 */
public class MongoUnitQuarkusCallback implements QuarkusTestBeforeTestExecutionCallback, QuarkusTestAfterTestExecutionCallback {

    MongoDbUnitCommandListener mongoDbUnitCommandListener;
    private static final ConcurrentHashMap<String, MongoDbUnitCommandListener> STORE = new ConcurrentHashMap<>();

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
        QueryVerifier.verify(context, listener);
    }

}
