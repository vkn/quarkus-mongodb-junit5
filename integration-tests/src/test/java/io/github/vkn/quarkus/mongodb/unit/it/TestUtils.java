package io.github.vkn.quarkus.mongodb.unit.it;

import io.restassured.common.mapper.TypeRef;
import org.assertj.core.api.Assertions;

import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The code is based on example from quarkus integration tests repository
 */
class TestUtils {
    private static final TypeRef<List<Book>> bookListType = new TypeRef<>() {
    };

    static void insertBooks(String endpoint) {
        deleteBooks(endpoint);

        assertThat(get(endpoint).as(bookListType)).isEmpty();
        saveBook(new Book("Victor Hugo", "Les Misérables"), endpoint);
        saveBook(new Book("Victor Hugo", "Notre-Dame de Paris"), endpoint);
        saveBook(new Book("Charles Baudelaire", "Les fleurs du mal"), endpoint);
        await().atMost(Duration.ofSeconds(60L))
                .untilAsserted(() -> assertThat(get(endpoint).as(bookListType)).hasSize(3));
        List<Book> books = get("%s/Victor Hugo".formatted(endpoint))
                .as(bookListType);
        assertThat(books).hasSize(2);
    }

    static void deleteBooks(String endpoint) {
        given()
                .delete(endpoint)
                .then()
                .assertThat()
                .statusCode(200);
    }

    private static void saveBook(Book book, String endpoint) {
        given()
                .header("Content-Type", "application/json")
                .body(book)
                .post(endpoint)
                .then().assertThat().statusCode(202);
    }
}
