/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.signals.events.things.examplejson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationModelFactory;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.base.json.Jsonifiable;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureDefinition;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.model.things.ThingRevision;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.events.things.AttributeCreated;
import org.eclipse.ditto.signals.events.things.AttributeDeleted;
import org.eclipse.ditto.signals.events.things.AttributeModified;
import org.eclipse.ditto.signals.events.things.AttributesCreated;
import org.eclipse.ditto.signals.events.things.AttributesDeleted;
import org.eclipse.ditto.signals.events.things.AttributesModified;
import org.eclipse.ditto.signals.events.things.FeatureCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionCreated;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDefinitionModified;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.things.FeatureModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertiesModified;
import org.eclipse.ditto.signals.events.things.FeaturePropertyCreated;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.eclipse.ditto.signals.events.things.FeaturePropertyModified;
import org.eclipse.ditto.signals.events.things.FeaturesCreated;
import org.eclipse.ditto.signals.events.things.FeaturesDeleted;
import org.eclipse.ditto.signals.events.things.FeaturesModified;
import org.eclipse.ditto.signals.events.things.PolicyIdModified;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.eclipse.ditto.signals.events.things.ThingModified;


public class JsonExamplesProducer {

    private static final String FEATURE_ID = "accelerometer";
    private static final String NAMESPACE = "com.acme";

    /*
     * Thing
     */
    private static final ThingId THING_ID = ThingId.of(NAMESPACE, "xdk_53");
    private static final PolicyId POLICY_ID = PolicyId.of(NAMESPACE + ":policy0815");
    private static final ThingLifecycle LIFECYCLE = ThingLifecycle.ACTIVE;
    private static final AuthorizationSubject AUTH_SUBJECT_1 =
            AuthorizationModelFactory.newAuthSubject("the_auth_subject");
    private static final JsonObject ATTRIBUTE_VALUE = JsonFactory.newObjectBuilder()
            .set("latitude", 44.673856)
            .set("longitude", 8.261719)
            .build();
    private static final JsonPointer ATTRIBUTE_POINTER = JsonFactory.newPointer("location");
    private static final Attributes ATTRIBUTES = ThingsModelFactory.newAttributesBuilder()
            .set(ATTRIBUTE_POINTER, ATTRIBUTE_VALUE)
            .build();
    private static final long REVISION_NUMBER = 1L;
    private static final ThingRevision THING_REVISION = ThingsModelFactory.newThingRevision(REVISION_NUMBER);
    private static final String PROPERTY_X = "x";
    private static final JsonPointer PROPERTY_POINTER = JsonFactory.newPointer(PROPERTY_X);
    private static final JsonValue PROPERTY_VALUE = JsonFactory.newValue(42);
    private static final FeatureDefinition FEATURE_DEFINITION =
            FeatureDefinition.fromIdentifier("org.eclipse.ditto:fluxcapacitor:1.0.0");
    private static final FeatureProperties FEATURE_PROPERTIES = ThingsModelFactory.newFeaturePropertiesBuilder()
            .set("x", 3.141)
            .set("y", 2.718)
            .set("z", 1)
            .set("unit", "g")
            .build();
    private static final Feature FLUX_CAPACITOR = ThingsModelFactory.newFeatureBuilder()
            .properties(FEATURE_PROPERTIES)
            .withId(FEATURE_ID)
            .build();
    private static final Features FEATURES = ThingsModelFactory.newFeatures(FLUX_CAPACITOR);
    private static final Thing THING = ThingsModelFactory.newThingBuilder()
            .setId(THING_ID)
            .setRevision(THING_REVISION)
            .setAttributes(ATTRIBUTES)
            .setFeatures(FEATURES)
            .setLifecycle(LIFECYCLE)
            .setPolicyId(POLICY_ID)
            .build();

    private static final Instant TIMESTAMP = Instant.EPOCH;
    private static final DittoHeaders DITTO_HEADERS = DittoHeaders.empty();
    private static final Metadata METADATA = Metadata.newBuilder()
            .set("creator", "The epic Ditto team")
            .build();


    public static void main(final String... args) throws IOException {
        run(args, new JsonExamplesProducer());
    }

    private static void run(final String[] args, final JsonExamplesProducer producer) throws
            IOException {
        if (args.length != 1) {
            System.err.println("Exactly 1 argument required: the target folder in which to generate the JSON files");
            System.exit(-1);
        }
        producer.produce(Paths.get(args[0]));
    }

    private void produce(final Path rootPath) throws IOException {
        produceThingEvents(rootPath.resolve("things"));
    }

    private void produceThingEvents(final Path rootPath) throws IOException {
        final Path eventsDir = rootPath.resolve(Paths.get("events"));
        Files.createDirectories(eventsDir);

        final ThingCreated thingCreated = ThingCreated.of(THING, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("thingCreated.json")), thingCreated);

