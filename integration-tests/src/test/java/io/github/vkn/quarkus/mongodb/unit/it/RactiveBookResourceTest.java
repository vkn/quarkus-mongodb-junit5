package io.github.vkn.quarkus.mongodb.unit.it;

import io.github.vkn.quarkus.mongodb.unit.runtime.MongoDbQueryTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class RactiveBookResourceTest {


    @BeforeEach
    void setUp() {
        TestUtils.insertBooks("/reactive-books");
    }

    @AfterEach
    void tearDown() {
        TestUtils.deleteBooks("/reactive-books");
    }

    @Test
    @MongoDbQueryTest(atLeast = 1)
    public void count() {
        given()
                .when().get("/reactive-books")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    @MongoDbQueryTest(atLeast = 1, collection = "my-reactive-collection")
    void countWithError() {
        given()
                .get("/reactive-books/invalid")
                .then()
                .assertThat()
                .statusCode(500);
    }

}
