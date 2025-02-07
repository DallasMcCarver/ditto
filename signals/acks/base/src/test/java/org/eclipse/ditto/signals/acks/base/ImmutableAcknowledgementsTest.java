/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.signals.acks.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.assertj.core.util.Lists;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.acks.AcknowledgementLabel;
import org.eclipse.ditto.model.base.acks.DittoAcknowledgementLabel;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Unit tests for {@link ImmutableAcknowledgements}.
 */
public final class ImmutableAcknowledgementsTest {

    private static final ThingId KNOWN_ENTITY_ID = ThingId.generateRandom();
    private static final HttpStatus KNOWN_HTTP_STATUS = HttpStatus.OK;
    private static final JsonValue KNOWN_PAYLOAD = JsonObject.newBuilder().set("known", "payload").build();
    private static final DittoHeaders KNOWN_DITTO_HEADERS = DittoHeaders.newBuilder()
            .randomCorrelationId()
            .responseRequired(false)
            .build();
    private static final Acknowledgement KNOWN_ACK_1 =
            ImmutableAcknowledgement.of(AcknowledgementLabel.of("welcome-ack"), KNOWN_ENTITY_ID, KNOWN_HTTP_STATUS,
                    KNOWN_DITTO_HEADERS, KNOWN_PAYLOAD);
    private static final Acknowledgement KNOWN_ACK_2 =
            ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID, KNOWN_HTTP_STATUS,
                    KNOWN_DITTO_HEADERS, KNOWN_PAYLOAD);

    private static List<Acknowledgement> knownAcknowledgements;
    private static JsonObject knownAcknowledgementsJsonRepresentation;
    private static JsonObject knownAcknowledgementsGetEntityJsonRepresentation;
    private static JsonObject knownJsonRepresentation;
    private static ImmutableAcknowledgements knownAcknowledgementsWith2Acks;

    @BeforeClass
    public static void setUpClass() {
        knownAcknowledgements = Lists.list(KNOWN_ACK_1, KNOWN_ACK_2);
        knownAcknowledgementsJsonRepresentation = knownAcknowledgements.stream()
                .map(ack -> JsonField.newInstance(ack.getLabel(), ack.toJson()))
                .collect(JsonCollectors.fieldsToObject());

        knownAcknowledgementsGetEntityJsonRepresentation = knownAcknowledgements.stream()
                .map(ack -> JsonField.newInstance(ack.getLabel(),
                        JsonObject.newBuilder()
                                .set(Acknowledgement.JsonFields.STATUS_CODE, ack.getHttpStatus().getCode())
                                .set(Acknowledgement.JsonFields.PAYLOAD, ack.getEntity().get())
                                .set(Acknowledgement.JsonFields.DITTO_HEADERS, ack.getDittoHeaders().toJson())
                                .build())
                )
                .collect(JsonCollectors.fieldsToObject());

        knownJsonRepresentation = JsonObject.newBuilder()
                .set(Acknowledgements.JsonFields.ENTITY_ID, KNOWN_ENTITY_ID.toString())
                .set(Acknowledgements.JsonFields.ENTITY_TYPE, KNOWN_ENTITY_ID.getEntityType().toString())
                .set(Acknowledgements.JsonFields.STATUS_CODE, KNOWN_HTTP_STATUS.getCode())
                .set(Acknowledgements.JsonFields.ACKNOWLEDGEMENTS, knownAcknowledgementsJsonRepresentation)
                .set(Acknowledgements.JsonFields.DITTO_HEADERS, KNOWN_DITTO_HEADERS.toJson())
                .build();

        knownAcknowledgementsWith2Acks =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);
    }

    @Test
    public void testHashCodeAndEquals() {
        final ThingId red = ThingId.generateRandom();
        final ThingId black = ThingId.generateRandom();

        EqualsVerifier.forClass(ImmutableAcknowledgements.class)
                .usingGetClass()
                .withPrefabValues(EntityId.class, red, black)
                .verify();
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableAcknowledgements.class,
                areImmutable(),
                provided(Acknowledgement.class,
                        AcknowledgementLabel.class,
                        DittoHeaders.class,
                        EntityId.class,
                        HttpStatus.class).isAlsoImmutable());
    }

    @Test
    public void tryToGetInstanceWithNullAcknowledgements() {
        assertThatNullPointerException()
                .isThrownBy(() -> ImmutableAcknowledgements.of(null, KNOWN_DITTO_HEADERS))
                .withMessage("The acknowledgements must not be null!")
                .withNoCause();
    }

    @Test
    public void tryToGetInstanceWithEmptyAcknowledgements() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ImmutableAcknowledgements.of(Collections.emptySet(), KNOWN_DITTO_HEADERS))
                .withMessage("The argument 'acknowledgements' must not be empty!")
                .withNoCause();
    }

    @Test
    public void tryToGetInstanceWithAcknowledgementsWithDifferentEntityIds() {
        final ThingId otherEntityId = ThingId.of("com.example:flux-condensator");
        final Acknowledgement otherAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, otherEntityId, KNOWN_HTTP_STATUS,
                        KNOWN_DITTO_HEADERS, null);
        final List<Acknowledgement> acknowledgements = Lists.list(KNOWN_ACK_1, otherAcknowledgement);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ImmutableAcknowledgements.of(acknowledgements, KNOWN_DITTO_HEADERS))
                .withMessage("The entity ID <%s> is not compatible with <%s>!", otherEntityId, KNOWN_ENTITY_ID)
                .withNoCause();
    }

    @Test
    public void tryToGetInstanceWithNullDittoHeaders() {
        assertThatNullPointerException()
                .isThrownBy(() -> ImmutableAcknowledgements.of(knownAcknowledgements, null))
                .withMessage("The dittoHeaders must not be null!")
                .withNoCause();
    }

    @Test
    public void toJsonReturnsExpected() {
        final JsonObject actual = knownAcknowledgementsWith2Acks.toJson();

        assertThat(actual).isEqualTo(knownJsonRepresentation);
    }

    @Test
    public void getEntityReturnsExpected() {
        final Optional<JsonValue> actual = knownAcknowledgementsWith2Acks.getEntity();

        assertThat(actual).contains(knownAcknowledgementsGetEntityJsonRepresentation);
    }

    @Test
    public void getHttpStatusWithOneItem() {
        final Acknowledgement acknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.CONFLICT, KNOWN_DITTO_HEADERS, null);
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(Lists.list(acknowledgement), KNOWN_DITTO_HEADERS);

        assertThat(underTest.getHttpStatus()).isEqualTo(acknowledgement.getHttpStatus());
    }

    @Test
    public void getHttpStatusWithMultipleItemsWithSameStatusCode() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getHttpStatus()).isEqualTo(KNOWN_HTTP_STATUS);
    }

    @Test
    public void getHttpStatusWithMultipleItemsWithDivergentStatusCodes() {
        final Acknowledgement otherAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.CONFLICT, KNOWN_DITTO_HEADERS, null);
        final List<Acknowledgement> acknowledgements = Lists.list(KNOWN_ACK_1, otherAcknowledgement);
        final ImmutableAcknowledgements underTest = ImmutableAcknowledgements.of(acknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getHttpStatus()).isEqualTo(HttpStatus.FAILED_DEPENDENCY);
    }

    @Test
    public void getMissingAcknowledgementLabelsReturnsExpected() {
        final Acknowledgement timeoutAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.REQUEST_TIMEOUT, KNOWN_DITTO_HEADERS, null);
        final List<Acknowledgement> acknowledgements = Lists.list(KNOWN_ACK_1, timeoutAcknowledgement);
        final ImmutableAcknowledgements underTest = ImmutableAcknowledgements.of(acknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getMissingAcknowledgementLabels()).containsOnly(timeoutAcknowledgement.getLabel());
    }

    @Test
    public void getSuccessfulAcknowledgementsReturnsExpected() {
        final Acknowledgement timeoutAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.REQUEST_TIMEOUT, KNOWN_DITTO_HEADERS, null);
        final Acknowledgement successfulAcknowledgement = KNOWN_ACK_1;
        final List<Acknowledgement> acknowledgements = Lists.list(successfulAcknowledgement, timeoutAcknowledgement);
        final ImmutableAcknowledgements underTest = ImmutableAcknowledgements.of(acknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getSuccessfulAcknowledgements()).containsOnly(successfulAcknowledgement);
    }

    @Test
    public void getFailedAcknowledgementsReturnsExpected() {
        final Acknowledgement timeoutAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.REQUEST_TIMEOUT, KNOWN_DITTO_HEADERS, null);
        final Acknowledgement failedAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.NOT_FOUND, KNOWN_DITTO_HEADERS, null);
        final List<Acknowledgement> acknowledgements =
                Lists.list(KNOWN_ACK_1, timeoutAcknowledgement, failedAcknowledgement);
        final ImmutableAcknowledgements underTest = ImmutableAcknowledgements.of(acknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getFailedAcknowledgements()).containsOnly(timeoutAcknowledgement, failedAcknowledgement);
    }

    @Test
    public void iteratorDoesNotAllowModification() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        final Iterator<Acknowledgement> acknowledgementIterator = underTest.iterator();
        acknowledgementIterator.next();

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(acknowledgementIterator::remove)
                .withNoCause();
    }

    @Test
    public void getSizeReturnsExpected() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getSize()).isEqualTo(knownAcknowledgements.size());
    }

    @Test
    public void isEmptyReturnsExpected() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.isEmpty()).isFalse();
    }

    @Test
    public void getAcknowledgementReturnsExpected() {
        final Acknowledgement timeoutAcknowledgement =
                ImmutableAcknowledgement.of(DittoAcknowledgementLabel.TWIN_PERSISTED, KNOWN_ENTITY_ID,
                        HttpStatus.REQUEST_TIMEOUT, KNOWN_DITTO_HEADERS, null);
        final AcknowledgementLabel customAckLabel = AcknowledgementLabel.of("foo");
        final Acknowledgement customAcknowledgement = ImmutableAcknowledgement.of(customAckLabel, KNOWN_ENTITY_ID,
                HttpStatus.OK, KNOWN_DITTO_HEADERS, null);
        final List<Acknowledgement> acknowledgements =
                Lists.list(KNOWN_ACK_1, timeoutAcknowledgement, customAcknowledgement);
        final ImmutableAcknowledgements underTest = ImmutableAcknowledgements.of(acknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getAcknowledgement(DittoAcknowledgementLabel.TWIN_PERSISTED))
                .contains(timeoutAcknowledgement);
        assertThat(underTest.getAcknowledgement(customAckLabel)).contains(customAcknowledgement);
    }

    @Test
    public void getDittoHeadersReturnsExpected() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getDittoHeaders()).isEqualTo(KNOWN_DITTO_HEADERS);
    }

    @Test
    public void responseRequiredIsSetToFalse() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, DittoHeaders.empty());

        assertThat(underTest.getDittoHeaders().isResponseRequired()).isFalse();
    }

    @Test
    public void setDittoHeadersWorksAsExpected() {
        final DittoHeaders newDittoHeaders = DittoHeaders.newBuilder()
                .randomCorrelationId()
                .responseRequired(false)
                .build();
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        final ImmutableAcknowledgements newAcknowledgements = underTest.setDittoHeaders(newDittoHeaders);

        try (final AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(newAcknowledgements.getDittoHeaders())
                    .as("new headers")
                    .isEqualTo(newDittoHeaders);
            softly.assertThat((CharSequence) newAcknowledgements.getEntityId())
                    .as("same entity ID")
                    .isEqualTo(underTest.getEntityId());
            softly.assertThat(newAcknowledgements.getHttpStatus())
                    .as("same HTTP status")
                    .isEqualTo(underTest.getHttpStatus());
            softly.assertThat(newAcknowledgements)
                    .as("same elements")
                    .containsExactlyElementsOf(underTest);
        }
    }

    @Test
    public void getEntityIdReturnsExpected() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat((CharSequence) underTest.getEntityId()).isEqualTo(KNOWN_ENTITY_ID);
    }

    @Test
    public void getEntityTypeReturnsExpected() {
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat((CharSequence) underTest.getEntityType()).isEqualTo(KNOWN_ENTITY_ID.getEntityType());
    }

    @Test
    public void getTypeReturnsExpected() {
        final String expected = Acknowledgements.getType(KNOWN_ENTITY_ID.getEntityType());
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(knownAcknowledgements, KNOWN_DITTO_HEADERS);

        assertThat(underTest.getType()).isEqualTo(expected);
    }

    @Test
    public void getEntityWith2AcksReturnsExpected() {
        final Optional<JsonValue> actual = knownAcknowledgementsWith2Acks.getEntity(JsonSchemaVersion.LATEST);

        assertThat(actual).contains(knownAcknowledgementsGetEntityJsonRepresentation);
    }

    @Test
    public void getEntityWith1AckReturnsExpected() {
        final Optional<JsonValue> expected = KNOWN_ACK_1.getEntity(JsonSchemaVersion.LATEST);
        final ImmutableAcknowledgements underTest =
                ImmutableAcknowledgements.of(Collections.singleton(KNOWN_ACK_1), KNOWN_DITTO_HEADERS);

        final Optional<JsonValue> actual = underTest.getEntity(JsonSchemaVersion.LATEST);

        assertThat(actual).isEqualTo(expected);
    }

}
