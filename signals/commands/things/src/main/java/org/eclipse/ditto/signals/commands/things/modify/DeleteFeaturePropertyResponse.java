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
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link DeleteFeatureProperty} command.
 */
@Immutable
@JsonParsableCommandResponse(type = DeleteFeaturePropertyResponse.TYPE)
public final class DeleteFeaturePropertyResponse extends AbstractCommandResponse<DeleteFeaturePropertyResponse>
        implements ThingModifyCommandResponse<DeleteFeaturePropertyResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + DeleteFeatureProperty.NAME;

    static final JsonFieldDefinition<String> JSON_FEATURE_ID =
            JsonFactory.newStringFieldDefinition("featureId", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<String> JSON_PROPERTY =
            JsonFactory.newStringFieldDefinition("property", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final String featureId;
    private final JsonPointer propertyPointer;

    private DeleteFeaturePropertyResponse(final ThingId thingId,
            final String featureId,
            final JsonPointer propertyPointer,
            final DittoHeaders dittoHeaders) {

        super(TYPE, HttpStatus.NO_CONTENT, dittoHeaders);
        this.thingId = checkNotNull(thingId, "Thing ID");
        this.featureId = checkNotNull(featureId, "Feature ID");
        this.propertyPointer = checkPropertyPointer(propertyPointer);
    }

    private static JsonPointer checkPropertyPointer(final JsonPointer propertyPointer) {
        checkNotNull(propertyPointer, "Property JsonPointer");
        return ThingsModelFactory.validateFeaturePropertyPointer(propertyPointer);
    }

    /**
     * Creates a response to a {@link DeleteFeatureProperty} command.
     *
     * @param thingId the Thing ID of the deleted feature property.
     * @param featureId the {@code Feature}'s ID whose Property was deleted.
     * @param propertyPointer the JSON pointer of the deleted Property.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of {@code propertyPointer} are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static DeleteFeaturePropertyResponse of(final ThingId thingId,
            final String featureId,
            final JsonPointer propertyPointer,
            final DittoHeaders dittoHeaders) {

        return new DeleteFeaturePropertyResponse(thingId, featureId, propertyPointer, dittoHeaders);
    }

    /**
     * Creates a response to a {@link DeleteFeatureProperty} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of property pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static DeleteFeaturePropertyResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link DeleteFeatureProperty} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     * @throws org.eclipse.ditto.json.JsonKeyInvalidException if keys of property pointer are not valid
     * according to pattern {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NO_CONTROL_CHARS_NO_SLASHES_PATTERN}.
     */
    public static DeleteFeaturePropertyResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<DeleteFeaturePropertyResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final String extractedFeatureId = jsonObject.getValueOrThrow(JSON_FEATURE_ID);
                    final String extractedPointerString = jsonObject.getValueOrThrow(JSON_PROPERTY);
                    final JsonPointer extractedPointer = JsonFactory.newPointer(extractedPointerString);

                    return of(thingId, extractedFeatureId, extractedPointer, dittoHeaders);
                });
    }

    @Override
    public ThingId getEntityId() {
        return thingId;
    }

    /**
     * Returns the {@code Feature}'s ID whose property was deleted.
     *
     * @return the Feature's ID.
     */
    public String getFeatureId() {
        return featureId;
    }

    /**
     * Returns the JSON pointer of the Property to delete.
     *
     * @return the JSON pointer of the Property to delete.
     */
    public JsonPointer getPropertyPointer() {
        return propertyPointer;
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonFactory.newPointer("/features/" + featureId + "/properties" + propertyPointer);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> predicate) {

        final Predicate<JsonField> p = schemaVersion.and(predicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), p);
        jsonObjectBuilder.set(JSON_FEATURE_ID, featureId, p);
        jsonObjectBuilder.set(JSON_PROPERTY, propertyPointer.toString(), p);
    }

    @Override
    public DeleteFeaturePropertyResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(thingId, featureId, propertyPointer, dittoHeaders);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeleteFeaturePropertyResponse that = (DeleteFeaturePropertyResponse) o;
        return Objects.equals(thingId, that.thingId) &&
                Objects.equals(featureId, that.featureId) &&
                Objects.equals(propertyPointer, that.propertyPointer) &&
                that.canEqual(this) &&
                super.equals(o);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof DeleteFeaturePropertyResponse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(thingId, featureId, propertyPointer, super.hashCode());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId
                + ", featureId=" + featureId + ", propertyPointer=" + propertyPointer + "]";
    }

}
