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
package org.eclipse.ditto.signals.base;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.id.WithEntityId;
import org.eclipse.ditto.model.base.entity.type.EntityType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersSettable;
import org.eclipse.ditto.model.base.json.Jsonifiable;

/**
 * A message envelope for messages to PersistenceActors which do not contain itself an ID. Holds both an ID and the
 * message (message) which should be delivered to the PersistenceActor.
 */
public final class ShardedMessageEnvelope
        implements Jsonifiable<JsonObject>, DittoHeadersSettable<ShardedMessageEnvelope>, WithEntityId {

    /**
     * JSON field containing the identifier of a {@code ShardedMessageEnvelope}.
     */
    public static final JsonFieldDefinition<String> JSON_ID = JsonFactory.newStringFieldDefinition("id");


    /**
     * JSON field containing the type of the entity the id identifies.
     */
    public static final JsonFieldDefinition<String> JSON_ID_TYPE = JsonFactory.newStringFieldDefinition("entityType");

    /**
     * JSON field containing the type of the message of a {@code ShardedMessageEnvelope}.
     */
    public static final JsonFieldDefinition<String> JSON_TYPE = JsonFactory.newStringFieldDefinition("type");

    /**
     * JSON field containing the message of a {@code ShardedMessageEnvelope}.
     */
    public static final JsonFieldDefinition<JsonObject> JSON_MESSAGE =
            JsonFactory.newJsonObjectFieldDefinition("message");

    /**
     * JSON field containing the {@code DittoHeaders} of a {@code ShardedMessageEnvelope}.
     */
    public static final JsonFieldDefinition<JsonObject> JSON_DITTO_HEADERS =
            JsonFactory.newJsonObjectFieldDefinition("dittoHeaders");

    private final EntityId id;
    private final String type;
    private final JsonObject message;
    private final DittoHeaders dittoHeaders;

    private ShardedMessageEnvelope(final EntityId id,
            final String type,
            final JsonObject message,
            final DittoHeaders dittoHeaders) {

        this.id = checkNotNull(id, "Message ID");
        this.type = checkNotNull(type, "Type");
        this.message = checkNotNull(message, "Message");
        this.dittoHeaders = checkNotNull(dittoHeaders, "Command Headers");
    }

    /**
     * Returns a new {@code ShardedMessageEnvelope} for the specified {@code id} and {@code message}.
     *
     * @param id the identifier.
     * @param type the type of the message.
     * @param message the message.
     * @param dittoHeaders the command headers.
     * @return the ShardedMessageEnvelope.
     */
    public static ShardedMessageEnvelope of(final EntityId id,
            final String type,
            final JsonObject message,
            final DittoHeaders dittoHeaders) {

        return new ShardedMessageEnvelope(id, type, message, dittoHeaders);
    }

    /**
     * Returns a new {@code ShardedMessageEnvelope} parsed from the specified {@code jsonObject}.
     *
     * @param jsonObject the JSON object.
     * @return the ShardedMessageEnvelope.
     */
    public static ShardedMessageEnvelope fromJson(final JsonObject jsonObject) {
        final EntityType entityType = EntityType.of(jsonObject.getValueOrThrow(JSON_ID_TYPE));
        final String extractedId = jsonObject.getValueOrThrow(JSON_ID);
        final EntityId entityId = EntityId.of(entityType, extractedId);
        final String extractedType = jsonObject.getValueOrThrow(JSON_TYPE);
        final JsonObject extractedMessage = jsonObject.getValueOrThrow(JSON_MESSAGE);
        final JsonObject jsonDittoHeaders = jsonObject.getValueOrThrow(JSON_DITTO_HEADERS);
        final DittoHeaders extractedDittoHeaders = DittoHeaders.newBuilder(jsonDittoHeaders).build();

        return of(entityId, extractedType, extractedMessage, extractedDittoHeaders);
    }

    /**
     * Returns the ID of the envelope.
     *
     * @return the ID of the envelope.
     */
    public EntityId getEntityId() {
        return id;
    }

    /**
     * Returns the type of the message.
     *
     * @return the type of the message.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the message of the envelope.
     *
     * @return the message of the envelope.
     */
    public JsonObject getMessage() {
        return message;
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return dittoHeaders;
    }

    @Override
    public ShardedMessageEnvelope setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(id, type, message, dittoHeaders);
    }

    @Override
    public JsonObject toJson() {
        return JsonObject.newBuilder()
                .set(JSON_ID_TYPE, id.getEntityType().toString())
                .set(JSON_ID, String.valueOf(id))
                .set(JSON_TYPE, type)
                .set(JSON_MESSAGE, message)
                .set(JSON_DITTO_HEADERS, dittoHeaders.toJson())
                .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ShardedMessageEnvelope that = (ShardedMessageEnvelope) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(message, that.message)
                && Objects.equals(dittoHeaders, that.dittoHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, message, dittoHeaders);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "id=" + id + ", type=" + type + ", message=" + message
                + ", dittoHeaders=" + dittoHeaders + "]";
    }

}