        final ThingModified thingModified = ThingModified.of(THING, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("thingModified.json")), thingModified);

        final ThingDeleted thingDeleted = ThingDeleted.of(THING_ID, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("thingDeleted.json")), thingDeleted);

        final PolicyIdModified policyIdModified =
                PolicyIdModified.of(THING_ID, PolicyId.of(THING_ID), REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("policyIdModified.json")), policyIdModified);

        final AttributesCreated attributesCreated = AttributesCreated.of(THING_ID, ATTRIBUTES, REVISION_NUMBER,
                TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("attributesCreated.json")), attributesCreated);

        final AttributesModified attributesModified = AttributesModified.of(THING_ID, ATTRIBUTES, REVISION_NUMBER,
                TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("attributesModified.json")), attributesModified);

        final AttributesDeleted attributesDeleted = AttributesDeleted.of(THING_ID, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("attributesDeleted.json")), attributesDeleted);

        final AttributeCreated attributeCreated = AttributeCreated.of(THING_ID, ATTRIBUTE_POINTER, ATTRIBUTE_VALUE,
                REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("attributeCreated.json")), attributeCreated);

        final AttributeModified attributeModified = AttributeModified.of(THING_ID, ATTRIBUTE_POINTER, ATTRIBUTE_VALUE,
                REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("attributeModified.json")), attributeModified);

        final AttributeDeleted attributeDeleted = AttributeDeleted.of(THING_ID, ATTRIBUTE_POINTER, REVISION_NUMBER,
                TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("attributeDeleted.json")), attributeDeleted);

        final FeatureCreated featureCreated = FeatureCreated.of(THING_ID, FLUX_CAPACITOR, REVISION_NUMBER,
                TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featureCreated.json")), featureCreated);

        final FeatureModified featureModified = FeatureModified.of(THING_ID, FLUX_CAPACITOR, REVISION_NUMBER,
                TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featureModified.json")), featureModified);

        final FeatureDeleted featureDeleted = FeatureDeleted.of(THING_ID, FEATURE_ID, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featureDeleted.json")), featureDeleted);

        final FeaturesDeleted featuresDeleted = FeaturesDeleted.of(THING_ID, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featuresDeleted.json")), featuresDeleted);

        final FeaturesCreated featuresCreated = FeaturesCreated.of(THING_ID,
                ThingsModelFactory.newFeatures(FLUX_CAPACITOR), REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featuresCreated.json")), featuresCreated);

        final FeaturesModified featuresModified = FeaturesModified.of(THING_ID, FEATURES, REVISION_NUMBER,
                TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featuresModified.json")), featuresModified);

        final FeatureDefinitionCreated featureDefinitionCreated = FeatureDefinitionCreated.of(THING_ID, FEATURE_ID,
                FEATURE_DEFINITION, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featureDefinitionCreated.json")), featureDefinitionCreated);

        final FeatureDefinitionModified featureDefinitionModified = FeatureDefinitionModified.of(THING_ID, FEATURE_ID,
                FEATURE_DEFINITION, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featureDefinitionModified.json")), featureDefinitionModified);

        final FeaturePropertiesCreated featurePropertiesCreated = FeaturePropertiesCreated.of(THING_ID, FEATURE_ID,
                FEATURE_PROPERTIES, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featurePropertiesCreated.json")), featurePropertiesCreated);

        final FeaturePropertiesModified featurePropertiesModified = FeaturePropertiesModified.of(THING_ID, FEATURE_ID,
                FEATURE_PROPERTIES, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featurePropertiesModified.json")), featurePropertiesModified);

        final FeaturePropertyCreated featurePropertyCreated = FeaturePropertyCreated.of(THING_ID, FEATURE_ID,
                PROPERTY_POINTER, PROPERTY_VALUE, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featurePropertyCreated.json")), featurePropertyCreated);

        final FeaturePropertyModified featurePropertyModified = FeaturePropertyModified.of(THING_ID, FEATURE_ID,
                PROPERTY_POINTER, PROPERTY_VALUE, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featurePropertyModified.json")), featurePropertyModified);

        final FeatureDefinitionDeleted featureDefinitionDeleted = FeatureDefinitionDeleted.of(THING_ID, FEATURE_ID,
                REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featureDefinitionDeleted.json")), featureDefinitionDeleted);

        final FeaturePropertiesDeleted featurePropertiesDeleted = FeaturePropertiesDeleted.of(THING_ID, FEATURE_ID,
                REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featurePropertiesDeleted.json")), featurePropertiesDeleted);

        final FeaturePropertyDeleted featurePropertyDeleted = FeaturePropertyDeleted.of(THING_ID, FEATURE_ID,
                PROPERTY_POINTER, REVISION_NUMBER, TIMESTAMP, DITTO_HEADERS, METADATA);
        writeJson(eventsDir.resolve(Paths.get("featurePropertyDeleted.json")), featurePropertyDeleted);
    }

    private void writeJson(final Path path, final Jsonifiable.WithPredicate<JsonObject, JsonField> jsonifiable)
            throws IOException {
        writeJson(path, jsonifiable, JsonSchemaVersion.LATEST);
    }

    private void writeJson(final Path path, final Jsonifiable.WithPredicate<JsonObject, JsonField> jsonifiable,
            final JsonSchemaVersion schemaVersion) throws IOException {
        final String jsonString = jsonifiable.toJsonString(schemaVersion);
        System.out.println("Writing file: " + path.toAbsolutePath());
        Files.write(path, jsonString.getBytes());
    }
}
