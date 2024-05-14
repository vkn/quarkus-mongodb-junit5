package io.github.vkn.quarkus.mongodb.unit.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

record Config(MongoDbQueryTest annotation) {

    String constrains() {
        List<String> tmp = new ArrayList<>(6);
        Optional.ofNullable(annotation.db()).filter(Predicate.not(String::isBlank))
                .ifPresent(tmp::add);
        Optional.ofNullable(annotation.collection()).filter(Predicate.not(String::isBlank))
                .ifPresent(tmp::add);
        Optional.ofNullable(annotation.commandName()).filter(Predicate.not(String::isBlank))
                .ifPresent(tmp::add);
        return String.join(", ", tmp);
    }

    boolean matchesDb(MongoDbUnitCommandListener.MongoCommand commandDb) {
        var db = annotation.db();
        return db == null || db.isBlank() || db.equals(commandDb.db());
    }

    boolean matchesCollection(MongoDbUnitCommandListener.MongoCommand commandDb) {
        String collection = annotation.collection();
        return collection == null || collection.isBlank() || collection.equals(commandDb.collection());
    }

    boolean matchesName(MongoDbUnitCommandListener.MongoCommand commandDb) {
        String name = annotation.commandName();
        return name == null || name.isBlank() || name.equals(commandDb.name());
    }
}
