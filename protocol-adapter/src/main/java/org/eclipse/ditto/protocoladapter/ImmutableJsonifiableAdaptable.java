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
package org.eclipse.ditto.protocoladapter;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;

/**
 * Immutable implementation of {@link JsonifiableAdaptable}.
 */
@Immutable
final class ImmutableJsonifiableAdaptable implements JsonifiableAdaptable {

    private final Adaptable delegateAdaptable;

    private ImmutableJsonifiableAdaptable(final Adaptable delegateAdaptable) {
        this.delegateAdaptable = delegateAdaptable;
    }

    /**
     * Wraps a given {@code Adaptable} as {@code PlainJsonAdaptable} in order to make a full describing JSON from it.
     *
     * @param delegateAdaptable the {@code Adaptable} to wrap.
     * @return the PlainJsonAdaptable.
     * @throws NullPointerException if {@code delegateAdaptable} is {@code null}.
     */
    public static JsonifiableAdaptable of(final Adaptable delegateAdaptable) {
        requireNonNull(delegateAdaptable, "delegate adaptable");

        return new ImmutableJsonifiableAdaptable(delegateAdaptable);
    }

    /**
     * Returns a new ImmutablePlainJsonAdaptable from the specified {@code jsonObject}.
     *
     * @param jsonObject the JSON object.
     * @return the ImmutablePlainJsonAdaptable.
     * @throws org.eclipse.ditto.json.JsonMissingFieldException if {@code jsonObject} is missing required JSON fields.
     */
    public static ImmutableJsonifiableAdaptable fromJson(final JsonObject jsonObject) {
        final DittoHeaders headers = jsonObject.getValue(JsonFields.HEADERS)
                .map(ProtocolFactory::newHeaders)
                .orElse(DittoHeaders.empty());

        final TopicPath topicPath;

        try {
            topicPath = jsonObject.getValue(JsonFields.TOPIC)
                    .map(ProtocolFactory::newTopicPath)
                    .orElseGet(ProtocolFactory::emptyTopicPath);
        } catch (final DittoRuntimeException e) {
            throw e.setDittoHeaders(headers);
        }

        return new ImmutableJsonifiableAdaptable(ImmutableAdaptable.of(topicPath,
                ProtocolFactory.newPayload(jsonObject), headers));
    }

    @Override
    public TopicPath getTopicPath() {
        return delegateAdaptable.getTopicPath();
    }

    @Override
    public Payload getPayload() {
        return delegateAdaptable.getPayload();
    }

    @Override
    public boolean containsHeaderForKey(final CharSequence key) {
        return delegateAdaptable.containsHeaderForKey(key);
    }

    @Override
    public JsonObject toJson() {
        return toJson(getDittoHeaders());
    }

    @Override
    public JsonObject toJson(final DittoHeaders specificHeaders) {
        final JsonObjectBuilder jsonObjectBuilder = JsonFactory.newObjectBuilder();
        if (!getTopicPath().equals(ProtocolFactory.emptyTopicPath())) {
            jsonObjectBuilder.set(JsonFields.TOPIC, getTopicPath().getPath());
        }
        return jsonObjectBuilder
                .set(JsonFields.HEADERS, specificHeaders.toJson())
                .setAll(getPayload().toJson())
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
        final ImmutableJsonifiableAdaptable that = (ImmutableJsonifiableAdaptable) o;
        return Objects.equals(delegateAdaptable, that.delegateAdaptable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegateAdaptable);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "delegateAdaptable=" + delegateAdaptable + "]";
    }

    @Override
    public DittoHeaders getDittoHeaders() {
        return delegateAdaptable.getDittoHeaders();
    }

    @Override
    public JsonifiableAdaptable setDittoHeaders(@Nonnull final DittoHeaders dittoHeaders) {
        return new ImmutableJsonifiableAdaptable(delegateAdaptable.setDittoHeaders(dittoHeaders));
    }
}
