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
import java.util.Optional;
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
import org.eclipse.ditto.model.things.Attributes;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;

/**
 * Response to a {@link ModifyAttributes} command.
 */
@Immutable
@JsonParsableCommandResponse(type = ModifyAttributesResponse.TYPE)
public final class ModifyAttributesResponse extends AbstractCommandResponse<ModifyAttributesResponse>
        implements ThingModifyCommandResponse<ModifyAttributesResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + ModifyAttributes.NAME;

    static final JsonFieldDefinition<JsonObject> JSON_ATTRIBUTES =
            JsonFactory.newJsonObjectFieldDefinition("attributes", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    private final ThingId thingId;
    private final Attributes attributesCreated;

    private ModifyAttributesResponse(final ThingId thingId,
            final HttpStatus httpStatus,
            final Attributes attributesCreated,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.thingId = checkNotNull(thingId, "Thing ID");
        this.attributesCreated = checkNotNull(attributesCreated, "Attributes");
    }

    /**
     * Returns a new {@code ModifyAttributesResponse} for a created Attributes. This corresponds to the HTTP status
     * {@link HttpStatus#CREATED}.
     *
     * @param thingId the Thing ID of the created Attributes.
     * @param attributes the created Attributes.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a created FeatureProperties.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyAttributesResponse created(final ThingId thingId, final Attributes attributes,
            final DittoHeaders dittoHeaders) {

        return new ModifyAttributesResponse(thingId, HttpStatus.CREATED, attributes, dittoHeaders);
    }

    /**
     * Returns a new {@code ModifyAttributesResponse} for a modified Attributes. This corresponds to the HTTP status
     * code {@link HttpStatus#NO_CONTENT}.
     *
     * @param thingId the Thing ID of the modified Attributes.
     * @param dittoHeaders the headers of the ThingCommand which caused the new response.
     * @return a command response for a modified FeatureProperties.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static ModifyAttributesResponse modified(final ThingId thingId, final DittoHeaders dittoHeaders) {
        return new ModifyAttributesResponse(thingId, HttpStatus.NO_CONTENT, ThingsModelFactory.nullAttributes(),
                dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyAttributes} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if any argument is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static ModifyAttributesResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@link ModifyAttributes} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static ModifyAttributesResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<ModifyAttributesResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String extractedThingId =
                            jsonObject.getValueOrThrow(ThingCommandResponse.JsonFields.JSON_THING_ID);
                    final ThingId thingId = ThingId.of(extractedThingId);
                    final JsonObject attributesJsonObject = jsonObject.getValueOrThrow(JSON_ATTRIBUTES);

                    final Attributes extractedAttributes = !attributesJsonObject.isNull()
                            ? ThingsModelFactory.newAttributes(attributesJsonObject)
                            : ThingsModelFactory.nullAttributes();

                    return new ModifyAttributesResponse(thingId, httpStatus, extractedAttributes, dittoHeaders);
                });
    }

    @Override
    public ThingId getEntityId() {
        return thingId;
    }

    /**
     * Returns the created {@code Attributes}.
     *
     * @return the created Attributes.
     */
    public Attributes getAttributesCreated() {
        return attributesCreated;
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.of(attributesCreated);
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.of("/attributes");
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ThingCommandResponse.JsonFields.JSON_THING_ID, thingId.toString(), predicate);
        jsonObjectBuilder.set(JSON_ATTRIBUTES, attributesCreated.toJson(schemaVersion, thePredicate), predicate);
    }

    @Override
    public ModifyAttributesResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return HttpStatus.CREATED.equals(getHttpStatus())
                ? created(thingId, attributesCreated, dittoHeaders)
                : modified(thingId, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof ModifyAttributesResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModifyAttributesResponse that = (ModifyAttributesResponse) o;
        return that.canEqual(this) &&
                Objects.equals(thingId, that.thingId) &&
                Objects.equals(attributesCreated, that.attributesCreated) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), thingId, attributesCreated);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", thingId=" + thingId
                + ", attributesCreated=" + attributesCreated + "]";
    }

}
