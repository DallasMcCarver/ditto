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
package org.eclipse.ditto.services.things.persistence.actors.strategies.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_ID;
import static org.eclipse.ditto.model.things.TestConstants.Thing.THING_V2;
import static org.eclipse.ditto.services.things.persistence.actors.ETagTestUtils.sudoRetrieveThingResponse;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.models.things.commands.sudo.SudoRetrieveThing;
import org.eclipse.ditto.services.models.things.commands.sudo.SudoRetrieveThingResponse;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.things.exceptions.ThingNotAccessibleException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link SudoRetrieveThingStrategy}.
 */
public final class SudoRetrieveThingStrategyTest extends AbstractCommandStrategyTest {

    private SudoRetrieveThingStrategy underTest;

    @Before
    public void setUp() {
        underTest = new SudoRetrieveThingStrategy();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(SudoRetrieveThingStrategy.class, areImmutable());
    }

    @Test
    public void isNotDefinedForDeviantThingIds() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final SudoRetrieveThing command =
                SudoRetrieveThing.of(ThingId.of("org.example", "myThing"), DittoHeaders.empty());

        final boolean defined = underTest.isDefined(context, THING_V2, command);

        assertThat(defined).isFalse();
    }

    @Test
    public void isNotDefinedIfContextHasNoThing() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final SudoRetrieveThing command = SudoRetrieveThing.of(THING_ID, DittoHeaders.empty());

        final boolean defined = underTest.isDefined(context, null, command);

        assertThat(defined).isFalse();
    }

    @Test
    public void isDefinedIfContextHasThingAndThingIdsAreEqual() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final SudoRetrieveThing command = SudoRetrieveThing.of(context.getState(), DittoHeaders.empty());

        final boolean defined = underTest.isDefined(context, THING_V2, command);

        assertThat(defined).isTrue();
    }

    @Test
    public void retrieveThingWithoutSelectedFields() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final DittoHeaders dittoHeaders = DittoHeaders.newBuilder()
                .schemaVersion(JsonSchemaVersion.V_2)
                .build();
        final SudoRetrieveThing command = SudoRetrieveThing.of(context.getState(), dittoHeaders);
        final JsonObject expectedThingJson = THING_V2.toJson(command.getImplementedSchemaVersion(),
                FieldType.regularOrSpecial());
        final SudoRetrieveThingResponse expectedResponse =
                sudoRetrieveThingResponse(THING_V2, expectedThingJson, dittoHeaders);

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void retrieveThingWithoutSelectedFieldsWithOriginalSchemaVersion() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final SudoRetrieveThing command =
                SudoRetrieveThing.withOriginalSchemaVersion(context.getState(), DittoHeaders.empty());
        final JsonObject expectedThingJson = THING_V2.toJson(THING_V2.getImplementedSchemaVersion(),
                FieldType.regularOrSpecial());
        final SudoRetrieveThingResponse expectedResponse =
                sudoRetrieveThingResponse(THING_V2, expectedThingJson, DittoHeaders.empty());

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void retrieveThingWithSelectedFields() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final JsonFieldSelector fieldSelector = JsonFactory.newFieldSelector("/attribute/location");
        final DittoHeaders dittoHeaders = DittoHeaders.newBuilder()
                .schemaVersion(JsonSchemaVersion.V_2)
                .build();
        final SudoRetrieveThing command =
                SudoRetrieveThing.of(context.getState(), fieldSelector, dittoHeaders);
        final JsonObject expectedThingJson = THING_V2.toJson(command.getImplementedSchemaVersion(), fieldSelector,
                FieldType.regularOrSpecial());
        final SudoRetrieveThingResponse expectedResponse =
                sudoRetrieveThingResponse(THING_V2, expectedThingJson, dittoHeaders);

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void retrieveThingWithSelectedFieldsWithOriginalSchemaVersion() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final JsonFieldSelector fieldSelector = JsonFactory.newFieldSelector("/attribute/location");
        final SudoRetrieveThing command =
                SudoRetrieveThing.of(context.getState(), fieldSelector, DittoHeaders.empty());
        final JsonObject expectedThingJson = THING_V2.toJson(THING_V2.getImplementedSchemaVersion(), fieldSelector,
                FieldType.regularOrSpecial());
        final SudoRetrieveThingResponse expectedResponse =
                sudoRetrieveThingResponse(THING_V2, expectedThingJson, DittoHeaders.empty());

        assertQueryResult(underTest, THING_V2, command, expectedResponse);
    }

    @Test
    public void unhandledReturnsThingNotAccessibleException() {
        final CommandStrategy.Context<ThingId> context = getDefaultContext();
        final SudoRetrieveThing command = SudoRetrieveThing.of(context.getState(), DittoHeaders.empty());
        final ThingNotAccessibleException expectedException =
                new ThingNotAccessibleException(context.getState(), command.getDittoHeaders());

        assertUnhandledResult(underTest, THING_V2, command, expectedException);
    }
}
