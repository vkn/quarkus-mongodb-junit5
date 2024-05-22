package io.github.vkn.quarkus.mongodb.unit.runtime;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wildfly.common.Assert.assertTrue;

class MongoDbUnitCommandListenerTest {

    private CommandStartedEvent commandStartedEvent;

    private MongoDbUnitCommandListener listener;

    @BeforeEach
    void setUp() {
        commandStartedEvent = mock();
        when(commandStartedEvent.getCommandName()).thenReturn("find");
        when(commandStartedEvent.getDatabaseName()).thenReturn("testDB");
        BsonDocument commandDocument = new BsonDocument("find", new BsonString("testCollection"));
        when(commandStartedEvent.getCommand()).thenReturn(commandDocument);

        listener = new MongoDbUnitCommandListener();
        listener.start();  // Resets the commands list
    }

    @Test
    void testCommandStarted() {
        listener.commandStarted(commandStartedEvent);

        assertThat(listener.getCommands()).hasSize(1);
        MongoDbUnitCommandListener.MongoCommand command = listener.getCommands().get(0);
        assertThat(command.db()).isEqualTo("testDB");
        assertThat(command.collection()).isEqualTo("testCollection");
        assertThat(command.name()).isEqualTo("find");
    }

    @Test
    void start() {
        listener.commandStarted(commandStartedEvent);
        assertThat(listener.getCommands()).hasSize(1);

        listener.start();
        assertTrue(listener.getCommands().isEmpty());
    }

    @Test
    void stop() {
        listener.commandStarted(commandStartedEvent);
        assertThat(listener.getCommands()).hasSize(1);

        listener.stop();
        assertTrue(listener.getCommands().isEmpty());
    }

    @Test
    void commandSucceeded() {
        CommandSucceededEvent event = mock();
        assertThatNoException().isThrownBy(() -> listener.commandSucceeded(event));
    }

    @Test
    void commandFailed() {
        CommandFailedEvent event = mock();
        assertThatNoException().isThrownBy(() -> listener.commandFailed(event));
    }
}