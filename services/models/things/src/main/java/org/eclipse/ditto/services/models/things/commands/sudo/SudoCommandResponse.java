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

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.base.WithEntity;

/**
 * Aggregates all Thing SudoCommand Responses.
 *
 * @param <T> the type of the implementing class.
 */
public interface SudoCommandResponse<T extends SudoCommandResponse<T>> extends CommandResponse<T>, WithEntity<T>{

    /**
     * Type Prefix of Sudo commands.
     */
    String TYPE_PREFIX = "things.sudo." + TYPE_QUALIFIER + ":";

    @Override
    default JsonPointer getResourcePath() {
        // return empty resource path for SudoCommandResponses as this path is currently not needed for
        // SudoCommandResponses:
        return JsonPointer.empty();
    }

    @Override
    default String getResourceType() {
        return SudoCommand.RESOURCE_TYPE;
    }

    @Override
    T setEntity(JsonValue entity);

    @Override
    T setDittoHeaders(DittoHeaders dittoHeaders);

    /**
     * An enumeration of the known {@link org.eclipse.ditto.json.JsonField}s of a Sudo Thing command response.
     */
    class JsonFields extends CommandResponse.JsonFields {

        /**
         * JSON field containing the Thing ID.
         */
        public static final JsonFieldDefinition<String> JSON_THING_ID =
                JsonFactory.newStringFieldDefinition("payload/thingId", FieldType.REGULAR,
                        JsonSchemaVersion.V_2);

        private JsonFields() {
            throw new AssertionError();
        }

    }

}
