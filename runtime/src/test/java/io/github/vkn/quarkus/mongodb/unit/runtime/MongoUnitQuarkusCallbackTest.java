package io.github.vkn.quarkus.mongodb.unit.runtime;

import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoUnitQuarkusCallbackTest {

    @Test
    void getListener() {
        var callback = new TestMongoUnitQuarkusCallback();
        assertThat(callback.getListener()).isNotNull();
    }

    @Test
    void beforeTestExecution() throws Exception {
        QuarkusTestMethodContext context = mock();
        Method method = TestedClass.class.getDeclaredMethod("someTest");
        when(context.getTestMethod()).thenReturn(method);
        var callback = new TestMongoUnitQuarkusCallback();
        callback.beforeTestExecution(context);
    }

    @Test
    void afterTestExecutionMustFail() throws Exception {
        QuarkusTestMethodContext context = mock();
        Method method = TestedClass.class.getDeclaredMethod("someFailingTest");
        when(context.getTestMethod()).thenReturn(method);
        var callback = new TestMongoUnitQuarkusCallback();
        callback.beforeTestExecution(context);
        assertThatThrownBy(() -> callback.afterTestExecution(context))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Mongodb extension requires exactly 1 command, but count is 0");
    }

    @Test
    void afterTestExecution() throws Exception {
        QuarkusTestMethodContext context = mock();
        Method method = TestedClass.class.getDeclaredMethod("someTest");
        when(context.getTestMethod()).thenReturn(method);
        var callback = new TestMongoUnitQuarkusCallback();
        callback.beforeTestExecution(context);
        assertThatNoException().isThrownBy(() -> callback.afterTestExecution(context));
    }

    @Test
    void afterTestExecutionWithRepeatedAnnotation() throws Exception {
        QuarkusTestMethodContext context = mock();
        Method method = TestedClass.class.getDeclaredMethod("someTestWithRepeatedAnnotation");
        when(context.getTestMethod()).thenReturn(method);
        var callback = new TestMongoUnitQuarkusCallback();
        callback.beforeTestExecution(context);
        assertThatNoException().isThrownBy(() -> callback.afterTestExecution(context));
    }
    @Test
    void afterTestExecutionWthoutMongoTestAnnotation() throws Exception {
        QuarkusTestMethodContext context = mock();
        Method method = TestedClass.class.getDeclaredMethod("noAnnotation");
        when(context.getTestMethod()).thenReturn(method);
        var callback = new TestMongoUnitQuarkusCallback();
        callback.beforeTestExecution(context);
        assertThatNoException().isThrownBy(() -> callback.afterTestExecution(context));
    }

    private static class TestMongoUnitQuarkusCallback extends MongoUnitQuarkusCallback {

        @Override
        MongoDbUnitCommandListener getListener() {
            return new MongoDbUnitCommandListener();
        }
    }
}