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
package org.eclipse.ditto.signals.commands.connectivity.modify;

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
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;
import org.eclipse.ditto.signals.commands.connectivity.ConnectivityCommandResponse;

/**
 * Response to a {@link TestConnection} command.
 */
@Immutable
@JsonParsableCommandResponse(type = TestConnectionResponse.TYPE)
public final class TestConnectionResponse extends AbstractCommandResponse<TestConnectionResponse>
        implements ConnectivityModifyCommandResponse<TestConnectionResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + TestConnection.NAME;

    static final JsonFieldDefinition<String> JSON_TEST_RESULT =
            JsonFactory.newStringFieldDefinition("testResult", FieldType.REGULAR,
                    JsonSchemaVersion.V_2);

    private final ConnectionId connectionId;
    private final String testResult;

    private TestConnectionResponse(final HttpStatus httpStatusCode,
            final ConnectionId connectionId,
            final String testResult,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatusCode, dittoHeaders);
        this.connectionId = connectionId;
        this.testResult = testResult;
    }

    /**
     * Returns a new instance of {@code TestConnectionResponse}.
     *
     * @param connectionId the connectionId of the tested connection.
     * @param restResult the test result string containing information about the connection test.
     * @param dittoHeaders the headers of the request.
     * @return a new TestConnectionResponse.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static TestConnectionResponse success(final ConnectionId connectionId, final String restResult,
            final DittoHeaders dittoHeaders) {

        checkNotNull(connectionId, "ConnectionId");
        checkNotNull(restResult, "TestResult");
        return new TestConnectionResponse(HttpStatus.OK, connectionId, restResult, dittoHeaders);
    }

    /**
     * Returns a new instance of {@code TestConnectionResponse} for an already created connection.
     *
     * @param connectionId the connectionId of the tested connection.
     * @param dittoHeaders the headers of the request.
     * @return a new TestConnectionResponse.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static TestConnectionResponse alreadyCreated(final ConnectionId connectionId, final DittoHeaders dittoHeaders) {
        checkNotNull(connectionId, "ConnectionId");
        return new TestConnectionResponse(HttpStatus.CONFLICT, connectionId,
                "Connection was already created - no test possible", dittoHeaders);
    }

    /**
     * Creates a new {@code TestConnectionResponse} from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the response.
     * @return the response.
     * @throws NullPointerException if {@code jsonString} is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static TestConnectionResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a new {@code TestConnectionResponse} from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the response.
     * @return the response.
     * @throws NullPointerException if {@code jsonObject} is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static TestConnectionResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<TestConnectionResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final String readConnectionId =
                            jsonObject.getValueOrThrow(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID);
                    final ConnectionId connectionId = ConnectionId.of(readConnectionId);
                    final String readConnectionResult = jsonObject.getValueOrThrow(JSON_TEST_RESULT);

                    return new TestConnectionResponse(httpStatus, connectionId, readConnectionResult, dittoHeaders);
                });
    }

    /**
     * Returns the result String of the tested {@code Connection}.
     *
     * @return the Connection.
     */
    public String getTestResult() {
        return testResult;
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(ConnectivityCommandResponse.JsonFields.JSON_CONNECTION_ID, String.valueOf(connectionId),
                predicate);
        jsonObjectBuilder.set(JSON_TEST_RESULT, testResult, predicate);
    }

    @Override
    public ConnectionId getEntityId() {
        return connectionId;
    }

    @Override
    public TestConnectionResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return success(connectionId, testResult, dittoHeaders);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof TestConnectionResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestConnectionResponse)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final TestConnectionResponse that = (TestConnectionResponse) o;
        return Objects.equals(connectionId, that.connectionId) && Objects.equals(testResult, that.testResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connectionId, testResult);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                super.toString() +
                ", connectionId=" + connectionId +
                ", testResult=" + testResult +
                "]";
    }

}
