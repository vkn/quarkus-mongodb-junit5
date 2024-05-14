package io.github.vkn.quarkus.mongodb.unit.runtime;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

import static io.github.vkn.quarkus.mongodb.unit.runtime.MongoDbUnitCommandListener.MongoCommand;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

    @Test
    void constrains() {
        Config config = new Config(annotation(Map.of("db", "userdb", "commandName", "find")));
        assertThat(config.constrains()).isEqualTo("userdb, find");
    }

    @Test
    void matchesCollection() {
        var config = new Config(annotation(Map.of("collection", "books")));
        var command = new MongoCommand("userDb", "books", null, null);
        assertThat(config.matchesCollection(command)).isTrue();
    }

    @Test
    void matchesName() {
        var config = new Config(annotation(Map.of("commandName", "delete")));
        var command = new MongoCommand("userDb", "books", "delete", null);
        assertThat(config.matchesName(command)).isTrue();
    }

    @Test
    void matchesDb() {
        var config = new Config(annotation(Map.of("db", "userDb")));
        var command = new MongoCommand("userDb", null, null, null);
        assertThat(config.matchesDb(command)).isTrue();
    }

    @Test
    void matchesNullDb() {
        var config = new Config(annotation(Map.of("commandName", "find")));
        var command = new MongoCommand("userDb", null, null, null);
        assertThat(config.matchesDb(command)).isTrue();
    }

    @Test
    void matchesEmptyDb() {
        var config = new Config(annotation(Map.of("db", "")));
        var command = new MongoCommand("userDb", null, null, null);
        assertThat(config.matchesDb(command)).isTrue();
    }

    @Test
    void matchesBlankDb() {
        var config = new Config(annotation(Map.of("db", " ")));
        var command = new MongoCommand("userDb", null, null, null);
        assertThat(config.matchesDb(command)).isTrue();
    }

    @Test
    void notMatchesDb() {
        var config = new Config(annotation(Map.of("db", "foo")));
        var command = new MongoCommand("userDb", null, null, null);
        assertThat(config.matchesDb(command)).isFalse();
    }

    private static MongoDbQueryTest annotation(Map<String, Object> values) {
        return (MongoDbQueryTest) Proxy.newProxyInstance(
                MongoDbQueryTest.class.getClassLoader(),
                new Class[]{MongoDbQueryTest.class},
                (proxy, method, args) -> values.getOrDefault(method.getName(), method.getDefaultValue()));
    }


}