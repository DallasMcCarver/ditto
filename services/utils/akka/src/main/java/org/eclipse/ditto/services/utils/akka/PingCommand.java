/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.utils.akka;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.id.WithEntityId;
import org.eclipse.ditto.model.base.entity.type.EntityType;
import org.eclipse.ditto.model.base.json.Jsonifiable;

/**
 * Internal simple "ping" command to be sent to PersistenceActors (identified by the contained {@code entityId})
 * which is Jsonifiable and has an optional correlationId and optionally payload.
 */
@Immutable
public final class PingCommand implements Jsonifiable<JsonObject>, WithEntityId {

    private final EntityId entityId;
    @Nullable private final String correlationId;
    @Nullable private final JsonValue payload;

    private PingCommand(final EntityId entityId, @Nullable final String correlationId,
            @Nullable final JsonValue payload) {
        this.entityId = entityId;
        this.correlationId = correlationId;
        this.payload = payload;
    }

    /**
     * Returns a new {@code PingCommand} instance.
     *
     * @param entityId the Entity ID to send the ping command to.
     * @param correlationId an optional identifier correlating a PingCommand to a PingCommandResponse.
     * @param payload optional payload to transmit with the PingCommand.
     * @return the new PingCommand instance.
     */
    public static PingCommand of(final EntityId entityId, @Nullable final String correlationId,
            @Nullable final JsonValue payload) {
        return new PingCommand(entityId, correlationId, payload);
    }

    /**
     * Creates a new {@code PingCommand} from a JSON string.
     *
     * @param jsonString the JSON string of which a new PingCommand is to be created.
     * @return the PingCommand which was created from the given JSON string.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} does not contain a JSON
     * object or if it is not valid JSON.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if the passed in {@code jsonString} was not in the
     * expected 'PingCommand'
     * format.
     */
    public static PingCommand fromJson(final String jsonString) {
        return fromJson(JsonFactory.newObject(jsonString));
    }

    /**
     * Creates a new {@code PingCommand} from a JSON object.
     *
     * @param jsonObject the JSON object of which a new PingCommand is to be created.
     * @return the PingCommand which was created from the given JSON object.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonObject} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} is not valid JSON.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if the passed in {@code jsonObject} was not in the expected 'PingCommand'
     * format.
     */
    public static PingCommand fromJson(final JsonObject jsonObject) {
        final EntityType entityType = EntityType.of(jsonObject.getValueOrThrow(JsonFields.ENTITY_TYPE));
        final EntityId extractedEntityId =
                EntityId.of(entityType, jsonObject.getValueOrThrow(JsonFields.ENTITY_ID));
        final String extractedCorrelationId = jsonObject.getValue(JsonFields.CORRELATION_ID).orElse(null);
        final JsonValue extractedPayload = jsonObject.getValue(JsonFields.PAYLOAD).orElse(null);

        return of(extractedEntityId, extractedCorrelationId, extractedPayload);
    }

    @Override
    public EntityId getEntityId() {
        return entityId;
    }

    /**
     * Returns the optional correlationId.
     *
     * @return the optional correlationId.
     */
    public Optional<String> getCorrelationId() {
        return Optional.ofNullable(correlationId);
    }

    /**
     * Returns the optional JSON payload.
     *
     * @return the optional JSON payload.
     */
    public Optional<JsonValue> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Override
    public JsonObject toJson() {
        final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder()
                .set(JsonFields.ENTITY_TYPE, entityId.getEntityType().toString())
                .set(JsonFields.ENTITY_ID, entityId.toString());
        if (null != correlationId) {
            jsonObjectBuilder.set(JsonFields.CORRELATION_ID, correlationId);
        }
        if (null != payload) {
            jsonObjectBuilder.set(JsonFields.PAYLOAD, payload);
        }
        return jsonObjectBuilder.build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PingCommand that = (PingCommand) o;
        return Objects.equals(entityId, that.entityId) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, correlationId, payload);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "entityId=" + entityId +
                ", correlationId=" + correlationId +
                ", payload=" + payload +
                "]";
    }

    /**
     * An enumeration of the known {@link JsonFieldDefinition}s of a PingCommand.
     */
    @Immutable
    public static final class JsonFields {

        /**
         * JSON field containing the entity ID.
         */
        static final JsonFieldDefinition<String> ENTITY_TYPE = JsonFactory.newStringFieldDefinition("entityType");

        /**
         * JSON field containing the entity ID.
         */
        static final JsonFieldDefinition<String> ENTITY_ID = JsonFactory.newStringFieldDefinition("entityId");

        /**
         * JSON field containing the correlationId.
         */
        static final JsonFieldDefinition<String> CORRELATION_ID = JsonFactory.newStringFieldDefinition("correlationId");

        /**
         * JSON field containing optional payload.
         */
        static final JsonFieldDefinition<JsonValue> PAYLOAD = JsonFactory.newJsonValueFieldDefinition("payload");

        private JsonFields() {
            throw new AssertionError();
        }

    }

}
