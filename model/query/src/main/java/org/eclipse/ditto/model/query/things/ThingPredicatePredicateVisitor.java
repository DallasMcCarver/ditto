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
package org.eclipse.ditto.model.query.things;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.query.criteria.visitors.PredicateVisitor;
import org.eclipse.ditto.model.things.Thing;

/**
 * A Java {@link Predicate} based PredicateVisitor for evaluating whether {@link Thing}s match a given filter.
 */
public final class ThingPredicatePredicateVisitor implements PredicateVisitor<Function<String, Predicate<Thing>>> {

    private static ThingPredicatePredicateVisitor instance;

    private ThingPredicatePredicateVisitor() {
        // only internally instantiable
    }

    /**
     * Gets the singleton instance of this {@code ThingPredicatePredicateVisitor}.
     *
     * @return the singleton instance.
     */
    public static ThingPredicatePredicateVisitor getInstance() {
        if (null == instance) {
            instance = new ThingPredicatePredicateVisitor();
        }
        return instance;
    }

    /**
     * Creates a Java {@link Predicate} from a Ditto query
     * {@link org.eclipse.ditto.model.query.criteria.Predicate Predicate} and its field name.
     *
     * @param predicate The Ditto Predicate to generate the Predicate from.
     * @param fieldName Name of the field where the predicate is applied to.
     * @return The created Predicate.
     */
    public static Predicate<Thing> apply(final org.eclipse.ditto.model.query.criteria.Predicate predicate,
            final String fieldName) {
        return predicate.accept(getInstance()).apply(fieldName);
    }

    @Override
    public Function<String, Predicate<Thing>> visitEq(final Object value) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .map(Comparable.class::cast)
                        .filter(obj -> compare((Comparable<?>) value, obj) == 0)
                        .isPresent();
    }

    @Override
    public Function<String, Predicate<Thing>> visitNe(final Object value) {
        return fieldName ->
                thing -> !getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .map(Comparable.class::cast)
                        .filter(obj -> compare((Comparable<?>) value, obj) == 0)
                        .isPresent();
    }

    @Override
    public Function<String, Predicate<Thing>> visitGe(final Object value) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .filter(obj -> obj instanceof Comparable && value instanceof Comparable)
                        .map(Comparable.class::cast)
                        .filter(obj -> compare((Comparable<?>) value, obj) >= 0)
                        .isPresent();
    }

    @Override
    public Function<String, Predicate<Thing>> visitGt(final Object value) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .filter(obj -> obj instanceof Comparable && value instanceof Comparable)
                        .map(Comparable.class::cast)
                        .filter(obj -> compare((Comparable<?>) value, obj) > 0)
                        .isPresent();
    }

    @Override
    public Function<String, Predicate<Thing>> visitLe(final Object value) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .filter(obj -> obj instanceof Comparable && value instanceof Comparable)
                        .map(Comparable.class::cast)
                        .filter(obj -> compare((Comparable<?>) value, obj) <= 0)
                        .isPresent();
    }

    @Override
    public Function<String, Predicate<Thing>> visitLt(final Object value) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .filter(obj -> obj instanceof Comparable && value instanceof Comparable)
                        .map(Comparable.class::cast)
                        .filter(obj -> compare((Comparable<?>) value, obj) < 0)
                        .isPresent();
    }

    @SuppressWarnings({"rawtypes", "java:S3740"})
    private static int compare(final Comparable value, final Comparable obj) {
        final Comparable comparableObj = asNumber(obj);
        final Comparable comparableValue = asNumber(value);
        // best effort try to convert both values to a BigDecimal in order to compare them:
        if (comparableValue instanceof String && comparableObj instanceof BigDecimal) {
            try {
                return comparableObj.compareTo(new BigDecimal((String) comparableValue));
            } catch (final NumberFormatException e) {
                // continue trying
            }
        } else if (comparableValue instanceof BigDecimal && comparableObj instanceof String) {
            try {
                return new BigDecimal((String) comparableObj).compareTo((BigDecimal) comparableValue);
            } catch (final NumberFormatException e) {
                // continue trying
            }
        }

        if (comparableValue.getClass().equals(comparableObj.getClass())) {
            // only compare same classes:
            return comparableObj.compareTo(comparableValue);
        } else {
            // as a fallback, for different types, compare by their string representation:
            final String comparableObjString = comparableObj.toString();
            final String comparableValueString = comparableValue.toString();
            return comparableObjString.compareTo(comparableValueString);
        }
    }

    @SuppressWarnings({"rawtypes", "java:S3740"})
    private static Comparable asNumber(final Comparable<?> comparable) {
        return comparable instanceof Number ? new BigDecimal(comparable.toString()) : comparable;
    }

    @Override
    public Function<String, Predicate<Thing>> visitIn(final List<?> values) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .flatMap(ThingPredicatePredicateVisitor::mapJsonValueToJava)
                        .map(Comparable.class::cast)
                        .filter(obj -> values.stream().anyMatch(v -> compare((Comparable<?>) v, obj) == 0))
                        .isPresent();
    }

    @Override
    public Function<String, Predicate<Thing>> visitLike(final String value) {
        return fieldName ->
                thing -> getThingField(fieldName, thing)
                        .filter(JsonValue::isString)
                        .map(JsonValue::asString)
                        .filter(str -> Pattern.compile(value).matcher(str).matches())
                        .isPresent();
    }

    private static Optional<JsonValue> getThingField(final CharSequence fieldName, final Thing thing) {
        return thing.toJson(p -> true).getValue(fieldName);
    }

    private static Optional<Object> mapJsonValueToJava(final JsonValue jsonValue) {
        final Optional<Object> result;

        if (jsonValue.isString()) {
            result = Optional.of(jsonValue.asString());
        } else if (jsonValue.isBoolean()) {
            result = Optional.of(jsonValue.asBoolean());
        } else if (jsonValue.isNull()) {
            result = Optional.empty();
        } else if (jsonValue.isNumber()) {
            if (jsonValue.isInt()) {
                result = Optional.of(jsonValue.asInt());
            } else if (jsonValue.isLong()) {
                result = Optional.of(jsonValue.asLong());
            } else {
                result = Optional.of(jsonValue.asDouble());
            }
        } else if (jsonValue.isArray()) {
            result = Optional.empty(); // filtering arrays is not supported
        } else if (jsonValue.isObject()) {
            result = Optional.empty(); // filtering objects is not supported
        } else {
            result = Optional.empty();
        }

        return result;
    }

}
