package io.github.vkn.quarkus.mongodb.unit.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

/**
 * The listener is storing started mongodb commands
 */
public class MongoDbUnitCommandListener implements CommandListener {
    private static final Logger LOGGER = Logger.getLogger(MongoDbUnitCommandListener.class.getName());
    private static final String KEY = "mongodb.command";
    List<MongoCommand> commands = new ArrayList<>();

    record MongoCommand(String db, String collection, String name, String command) {
    }

    public MongoDbUnitCommandListener() {
        LOGGER.log(Level.INFO, "MongoTracingCommandListener created");
    }

    void start() {
        commands = new ArrayList<>();
    }

    List<MongoCommand> getCommands() {
        return List.copyOf(commands);
    }

    void stop() {
        if (commands != null) {
            commands.clear();
        }
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
        LOGGER.log(Level.INFO, "commandStarted event {0}", event.getCommand());
        var commandName = event.getCommandName();
        commands.add(new MongoCommand(
                event.getDatabaseName(),
                event.getCommand().getString(commandName).getValue(),
                commandName,
                event.getCommand().toJson()));
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        LOGGER.log(Level.FINEST, "commandSucceeded event {0}", event.getCommandName());
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        LOGGER.log(Level.FINEST, "commandFailed event {0}", event.getCommandName());
    }

}
