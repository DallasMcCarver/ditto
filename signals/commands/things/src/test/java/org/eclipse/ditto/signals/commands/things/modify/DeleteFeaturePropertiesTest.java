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
package org.eclipse.ditto.signals.commands.things.modify;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.eclipse.ditto.signals.commands.things.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.signals.commands.things.TestConstants;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link DeleteFeatureProperties}.
 */
public final class DeleteFeaturePropertiesTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(ThingCommand.JsonFields.TYPE, DeleteFeatureProperties.TYPE)
            .set(ThingCommand.JsonFields.JSON_THING_ID, TestConstants.Thing.THING_ID.toString())
            .set(DeleteFeatureProperties.JSON_FEATURE_ID, TestConstants.Feature.FLUX_CAPACITOR_ID)
            .build();

    @Test
    public void assertImmutability() {
        assertInstancesOf(DeleteFeatureProperties.class, areImmutable(), provided(ThingId.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(DeleteFeatureProperties.class)
                .withRedefinedSuperclass()
                .verify();
    }

    @Test
    public void tryToCreateInstanceWithNullThingId() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> DeleteFeatureProperties.of(null, TestConstants.Feature.FLUX_CAPACITOR_ID,
                        TestConstants.EMPTY_DITTO_HEADERS))
                .withNoCause();
    }

    @Test
    public void tryToCreateInstanceWithNullFeatureId() {
        assertThatNullPointerException()
                .isThrownBy(() -> DeleteFeatureProperties.of(TestConstants.Thing.THING_ID, null,
                        TestConstants.EMPTY_DITTO_HEADERS))
                .withMessage("The %s must not be null!", "Feature ID")
                .withNoCause();
    }

    @Test
    public void createInstanceWithValidArguments() {
        final DeleteFeatureProperties underTest = DeleteFeatureProperties.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.FLUX_CAPACITOR_ID, TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTest).isNotNull();
    }

    @Test
    public void toJsonReturnsExpected() {
        final DeleteFeatureProperties underTest = DeleteFeatureProperties.of(TestConstants.Thing.THING_ID,
                TestConstants.Feature.FLUX_CAPACITOR_ID, TestConstants.EMPTY_DITTO_HEADERS);
        final JsonObject actualJson = underTest.toJson(FieldType.regularOrSpecial());

        assertThat(actualJson).isEqualTo(KNOWN_JSON);
    }

    @Test
    public void createInstanceFromValidJson() {
        final DeleteFeatureProperties underTest =
                DeleteFeatureProperties.fromJson(KNOWN_JSON.toString(), TestConstants.EMPTY_DITTO_HEADERS);

        assertThat(underTest).isNotNull();
        assertThat((CharSequence) underTest.getEntityId()).isEqualTo(TestConstants.Thing.THING_ID);
        assertThat(underTest.getFeatureId()).isEqualTo(TestConstants.Feature.FLUX_CAPACITOR_ID);
    }

}
