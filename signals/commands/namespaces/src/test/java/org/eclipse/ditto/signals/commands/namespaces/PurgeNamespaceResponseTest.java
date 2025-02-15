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
package org.eclipse.ditto.signals.commands.namespaces;

import static org.eclipse.ditto.signals.commands.base.assertions.CommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.UUID;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit test for {@link org.eclipse.ditto.signals.commands.namespaces.PurgeNamespaceResponse}.
 */
public final class PurgeNamespaceResponseTest {

    private static final String NAMESPACE = "com.example.test";
    private static final String RESOURCE_TYPE = "policy";

    private static JsonObject knownJsonRepresentation;
    private static DittoHeaders dittoHeaders;

    @BeforeClass
    public static void initTestConstants() {
        knownJsonRepresentation = JsonFactory.newObjectBuilder()
                .set(PurgeNamespaceResponse.JsonFields.TYPE, PurgeNamespaceResponse.TYPE)
                .set(PurgeNamespaceResponse.JsonFields.STATUS, HttpStatus.OK.getCode())
                .set(PurgeNamespaceResponse.JsonFields.NAMESPACE, NAMESPACE)
                .set(PurgeNamespaceResponse.JsonFields.RESOURCE_TYPE, RESOURCE_TYPE)
                .set(PurgeNamespaceResponse.JsonFields.SUCCESSFUL, true)
                .build();

        dittoHeaders = DittoHeaders.newBuilder()
                .correlationId(String.valueOf(UUID.randomUUID()))
                .build();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(PurgeNamespaceResponse.class, areImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PurgeNamespaceResponse.class)
                .withRedefinedSuperclass()
                .usingGetClass()
                .verify();
    }

    @Test
    public void fromJsonReturnsExpected() {
        final PurgeNamespaceResponse responseFromJson =
                PurgeNamespaceResponse.fromJson(knownJsonRepresentation, dittoHeaders);

        assertThat(responseFromJson)
                .isEqualTo(PurgeNamespaceResponse.successful(NAMESPACE, RESOURCE_TYPE, dittoHeaders));
    }

    @Test
    public void successfulResponseToJson() {
        final PurgeNamespaceResponse underTest =
                PurgeNamespaceResponse.successful(NAMESPACE, RESOURCE_TYPE, dittoHeaders);

        assertThat(underTest.toJson()).isEqualTo(knownJsonRepresentation);
    }

    @Test
    public void failedResponseToJson() {
        final JsonObject expectedJson = knownJsonRepresentation.toBuilder()
                .set(PurgeNamespaceResponse.JsonFields.SUCCESSFUL, false)
                .build();

        final PurgeNamespaceResponse underTest = PurgeNamespaceResponse.failed(NAMESPACE, RESOURCE_TYPE, dittoHeaders);

        assertThat(underTest.toJson()).isEqualTo(expectedJson);
    }

    @Test
    public void toStringContainsExpected() {
        final PurgeNamespaceResponse underTest = PurgeNamespaceResponse.failed(NAMESPACE, RESOURCE_TYPE, dittoHeaders);

        assertThat(underTest.toString())
                .contains(underTest.getClass().getSimpleName())
                .contains(NAMESPACE)
                .contains(RESOURCE_TYPE)
                .contains(String.valueOf(underTest.isSuccessful()));
    }

}
