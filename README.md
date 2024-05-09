# Quarkus Mongodb Junit5 Extension

The extension allows to verify number of executed queries to [mongo db](https://www.mongodb.com/) during 
a [quarkus](https://quarkus.io/) test execution 



## Installation

Clone the repository and run `mvn install`, then add the dependency in your pom.xml

```xml
<dependency>
    <groupId>io.github.vkn</groupId>
    <artifactId>quarkus-mongodb-junit5</artifactId>
    <version>0.0.1</version>
    <scope>test</scope>
</dependency>
```


### Usage
To get started, annotate your test method with `@MongoDbQueryTest`. See `BookResourceTest` in integration-tests module
and java docs of `@MongoDbQueryTest` for more information


```java
@Test
@MongoDbQueryTest(exactly = 1, collection = "my-collection")
public void exactly() {
    given()
            .when().get("/books")
            .then()
            .statusCode(200)
            .body(not(empty()));
}
```

### Contributing
Contributions are welcome! If you have suggestions for improvements or encounter any issues,
please feel free to open an issue or submit a pull request.

### License
This project is licensed under the Apache Licence 2.0. See the LICENSE file for more details.

