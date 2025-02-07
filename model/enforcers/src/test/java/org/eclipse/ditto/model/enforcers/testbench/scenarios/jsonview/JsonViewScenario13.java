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
package org.eclipse.ditto.model.enforcers.testbench.scenarios.jsonview;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.enforcers.testbench.scenarios.Scenario;
import org.eclipse.ditto.model.enforcers.testbench.scenarios.ScenarioSetup;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.PoliciesResourceType;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.model.policies.SubjectIssuer;
import org.eclipse.ditto.model.policies.SubjectType;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Features;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;

/**
 * Tests Policy with granted permissions only.
 */
public final class JsonViewScenario13 implements JsonViewScenario {

    private static final class Permission {

        public static final String READ = "READ";
        public static final String WRITE = "WRITE";

        private Permission() {
            throw new AssertionError();
        }

    }

    private static final PolicyId POLICY_ID =
            PolicyId.of("org.eclipse.ditto", JsonViewScenario13.class.getSimpleName());
    private static final String LABEL_OWNER = "owner";
    private static final String LABEL_CLIENT = "client";
    private static final SubjectId SUBJECT_ID_OWNER = SubjectId.newInstance(SubjectIssuer.GOOGLE, LABEL_OWNER);
    private static final SubjectId SUBJECT_ID_CLIENT = SubjectId.newInstance(SubjectIssuer.GOOGLE, LABEL_CLIENT);
    private static final ThingId TEST_THING_ID = ThingId.of("org.eclipse.ditto", "thing1");
    private static final Feature GYROSCOPE_FEATURE = ThingsModelFactory.newFeatureBuilder()
            .properties(ThingsModelFactory.newFeaturePropertiesBuilder()
                    .set("status", JsonFactory.newObjectBuilder()
                            .set("minRangeValue", -2000)
                            .set("xValue", -0.05071427300572395)
                            .set("units", "Deg/s")
                            .set("yValue", -0.4192921817302704)
                            .set("zValue", 0.20766231417655945)
                            .set("maxRangeValue", 2000)
                            .build())
                    .build())
            .withId("Gyroscope.0")
            .build();
    private static final Features TEST_FEATURES = ThingsModelFactory.newFeatures(GYROSCOPE_FEATURE);
    private static final Thing TEST_THING = ThingsModelFactory.newThingBuilder()
            .setId(TEST_THING_ID)
            .setPolicyId(POLICY_ID)
            .setAttribute(JsonFactory.newPointer("isOnline"), JsonFactory.newValue(false))
            .setAttribute(JsonFactory.newPointer("lastUpdate"), JsonFactory.newValue("Thu Sep 28 15:01:43 CEST 2017"))
            .setFeatures(TEST_FEATURES)
            .build();

    private final ScenarioSetup setup;

    private JsonViewScenario13(final ScenarioSetup theSetup) {
        setup = checkNotNull(theSetup, "scenario setup");
    }

    /**
     * Returns an instance of {@code JsonViewScenario13}.
     *
     * @return the instance.
     */
    public static JsonViewScenario13 getInstance() {
        return new JsonViewScenario13(createScenarioSetup(createPolicy()));
    }

    private static ScenarioSetup createScenarioSetup(final Policy policy) {
        checkNotNull(policy, "policy of the scenario setup");

        final AuthorizationContext authorizationContext = Scenario.newAuthorizationContext(LABEL_CLIENT);
        final String resource = "/features/" + GYROSCOPE_FEATURE.getId();
        final JsonObject expectedJsonView = GYROSCOPE_FEATURE.toJson();
        final Set<AuthorizationSubject> expectedSubjectIds = new HashSet<>();
        Collections.addAll(expectedSubjectIds, AuthorizationSubject.newInstance(SUBJECT_ID_OWNER),
                AuthorizationSubject.newInstance(SUBJECT_ID_CLIENT));

        return Scenario.newScenarioSetup(true, "description", policy, authorizationContext, resource, TEST_THING,
                expectedJsonView, expectedSubjectIds, Permission.READ, Permission.WRITE);
    }

    private static Policy createPolicy() {
        return PoliciesModelFactory.newPolicyBuilder(POLICY_ID)
                .forLabel(LABEL_OWNER)
                .setSubject(SUBJECT_ID_OWNER, SubjectType.GENERATED)
                .setGrantedPermissions(PoliciesResourceType.policyResource("/"), Permission.READ, Permission.WRITE)
                .setGrantedPermissions(PoliciesResourceType.thingResource("/"), Permission.READ, Permission.WRITE)
                .setGrantedPermissions(PoliciesResourceType.messageResource("/"), Permission.READ, Permission.WRITE)
                .forLabel(LABEL_CLIENT)
                .setSubject(SUBJECT_ID_CLIENT, SubjectType.GENERATED)
                .setGrantedPermissions(PoliciesResourceType.thingResource("/features/" + GYROSCOPE_FEATURE.getId()),
                        Permission.READ, Permission.WRITE)
                .build();
    }

    @Override
    public ScenarioSetup getSetup() {
        return setup;
    }

    @Override
    public Policy getPolicy() {
        return setup.getPolicy();
    }

}
