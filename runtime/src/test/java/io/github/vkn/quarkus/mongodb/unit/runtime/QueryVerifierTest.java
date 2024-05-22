package io.github.vkn.quarkus.mongodb.unit.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static io.github.vkn.quarkus.mongodb.unit.runtime.MongoDbUnitCommandListener.MongoCommand;
import static io.github.vkn.quarkus.mongodb.unit.runtime.QueryVerifier.assertCounts;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryVerifierTest {

    private final Predicate<Integer> violated = i -> true;
    private Config cfg;

    private static MongoDbQueryTest getAnnotation(String methodName) {
        try {
            return TestedClass.class.getDeclaredMethod(methodName)
                    .getAnnotation(MongoDbQueryTest.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        var annotation = getAnnotation("someTest");
        cfg = new Config(annotation);
    }

    @Test
    void expectedIsNull() {
        List<MongoCommand> commands = List.of();
        assertThatNoException().isThrownBy(
                () -> assertCounts(null, violated, "exactly", commands, cfg));
    }

    @Test
    void expectedIsNegative() {
        assertThatNoException().isThrownBy(
                () -> assertCounts(-1, violated, "exactly", List.of(), cfg));
    }

    @Test
    void isViolatedReturnsFalse() {
        Predicate<Integer> notViolated = i -> false;
        assertThatNoException().isThrownBy(
                ()-> assertCounts(1, notViolated, "exactly", List.of(), cfg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"exactly", "at least", "at most"})
    void isViolatedReturnsTrueAndConstrainsIsEmpty(String type) {
        List<MongoCommand> commands = List.of(new MongoCommand("mydb", "mycol", "update", "foo"));
        assertThatThrownBy(() -> assertCounts(1, violated, type, commands, cfg))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Mongodb extension requires %s 1 command, but count is 1".formatted(type))
                .hasMessageContaining("mydb", "mycol", "update", "foo");
    }

    @Test
    void isViolatedReturnsTrueAndConstrainsIsNotEmpty() {
        List<MongoCommand> commands = List.of(new MongoCommand("mydb", "mycol", "update", "foo"));
        assertThatThrownBy(() -> assertCounts(0, violated, "exactly", commands, cfg))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Mongodb extension requires exactly 0 commands, but count is 1");
    }

    @Test
    void commandsSizeIsGreaterThanOne() {
        List<MongoCommand> commands = List.of(
                new MongoCommand("mydb", "mycol", "update", "foo"),
                new MongoCommand("mydb", "mycol", "find", "foo"));
        assertThatThrownBy(() -> assertCounts(1, violated, "exactly", commands, cfg))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Mongodb extension requires exactly 1 command, but count is 2")
                .hasMessageContaining("mydb", "update", "find", "foo");
    }

    @Test
    void trimOutput() {
        Predicate<Integer> isViolated = i -> true;
        List<MongoCommand> commands = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            commands.add(new MongoCommand("mydb", "mycol", "update", "foo" + i));
        }
        assertThatThrownBy(() -> assertCounts(10, isViolated, "exactly", commands, cfg))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Mongodb extension requires exactly 10 commands, but count is 30")
                .hasMessageContaining("mydb", "update", "foo0", "foo1", "foo19")
                .hasMessageNotContaining("foo20");
    }

    @Test
    void withConstraints() {
        var annotation = getAnnotation("someTestWithCommandConstraint");
        var customConfig = new Config(annotation);
        List<MongoCommand> commands = List.of(new MongoCommand("mydb", "mycol", "delete", "foo"));
        assertThatThrownBy(() -> assertCounts(0, violated, "exactly", commands, customConfig))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Mongodb extension requires exactly 0 commands for delete, but count is 1");
    }

}