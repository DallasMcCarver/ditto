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
package org.eclipse.ditto.signals.commands.connectivity.query;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.JsonParsableCommand;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.WithConnectionId;
import org.eclipse.ditto.signals.base.SignalWithEntityId;
import org.eclipse.ditto.signals.commands.base.AbstractCommand;
import org.eclipse.ditto.signals.commands.base.CommandJsonDeserializer;
import org.eclipse.ditto.signals.commands.connectivity.ConnectivityCommand;

/**
 * Command which retrieves a {@link Connection}.
 */
@Immutable
@JsonParsableCommand(typePrefix = ConnectivityCommand.TYPE_PREFIX, name = RetrieveConnection.NAME)
public final class RetrieveConnection extends AbstractCommand<RetrieveConnection>
        implements ConnectivityQueryCommand<RetrieveConnection>, WithConnectionId, SignalWithEntityId<RetrieveConnection> {

    /**
     * Name of this command.
     */
    public static final String NAME = "retrieveConnection";

    /**
     * Type of this command.
     */
    public static final String TYPE = TYPE_PREFIX + NAME;

    private final ConnectionId connectionId;

    private RetrieveConnection(final ConnectionId connectionId, final DittoHeaders dittoHeaders) {
        super(TYPE, dittoHeaders);
        this.connectionId = connectionId;
    }

    /**
     * Returns a new instance of {@code RetrieveConnection}.
     *
     * @param connectionId the identifier of the connection to be retrieved.
     * @param dittoHeaders the headers of the request.
     * @return a new RetrieveConnection command.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static RetrieveConnection of(final ConnectionId connectionId, final DittoHeaders dittoHeaders) {
        checkNotNull(connectionId, "Connection ID");
        return new RetrieveConnection(connectionId, dittoHeaders);
    }

    /**
     * Creates a new {@code RetrieveConnection} from a JSON string.
     *
     * @param jsonString the JSON string of which the command is to be retrieved.
     * @param dittoHeaders the headers of the command.
     * @return the command.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static RetrieveConnection fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code RetrieveConnection} from a JSON object.
     *
     * @param jsonObject the JSON object of which the command is to be created.
     * @param dittoHeaders the headers of the command.
     * @return the command.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static RetrieveConnection fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandJsonDeserializer<RetrieveConnection>(TYPE, jsonObject).deserialize(() -> {
            final String readConnectionId = jsonObject.getValueOrThrow(ConnectivityCommand.JsonFields.JSON_CONNECTION_ID);
            final ConnectionId connectionId = ConnectionId.of(readConnectionId);

            return of(connectionId, dittoHeaders);
        });
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ConnectivityCommand.JsonFields.JSON_CONNECTION_ID, String.valueOf(connectionId),
                predicate);
    }

    @Override
    public ConnectionId getEntityId() {
        return connectionId;
    }

    @Override
    public Category getCategory() {
        return Category.QUERY;
    }

    @Override
    public RetrieveConnection setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(connectionId, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof RetrieveConnection;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final RetrieveConnection that = (RetrieveConnection) o;
        return Objects.equals(connectionId, that.connectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connectionId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", connectionId=" + connectionId +
                "]";
    }

}
