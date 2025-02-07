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
package org.eclipse.ditto.signals.commands.things.query;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link RetrieveFeature} command.
 */
@Immutable
@JsonParsableCommandResponse(type = RetrieveFeatureResponse.TYPE)
public final class RetrieveFeatureResponse extends AbstractCommandResponse<RetrieveFeatureResponse> implements
        ThingQueryCommandResponse<RetrieveFeatureResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + RetrieveFeature.NAME;

    static final JsonFieldDefinition<String> JSON_FEATURE_ID =
            JsonFactory.newStringFieldDefinition("featureId", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<JsonObject> JSON_FEATURE =
            JsonFactory.newJsonObjectFieldDefinition("feature", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final Feature feature;

    private RetrieveFeatureResponse(final ThingId thingId, final Feature feature, final DittoHeaders dittoHeaders) {
        super(TYPE, HttpStatus.OK, dittoHeaders);
        this.thingId = checkNotNull(thingId, "thing ID");
        this.feature = feature;
    }


    /**
     * Creates a response to a {@link RetrieveFeature} command.
     *
     * @param thingId the Thing ID of the retrieved feature.
     * @param featureId the identifier of the retrieved Feature.
     * @param jsonObject the retrieved Feature JSON.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument but {@code jsonObject} is {@code null}.
     */
    public static RetrieveFeatureResponse of(final ThingId thingId,
            final String featureId,
            @Nullable final JsonObject jsonObject,
            final DittoHeaders dittoHeaders) {

        checkNotNull(featureId, "Feature ID");

        final Feature feature = null != jsonObject
                ? ThingsModelFactory.newFeatureBuilder(jsonObject).useId(featureId).build()
                : ThingsModelFactory.nullFeature(featureId);

        return new RetrieveFeatureResponse(thingId, feature, dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveFeature} command.
     *
     * @param thingId the Thing ID of the retrieved feature.
     * @param feature the retrieved Feature.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrieveFeatureResponse of(final ThingId thingId, final Feature feature,
            final DittoHeaders dittoHeaders) {

        return new RetrieveFeatureResponse(thingId, checkNotNull(feature, "retrieved Feature"), dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveFeature} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrieveFeatureResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link RetrieveFeature} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrieveFeatureResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<RetrieveFeatureResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final JsonObject extractedFeatureJsonObject = jsonObject.getValueOrThrow(JSON_FEATURE);
                    final Feature extractedFeature = ThingsModelFactory.newFeatureBuilder(extractedFeatureJsonObject)
                            .useId(jsonObject.getValueOrThrow(JSON_FEATURE_ID))
                            .build();

                    return of(thingId, extractedFeature, dittoHeaders);
                });
    }

    @Override
    public ThingId getEntityId() {
        return thingId;
    }

    /**
     * Returns the identifier of the {@code Feature} whose properties were modified.
     *
     * @return the identifier.
     */
    public String getFeatureId() {
        return feature.getId();
    }

    /**
     * Returns the retrieved Feature.
     *
     * @return the retrieved Feature.
     */
    public Feature getFeature() {
        return feature;
    }

    @Override
    public JsonObject getEntity(final JsonSchemaVersion schemaVersion) {
        return feature.toJson(schemaVersion);
    }

    @Override
    public RetrieveFeatureResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(thingId, getFeatureId(), entity.asObject(), getDittoHeaders());
    }

    @Override
    public RetrieveFeatureResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(thingId, feature, dittoHeaders);
    }

    @Override
    public JsonPointer getResourcePath() {
        final String path = "/features/" + getFeatureId();
        return JsonPointer.of(path);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        jsonObjectBuilder.set(JSON_FEATURE_ID, getFeatureId(), predicate);
        jsonObjectBuilder.set(JSON_FEATURE, getEntity(schemaVersion), predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrieveFeatureResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetrieveFeatureResponse that = (RetrieveFeatureResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(feature, that.feature) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, feature);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString()
                + ", thingId=" + thingId
                + ", feature=" + feature
                + "]";
    }

}
