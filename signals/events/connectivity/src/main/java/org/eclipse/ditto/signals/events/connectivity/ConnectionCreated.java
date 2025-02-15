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
package org.eclipse.ditto.signals.events.connectivity;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonParsableEvent;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.signals.events.base.EventJsonDeserializer;

/**
 * This event is emitted after a {@link Connection} was created.
 */
@Immutable
@JsonParsableEvent(name = ConnectionCreated.NAME, typePrefix= ConnectivityEvent.TYPE_PREFIX)
public final class ConnectionCreated extends AbstractConnectivityEvent<ConnectionCreated>
        implements ConnectivityEvent<ConnectionCreated> {

    /**
     * Name of this event.
     */
    public static final String NAME = "connectionCreated";

    /**
     * Type of this event.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    private final Connection connection;

    private ConnectionCreated(final Connection connection,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders,
            @Nullable final Metadata metadata) {

        super(TYPE, connection.getId(), revision, timestamp, dittoHeaders, metadata);
        this.connection = connection;
    }

    /**
     * Returns a new {@code ConnectionCreated} event.
     *
     * @param connection the created Connection.
     * @param revision the revision of the Connection.
     * @param timestamp the timestamp of this event.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @param metadata the metadata to apply for the event.
     * @return the event.
     * @throws NullPointerException if {@code connection} or {@code dittoHeaders} are {@code null}.
     */
    public static ConnectionCreated of(final Connection connection,
            final long revision,
            @Nullable final Instant timestamp,
            final DittoHeaders dittoHeaders,
            @Nullable final Metadata metadata) {

        checkNotNull(connection, "Connection");
        return new ConnectionCreated(connection, revision, timestamp, dittoHeaders, metadata);
    }

    /**
     * Creates a {@code ConnectionCreated} event from a JSON string.
     *
     * @param jsonString the JSON string of which the event is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the event.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static ConnectionCreated fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a {@code ConnectionCreated} event from a JSON object.
     *
     * @param jsonObject the JSON object of which the event is to be created.
     * @param dittoHeaders the headers of the command which was the cause of this event.
     * @return the event.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static ConnectionCreated fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new EventJsonDeserializer<ConnectionCreated>(TYPE, jsonObject)
                .deserialize((revision, timestamp, metadata) -> {
                    final JsonObject connectionJsonObject = jsonObject
                            .getValueOrThrow(ConnectivityEvent.JsonFields.CONNECTION);
                    final Connection readConnection = ConnectivityModelFactory.connectionFromJson(connectionJsonObject);

                    return of(readConnection, revision, timestamp, dittoHeaders, metadata);
                });
    }

    /**
     * Returns the created {@code Connection}.
     *
     * @return the Connection.
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public Optional<JsonValue> getEntity(final JsonSchemaVersion schemaVersion) {
        return Optional.of(connection.toJson(schemaVersion));
    }

    @Override
    public JsonPointer getResourcePath() {
        return JsonPointer.empty();
    }

    @Override
    public ConnectionCreated setRevision(final long revision) {
        return of(connection, revision, getTimestamp().orElse(null), getDittoHeaders(),
                getMetadata().orElse(null));
    }

    @Override
    public ConnectionCreated setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(connection, getRevision(), getTimestamp().orElse(null), dittoHeaders,
                getMetadata().orElse(null));
    }

    @Override
    protected void appendPayloadAndBuild(final JsonObjectBuilder jsonObjectBuilder,
            final JsonSchemaVersion schemaVersion, final Predicate<JsonField> thePredicate) {
        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ConnectivityEvent.JsonFields.CONNECTION,
                connection.toJson(schemaVersion, thePredicate), predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return (other instanceof ConnectionCreated);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConnectionCreated)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ConnectionCreated that = (ConnectionCreated) o;
        return Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connection);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + super.toString() + ", connection=" + connection + "]";
    }
}
