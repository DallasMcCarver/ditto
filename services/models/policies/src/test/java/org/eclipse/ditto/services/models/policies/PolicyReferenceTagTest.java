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
package org.eclipse.ditto.services.models.policies;

import static org.eclipse.ditto.json.assertions.DittoJsonAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link PolicyReferenceTag}.
 */
public final class PolicyReferenceTagTest {

    private static final ThingId THING_ID = ThingId.of("ns:entityId");
    private static final PolicyTag POLICY_TAG =
            PolicyTag.of(TestConstants.Policy.POLICY_ID, TestConstants.Policy.REVISION_NUMBER);
    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(PolicyReferenceTag.JsonFields.ENTITY_ID, THING_ID.toString())
            .set(PolicyReferenceTag.JsonFields.POLICY_ID, POLICY_TAG.getEntityId().toString())
            .set(PolicyReferenceTag.JsonFields.POLICY_REV, POLICY_TAG.getRevision())
            .build();

    @Test
    public void assertImmutability() {
        assertInstancesOf(PolicyReferenceTag.class,
                areImmutable(),
                provided(EntityId.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PolicyReferenceTag.class).verify();
    }

    @Test
    public void toJsonReturnsExpected() {
        final PolicyReferenceTag underTest = PolicyReferenceTag.of(THING_ID, POLICY_TAG);
        final JsonValue jsonValue = underTest.toJson();

        assertThat(jsonValue).isEqualTo(KNOWN_JSON);
    }

    @Test
    public void createInstanceFromValidJson() {
        final PolicyReferenceTag underTest = PolicyReferenceTag.fromJson(KNOWN_JSON);

        assertThat(underTest).isNotNull();
        assertThat((CharSequence) underTest.getThingId()).isEqualTo(THING_ID);
        assertThat(underTest.getPolicyTag()).isEqualTo(POLICY_TAG);
    }

}
