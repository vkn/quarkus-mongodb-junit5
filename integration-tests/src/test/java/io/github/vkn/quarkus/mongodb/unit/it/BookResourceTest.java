package io.github.vkn.quarkus.mongodb.unit.it;

import io.github.vkn.quarkus.mongodb.unit.runtime.MongoDbQueryTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

/**
 * The test is based on example from quarkus integration tests repository
 */
@QuarkusTest
class BookResourceTest {


    @BeforeEach
    void setUp() {
        TestUtils.insertBooks("/books");
    }

    @AfterEach
    void tearDown() {
        TestUtils.deleteBooks("/books");
    }

    @Test
    @MongoDbQueryTest(exactly = 1, collection = "my-collection")
    public void exactly() {
        given()
                .when().get("/books")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    @MongoDbQueryTest(exactly = 1, collection = "my-collection")
    public void atMost() {
        given()
                .when().get("/books")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    @MongoDbQueryTest(atLeast = 1, collection = "my-collection")
    public void atLeast() {
        given()
                .when().get("/books")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    @MongoDbQueryTest(atLeast = 1, collection = "my-collection")
    void countWithError() {
        given()
                .get("/books/invalid")
                .then()
                .assertThat()
                .statusCode(500);
    }
}
