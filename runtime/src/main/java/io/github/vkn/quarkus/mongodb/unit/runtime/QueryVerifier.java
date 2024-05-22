package io.github.vkn.quarkus.mongodb.unit.runtime;

import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.vkn.quarkus.mongodb.unit.runtime.MongoDbUnitCommandListener.MongoCommand;

class QueryVerifier {
    private static final long MAX_COMMANDS_OUTPUT = 20L;

    private QueryVerifier() {
    }

    private static List<MongoDbQueryTest> getAnnotations(Method method) {
        return Optional.ofNullable(method)
                .map(mtd -> mtd.getAnnotationsByType(MongoDbQueryTest.class))
                .map(a -> Stream.of(a).toList())
                .orElse(List.of());
    }

    static void verify(QuarkusTestMethodContext context, MongoDbUnitCommandListener listener) {
        for (var annotation : getAnnotations(context.getTestMethod())) {
            var cfg = new Config(annotation);
            List<MongoCommand> commands = listener.getCommands()
                    .stream()
                    .filter(cfg::matchesDb)
                    .filter(cfg::matchesCollection)
                    .filter(cfg::matchesName)
                    .toList();
            assertCounts(annotation, commands, cfg);
        }

        listener.stop();
    }

    private static void assertCounts(MongoDbQueryTest annotation, List<MongoCommand> commands, Config cfg) {
        int atLeast = annotation.atLeast();
        int atMost = annotation.atMost();
        long exactly = annotation.exactly();
        assertCounts(atLeast, cnt -> cnt < atLeast, "at least", commands, cfg);
        assertCounts(atMost, cnt -> cnt > atMost, "at most", commands, cfg);
        assertCounts(exactly, cnt -> cnt != exactly, "exactly", commands, cfg);
    }

    static void assertCounts(Number expected,
                             Predicate<Integer> isViolated,
                             String errorType,
                             List<MongoCommand> commands,
                             Config cfg) {
        if (expected == null || expected.intValue() < 0 || !isViolated.test(commands.size())) {
            return;
        }
        String constrains = cfg.constrains().isEmpty() ? "" : " for " + cfg.constrains();
        String suffix = expected.intValue() == 1 ? "" : "s";
        var msg = "Mongodb extension requires %s %d command%s%s, but count is %d%n%s".formatted(
                errorType, expected.longValue(), suffix, constrains, commands.size(), commandsAsString(commands));
        System.out.println();
        System.out.println(msg);
        System.out.println();
        throw new AssertionError(msg);
    }

    private static String commandsAsString(List<MongoCommand> commands) {
        var commandsAsString = commands.stream()
                .map(Record::toString)
                .limit(MAX_COMMANDS_OUTPUT)
                .collect(Collectors.joining(System.lineSeparator()));
        if (commands.size() > MAX_COMMANDS_OUTPUT) {
            commandsAsString += "%s...%sremoved %d more commands from the output".formatted(
                    System.lineSeparator(), System.lineSeparator(), commands.size() - MAX_COMMANDS_OUTPUT);
        }
        return commandsAsString;
    }

}
