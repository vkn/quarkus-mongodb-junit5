package io.github.vkn.quarkus.mongodb.unit.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoDbQueryTest {

    /**
     * limit query count to collection name
     *
     * @return collection name
     */
    String collection() default "";

    /**
     * limit query count to db name
     *
     * @return db name
     */
    String db() default "";

    /**
     *
     * @return exact num of queries to be executed
     */
    int exactly() default -1;

    /**
     *
     * @return min num of queries to be executed
     */
    int atLeast() default -1;

    /**
     *
     * @return max num of queries to be executed
     */
    int atMost() default -1;
}
