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
package org.eclipse.ditto.services.models.things.commands.sudo;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.exceptions.DittoJsonException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.utils.jsr305.annotations.AllValuesAreNonnullByDefault;

/**
 * Response to a {@link SudoRetrieveThings} command.
 */
@Immutable
@AllValuesAreNonnullByDefault
@JsonParsableCommandResponse(type = SudoRetrieveThingsResponse.TYPE)
public final class SudoRetrieveThingsResponse extends AbstractCommandResponse<SudoRetrieveThingsResponse> implements
        SudoCommandResponse<SudoRetrieveThingsResponse> {

    /**
     * Name of the response.
     */
    public static final String NAME = "sudoRetrieveThingsResponse";

    /**
     * Type of this command.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    static final JsonFieldDefinition<JsonArray> JSON_THINGS =
            JsonFactory.newJsonArrayFieldDefinition("payload/things", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    static final JsonFieldDefinition<String> JSON_THINGS_PLAIN_JSON =
            JsonFactory.newStringFieldDefinition("payload/thingsPlainJson", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    private final String thingsPlainJson;

    @Nullable private JsonArray things;

    private SudoRetrieveThingsResponse(@Nullable final JsonArray things, final String thingsPlainJson,
            final DittoHeaders dittoHeaders) {

        super(TYPE, HttpStatus.OK, dittoHeaders);
        this.thingsPlainJson = checkNotNull(thingsPlainJson, "Things plain JSON");
        this.things = things;
    }

    /**
     * Creates a new instance of {@code SudoRetrieveThingsResponse}.
     *
     * @param thingsPlainJson the retrieved Things.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrieveThingsResponse of(final String thingsPlainJson, final DittoHeaders dittoHeaders) {
        return new SudoRetrieveThingsResponse(null, thingsPlainJson, dittoHeaders);
    }

    /**
     * Creates a response to a {@link SudoRetrieveThingsResponse} command.
     *
     * @param thingsPlainJson the retrieved Things.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrieveThingsResponse of(final List<String> thingsPlainJson, final DittoHeaders dittoHeaders) {
        return new SudoRetrieveThingsResponse(null, thingsPlainJson.stream()
                .collect(Collectors.joining(",", "[", "]")), dittoHeaders);
    }

    /**
     * Creates a new instance of {@code SudoRetrieveThingsResponse}.
     *
     * @param things the retrieved Things.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrieveThingsResponse of(final JsonArray things, final DittoHeaders dittoHeaders) {
        return new SudoRetrieveThingsResponse(things, things.toString(), dittoHeaders);
    }

    /**
     * Creates a new instance of {@code SudoRetrieveThingsResponse}.
     *
     * @param things the Things.
     * @param predicate the predicate to apply to the things when transforming to JSON.
     * @param dittoHeaders the command headers of the request.
     * @return a new SudoRetrieveThingsResponse object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrieveThingsResponse of(final List<Thing> things, final Predicate<JsonField> predicate,
            final DittoHeaders dittoHeaders) {
        final JsonArray thingsArray = checkNotNull(things, "Things").stream()
                .map(thing -> thing.toJson(dittoHeaders.getSchemaVersion().orElse(JsonSchemaVersion.LATEST),
                        predicate))
                .collect(JsonCollectors.valuesToArray());
        return new SudoRetrieveThingsResponse(thingsArray, thingsArray.toString(), dittoHeaders);
    }

    /**
     * Creates a new instance of {@code SudoRetrieveThingsResponse}.
     *
     * @param things the Things.
     * @param fieldSelector the JsonFieldSelector to apply to the passed things when transforming to JSON.
     * @param predicate the predicate to apply to the things when transforming to JSON.
     * @param dittoHeaders the command headers of the request.
     * @return a new SudoRetrieveThingsResponse object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrieveThingsResponse of(final List<Thing> things,
            final JsonFieldSelector fieldSelector,
            final Predicate<JsonField> predicate,
            final DittoHeaders dittoHeaders) {

        final JsonArray thingsArray = checkNotNull(things, "Things").stream()
                .map(thing -> thing.toJson(dittoHeaders.getSchemaVersion().orElse(JsonSchemaVersion.LATEST),
                        fieldSelector, predicate))
                .collect(JsonCollectors.valuesToArray());
        return new SudoRetrieveThingsResponse(thingsArray, thingsArray.toString(), dittoHeaders);
    }

    /**
     * Creates a new {@code SudoRetrieveThingsResponse} from a JSON string.
     *
     * @param jsonString the JSON string of which a new SudoRetrieveThingsResponse instance is to be created.
     * @param dittoHeaders the optional command headers of the request.
     * @return the {@code SudoRetrieveThingsResponse} which was created from the given JSON string.
     * @throws NullPointerException if {@code dittoHeaders} is {@code null}.
     * @throws DittoJsonException if the passed in {@code jsonString} was {@code null}, empty or not in the expected
     * 'SudoRetrieveThingsResponse' format.
     */
    public static SudoRetrieveThingsResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        final var jsonObject = DittoJsonException.wrapJsonRuntimeException(() -> JsonFactory.newObject(jsonString));
        return fromJson(jsonObject, dittoHeaders);
    }

    /**
     * Creates a new {@code SudoRetrieveThingsResponse} from a JSON object.
     *
     * @param jsonObject the JSON object of which a new SudoRetrieveThingsResponse instance is to be created.
     * @param dittoHeaders the optional command headers of the request.
     * @return the {@code SudoRetrieveThingsResponse} which was created from the given JSON object.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * 'SudoRetrieveThingsResponse' format.
     */
    public static SudoRetrieveThingsResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<SudoRetrieveThingsResponse>(TYPE, jsonObject)
                .deserialize(httpStatus -> {
                    final var thingsJsonArray = jsonObject.getValue(JSON_THINGS).orElse(null);
                    final var plainJsonString = jsonObject.getValue(JSON_THINGS_PLAIN_JSON)
                            .orElseGet(() -> String.valueOf(thingsJsonArray));

                    return new SudoRetrieveThingsResponse(thingsJsonArray, plainJsonString, dittoHeaders);
                });
    }

    /**
     * Returns the {@code Thing}s.
     *
     * @return the Things.
     */
    public List<Thing> getThings() {
        return getThingStream(lazyLoadThingsJsonArray()).collect(Collectors.toList());
    }

    private static Stream<Thing> getThingStream(final JsonArray thingsArray) {
        return thingsArray.stream()
                .filter(JsonValue::isObject)
                .map(JsonValue::asObject)
                .map(ThingsModelFactory::newThing);
    }

    private JsonArray lazyLoadThingsJsonArray() {
        if (things == null) {
            things = JsonFactory.readFrom(thingsPlainJson).asArray();
        }
        return things;
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return lazyLoadThingsJsonArray();
    }

    @Override
    public SudoRetrieveThingsResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(entity.asArray(), getDittoHeaders());
    }

    @Override
    public SudoRetrieveThingsResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(thingsPlainJson, dittoHeaders);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(JSON_THINGS_PLAIN_JSON, thingsPlainJson, predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), things, thingsPlainJson);
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "pmd:SimplifyConditional"})
    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SudoRetrieveThingsResponse that = (SudoRetrieveThingsResponse) o;
        return that.canEqual(this) && Objects.equals(things, that.things)
                && Objects.equals(thingsPlainJson, that.thingsPlainJson) && super.equals(that);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof SudoRetrieveThingsResponse;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", things=" + things + ", thingsPlainJson=" +
                thingsPlainJson + "]";
    }

}
