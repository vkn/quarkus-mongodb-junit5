package io.github.vkn.quarkus.mongodb.unit.deployment;

import io.github.vkn.quarkus.mongodb.unit.runtime.MongoDbUnitCommandListener;
import io.github.vkn.quarkus.mongodb.unit.runtime.MongoUnitQuarkusCallback;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

class MongodbUnitProcessor {

    private static final String FEATURE = "quarkus-mongodb-junit5";
    private static final String AFTER_CALLBACK = "io.quarkus.test.junit.callback.QuarkusTestAfterTestExecutionCallback";
    private static final String BEFORE_CALLBACK = "io.quarkus.test.junit.callback.QuarkusTestBeforeTestExecutionCallback";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalIndexedClassesBuildItem includeMongodbListener() {
        return new AdditionalIndexedClassesBuildItem(MongoDbUnitCommandListener.class.getName());
    }

    @BuildStep
    void produceServiceFiles(BuildProducer<GeneratedResourceBuildItem> resourceProducer) throws IOException {
        String implName = MongoUnitQuarkusCallback.class.getName();
        writeServices(resourceProducer, implName, AFTER_CALLBACK);
        writeServices(resourceProducer, implName, BEFORE_CALLBACK);
    }

    private static void writeServices(BuildProducer<GeneratedResourceBuildItem> resourceProducer,
                                      String implName,
                                      String serviceName) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             OutputStreamWriter w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                w.write(implName);
                w.write(System.lineSeparator());
                w.flush();
                resourceProducer.produce(
                        new GeneratedResourceBuildItem("META-INF/services/" + serviceName, os.toByteArray()));
            }
    }
}
