package io.github.vkn.quarkus.mongodb.unit.it;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Book(String author, String title) {

}
