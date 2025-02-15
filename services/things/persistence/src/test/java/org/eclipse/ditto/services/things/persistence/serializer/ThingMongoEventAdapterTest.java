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
package org.eclipse.ditto.services.things.persistence.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Instant;
import java.util.List;

import org.bson.BsonDocument;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.events.things.ThingCreated;
import org.eclipse.ditto.signals.events.things.ThingDeleted;
import org.junit.Test;

import akka.persistence.journal.EventSeq;
import scala.jdk.javaapi.CollectionConverters;

/**
 * Tests for {@link ThingMongoEventAdapter}.
 */
public final class ThingMongoEventAdapterTest {

    private final ThingMongoEventAdapter underTest;

    public ThingMongoEventAdapterTest() {
        underTest = new ThingMongoEventAdapter(null);
    }

    @Test
    public void extractsTypeOfEventAsManifest() {
        final ThingDeleted event = ThingDeleted.of(ThingId.generateRandom(), 1L, null, DittoHeaders.empty(), null);
        assertThat(underTest.manifest(event)).isEqualTo(event.getType());
    }

    @Test
    public void manifestThrowsIllegalArgumentExceptionForNonEvents() {
        final DeleteThing nonEvent = DeleteThing.of(ThingId.generateRandom(), DittoHeaders.empty());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> underTest.manifest(nonEvent));
    }

    @Test
    public void toJournalThrowsIllegalArgumentExceptionForNonEvents() {
        final DeleteThing nonEvent = DeleteThing.of(ThingId.generateRandom(), DittoHeaders.empty());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> underTest.toJournal(nonEvent));
    }

    @Test
    public void toJournalReturnsBsonDocument() {
        final Thing thing = Thing.newBuilder()
                .setId(ThingId.of("pap.th.tMJyAjktUVP:YlmZXbTQ"))
                .setModified(Instant.parse("2021-02-24T14:17:37.581679843Z"))
                .setCreated(Instant.parse("2021-02-24T14:17:37.581679843Z"))
                .setLifecycle(ThingLifecycle.ACTIVE)
                .setRevision(1)
                .setPolicyId(PolicyId.of("pap.th.tMJyAjktUVP:YlmZXbTQ"))
                .setAttributes(Attributes.newBuilder().set("hello", "cloud").build())
                .build();
        final ThingCreated thingCreated =
                ThingCreated.of(thing, 0, Instant.parse("2021-02-24T14:17:37.581679843Z"), DittoHeaders.empty(), null);

        final String journalEntry = "{\n" +
                "                \"type\" : \"things.events:thingCreated\",\n" +
                "                \"_timestamp\" : \"2021-02-24T14:17:37.581679843Z\",\n" +
                "                \"_metadata\" : null,\n" +
                "                \"thingId\" : \"pap.th.tMJyAjktUVP:YlmZXbTQ\",\n" +
                "                \"thing\" : {\n" +
                "                    \"__schemaVersion\" : 2,\n" +
                "                    \"__lifecycle\" : \"ACTIVE\",\n" +
                "                    \"_revision\" : 1,\n" +
                "                    \"_modified\" : \"2021-02-24T14:17:37.581679843Z\",\n" +
                "                    \"_created\" : \"2021-02-24T14:17:37.581679843Z\",\n" +
                "                    \"_namespace\" : \"pap.th.tMJyAjktUVP\",\n" +
                "                    \"thingId\" : \"pap.th.tMJyAjktUVP:YlmZXbTQ\",\n" +
                "                    \"policyId\" : \"pap.th.tMJyAjktUVP:YlmZXbTQ\",\n" +
                "                    \"attributes\" : {\n" +
                "                        \"hello\" : \"cloud\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }";
        final BsonDocument bsonEvent = BsonDocument.parse(journalEntry);

        assertThat(underTest.toJournal(thingCreated)).isEqualTo(bsonEvent);
    }

    @Test
    public void fromJournalThrowsIllegalArgumentExceptionForNonBsonValues() {
        final DeleteThing nonEvent = DeleteThing.of(ThingId.generateRandom(), DittoHeaders.empty());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> underTest.fromJournal(nonEvent, DeleteThing.TYPE));
    }

    @Test
    public void fromJournalReturnsEvent() {
        final String journalEntry = "{\n" +
                "                \"type\" : \"things.events:thingCreated\",\n" +
                "                \"_timestamp\" : \"2021-02-24T14:17:37.581679843Z\",\n" +
                "                \"_metadata\" : null,\n" +
                "                \"thingId\" : \"pap.th.tMJyAjktUVP:YlmZXbTQ\",\n" +
                "                \"thing\" : {\n" +
                "                    \"__schemaVersion\" : 2,\n" +
                "                    \"__lifecycle\" : \"ACTIVE\",\n" +
                "                    \"_revision\" : 1,\n" +
                "                    \"_modified\" : \"2021-02-24T14:17:37.581679843Z\",\n" +
                "                    \"_created\" : \"2021-02-24T14:17:37.581679843Z\",\n" +
                "                    \"_namespace\" : \"pap.th.tMJyAjktUVP\",\n" +
                "                    \"thingId\" : \"pap.th.tMJyAjktUVP:YlmZXbTQ\",\n" +
                "                    \"policyId\" : \"pap.th.tMJyAjktUVP:YlmZXbTQ\",\n" +
                "                    \"attributes\" : {\n" +
                "                        \"hello\" : \"cloud\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }";
        final BsonDocument bsonEvent = BsonDocument.parse(journalEntry);

        final Thing thing = Thing.newBuilder()
                .setId(ThingId.of("pap.th.tMJyAjktUVP:YlmZXbTQ"))
                .setModified(Instant.parse("2021-02-24T14:17:37.581679843Z"))
                .setCreated(Instant.parse("2021-02-24T14:17:37.581679843Z"))
                .setLifecycle(ThingLifecycle.ACTIVE)
                .setRevision(1)
                .setPolicyId(PolicyId.of("pap.th.tMJyAjktUVP:YlmZXbTQ"))
                .setAttributes(Attributes.newBuilder().set("hello", "cloud").build())
                .build();
        final ThingCreated thingCreated =
                ThingCreated.of(thing, 0, Instant.parse("2021-02-24T14:17:37.581679843Z"), DittoHeaders.empty(), null);
        final EventSeq eventSeq = underTest.fromJournal(bsonEvent, "things.events:thingCreated");
        final List<Object> objects = CollectionConverters.asJava(eventSeq.events());
        assertThat(objects).containsExactly(thingCreated);
    }

}
