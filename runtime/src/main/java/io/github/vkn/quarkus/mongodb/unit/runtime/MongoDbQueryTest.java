package io.github.vkn.quarkus.mongodb.unit.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation enables and configures check on the number of queries
 * during the test execution
 */
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
     *  Check only this command name
     *  e.g. find, delete
     *
      * @return
     */
    String commandName() default "";

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
