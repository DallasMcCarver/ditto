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
package org.eclipse.ditto.services.utils.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.io.SerializationException;
import io.jsonwebtoken.io.Serializer;
import io.jsonwebtoken.lang.Collections;
import io.jsonwebtoken.lang.DateFormats;
import io.jsonwebtoken.lang.Objects;

/**
 * JJWT library Serializer implementation which translates Java Objects (e.g. Maps) to JSON strings.
 */
@Immutable
public final class JjwtSerializer implements Serializer<Map<String, ?>> {

    private static Serializer<Map<String, ?>> instance;

    /**
     * @return the instance of {@link JjwtSerializer}.
     */
    public static Serializer<Map<String, ?>> getInstance() {
        if (instance == null) {
            instance = new JjwtSerializer();
        }
        return instance;
    }

    @Override
    public byte[] serialize(final Map<String, ?> t) {

        try {
            return toJson(t).toString().getBytes(StandardCharsets.UTF_8);
        } catch (final SerializationException se) {
            throw se;
        } catch (final Exception e) {
            throw new SerializationException("Unable to serialize object of type " +
                    Optional.ofNullable(t).map(obj -> obj.getClass().getName()).orElse("<null>") +
                    " to JSON: " + e.getMessage(), e);
        }
    }

    private static JsonValue toJson(final Object input) {

        if (input == null) {
            return JsonFactory.nullLiteral();
        } else if (input instanceof Boolean) {
            return JsonFactory.newValue((boolean) input);
        } else if (input instanceof Byte || input instanceof Short || input instanceof Integer) {
            return JsonFactory.newValue((int) input);
        } else if (input instanceof Long) {
            return JsonFactory.newValue((long) input);
        } else if (input instanceof Float) {
            return JsonFactory.newValue((float) input);
        } else if (input instanceof Double) {
            return JsonFactory.newValue((double) input);
        } else if (input instanceof Character || input instanceof String || input instanceof Enum) {
            return JsonFactory.newValue(input.toString());
        } else if (input instanceof Calendar) {
            return JsonFactory.newValue(DateFormats.formatIso8601(((Calendar) input).getTime()));
        } else if (input instanceof Date) {
            return JsonFactory.newValue(DateFormats.formatIso8601((Date) input));
        } else if (input instanceof byte[]) {
            return JsonFactory.newValue(Encoders.BASE64.encode((byte[]) input));
        } else if (input instanceof char[]) {
            return JsonFactory.newValue(new String((char[]) input));
        } else if (input instanceof Map) {
            return toJsonObject((Map<?, ?>) input);
        } else if (input instanceof Collection) {
            return toJsonArray((Collection<?>) input);
        } else if (Objects.isArray(input)) {
            return toJsonArray(Collections.arrayToList(input));
        }

        throw new SerializationException("Unable to serialize object of type " + input.getClass().getName() +
                " to JSON using known heuristics.");
    }

    private static JsonObject toJsonObject(final Map<?, ?> map) {

        return map.entrySet().stream()
                .map(entry -> JsonField.newInstance(String.valueOf(entry.getKey()), toJson(entry.getValue())))
                .collect(JsonCollectors.fieldsToObject());
    }

    private static JsonArray toJsonArray(final Collection<?> c) {

        return c.stream()
                .map(JjwtSerializer::toJson)
                .collect(JsonCollectors.valuesToArray());
    }

}
