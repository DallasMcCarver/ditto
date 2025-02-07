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
package org.eclipse.ditto.signals.commands.base;

import java.util.function.Predicate;

import javax.annotation.concurrent.Immutable;

import org.atteo.classindex.IndexSubclasses;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.ResponseType;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.signals.base.Signal;

/**
 * Aggregates all possible responses relating to a given {@link Command}.
 *
 * @param <T> the type of the implementing class.
 */
@IndexSubclasses
public interface CommandResponse<T extends CommandResponse<T>> extends Signal<T>, WithHttpStatus {

    /**
     * Type qualifier of command responses.
     */
    String TYPE_QUALIFIER = "responses";

    @Override
    T setDittoHeaders(DittoHeaders dittoHeaders);

    @Override
    default JsonSchemaVersion getImplementedSchemaVersion() {
        return getDittoHeaders().getSchemaVersion().orElse(getLatestSchemaVersion());
    }

    /**
     * Indicates whether this response is of a type contained in
     * {@link org.eclipse.ditto.model.base.headers.DittoHeaderDefinition#EXPECTED_RESPONSE_TYPES} header.
     *
     * @return true if this response is expected, false if not.
     * @since 1.2.0
     */
    default boolean isOfExpectedResponseType() {
        return getDittoHeaders().getExpectedResponseTypes().contains(getResponseType());
    }

    /**
     * @return the type of this response.
     * @since 1.2.0
     */
    default ResponseType getResponseType() {
        return ResponseType.RESPONSE;
    }

    /**
     * Returns all non hidden marked fields of this command response.
     *
     * @return a JSON object representation of this command response including only regular, non-hidden marked fields.
     */
    @Override
    default JsonObject toJson() {
        return toJson(FieldType.notHidden());
    }

    @Override
    JsonObject toJson(JsonSchemaVersion schemaVersion, Predicate<JsonField> predicate);

    /**
     * This class contains common definitions for all fields of a {@code CommandResponse}'s JSON representation.
     * Implementation of {@code CommandResponse} may add additional fields by extending this class.
     */
    @Immutable
    abstract class JsonFields {

        /**
         * JSON field containing the response type as String.
         */
        public static final JsonFieldDefinition<String> TYPE = JsonFactory.newStringFieldDefinition("type",
                FieldType.REGULAR, JsonSchemaVersion.V_2);

        /**
         * JSON field containing the message's status code as int.
         */
        public static final JsonFieldDefinition<Integer> STATUS = JsonFactory.newIntFieldDefinition("status",
                FieldType.REGULAR, JsonSchemaVersion.V_2);

        /**
         * JSON field containing the message's payload as {@link JsonValue}.
         */
        public static final JsonFieldDefinition<JsonValue> PAYLOAD = JsonFactory.newJsonValueFieldDefinition("payload",
                FieldType.REGULAR, JsonSchemaVersion.V_2);

        /**
         * Constructs a new {@code JsonFields} object.
         */
        protected JsonFields() {
            super();
        }

    }

}
