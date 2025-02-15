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
package org.eclipse.ditto.model.thingsearch;


import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.model.things.ThingRevision;
import org.eclipse.ditto.model.things.ThingsModelFactory;

/**
 * Defines constants for testing.
 */
public final class TestConstants {

    private TestConstants() {
        throw new AssertionError();
    }

    /**
     * Feature-related test constants.
     */
    public static final class SearchFeature {

        /**
         * A known ID of a Feature.
         */
        public static final String FLUX_CAPACITOR_ID = "FluxCapacitor";

        /**
         * Properties of a known Feature.
         */
        public static final FeatureProperties FLUX_CAPACITOR_PROPERTIES = FeatureProperties.newBuilder()
                .set("target_year_1", 1955)
                .set("target_year_2", 2015)
                .set("target_year_3", 1885)
                .build();

        /**
         * A known Feature which is required for time travel.
         */
        public static final org.eclipse.ditto.model.things.Feature FLUX_CAPACITOR =
                org.eclipse.ditto.model.things.Feature.newBuilder()
                        .properties(FLUX_CAPACITOR_PROPERTIES)
                        .withId(FLUX_CAPACITOR_ID)
                        .build();

        /**
         * Known features of a Thing.
         */
        public static final Features FEATURES = ThingsModelFactory.newFeatures(SearchFeature.FLUX_CAPACITOR);

        private SearchFeature() {
            throw new AssertionError();
        }
    }

    /**
     * Thing-related test constants.
     */
    public static final class SearchThing {

        /**
         * A known Thing ID for testing.
         */
        public static final ThingId THING_ID = ThingId.of("example.com", "testThing");

        /**
         * A known lifecycle of a Thing.
         */
        public static final ThingLifecycle LIFECYCLE = ThingLifecycle.ACTIVE;

        /**
         * A known Policy ID.
         */
        public static final PolicyId POLICY_ID = PolicyId.of(THING_ID);

        /**
         * A known path to the attribute with the key "manufacturer" within a Thing.
         */
        public static final JsonPointer MANUFACTURER_PATH = JsonFactory.newPointer("attributes/manufacturer");

        /**
         * A known location attribute for testing.
         */
        public static final JsonObject LOCATION_ATTRIBUTE = JsonFactory.newObjectBuilder() //
                .set("latitude", 44.673856) //
                .set("longitude", 8.261719) //
                .build();

        /**
         * Known attributes of a Thing.
         */
        public static final Attributes ATTRIBUTES = ThingsModelFactory.newAttributesBuilder() //
                .set("location", LOCATION_ATTRIBUTE) //
                .set(MANUFACTURER_PATH.getLeaf().get(), "Bosch") //
                .build();

        /**
         * A known revision number of a Thing.
         */
        public static final long REVISION_NUMBER = 0;

        /**
         * A known revision of a Thing.
         */
        public static final ThingRevision REVISION = ThingsModelFactory.newThingRevision(REVISION_NUMBER);

        /**
         * A known Thing for testing.
         */
        public static final org.eclipse.ditto.model.things.Thing THING = ThingsModelFactory.newThingBuilder() //
                .setAttributes(ATTRIBUTES) //
                .setFeatures(SearchFeature.FEATURES) //
                .setLifecycle(LIFECYCLE) //
                .setPolicyId(POLICY_ID) //
                .setId(THING_ID) //
                .build();

        private SearchThing() {
            throw new AssertionError();
        }
    }

}
