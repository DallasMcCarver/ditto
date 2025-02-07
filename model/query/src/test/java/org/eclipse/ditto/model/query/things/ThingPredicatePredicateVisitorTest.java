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
package org.eclipse.ditto.model.query.things;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

import org.assertj.core.api.AbstractBooleanAssert;
import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;
import org.junit.Test;

/**
 * Unit tests for {@link ThingPredicatePredicateVisitor}.
 */
public final class ThingPredicatePredicateVisitorTest {

    private final static ThingPredicatePredicateVisitor sut = ThingPredicatePredicateVisitor.getInstance();

    @Test
    public void matchingBooleanEq() {
        doTest(sut.visitEq(true), JsonValue.of(true))
                .isTrue();
    }

    @Test
    public void nonMatchingBooleanEq() {
        doTest(sut.visitEq(true), JsonValue.of(false))
                .isFalse();
    }

    @Test
    public void matchingStringEq() {
        doTest(sut.visitEq("yes"), JsonValue.of("yes"))
                .isTrue();
    }

    @Test
    public void nonMatchingStringEq() {
        doTest(sut.visitEq("no"), JsonValue.of("yes"))
                .isFalse();
    }

    @Test
    public void matchingStringNe() {
        doTest(sut.visitNe("yes"), JsonValue.of("no"))
                .isTrue();
    }

    @Test
    public void nonMatchingStringNe() {
        doTest(sut.visitNe("yes"), JsonValue.of("yes"))
                .isFalse();
    }

    @Test
    public void matchingStringLike() {
        // the sut already works on regex Pattern - the translation from "*" to ".*" is done in LikePredicateImpl
        doTest(sut.visitLike("this-is.*"), JsonValue.of("this-is-the-content"))
                .isTrue();
    }

    @Test
    public void nonMatchingStringLike() {
        // the sut already works on regex Pattern - the translation from "*" to ".*" is done in LikePredicateImpl
        doTest(sut.visitLike("this-is.*"), JsonValue.of("this-was-the-content"))
                .isFalse();
    }

    @Test
    public void matchingStringIn() {
        doTest(sut.visitIn(Collections.singletonList("this-is-some-content")), JsonValue.of("this-is-some-content"))
                .isTrue();
    }

    @Test
    public void nonMatchingStringIn() {
        doTest(sut.visitIn(Collections.singletonList("this-is-some-content")), JsonValue.of("this-is-the-content"))
                .isFalse();
    }

    @Test
    public void matchingIntegerEq() {
        doTest(sut.visitEq(7), JsonValue.of(7))
                .isTrue();
    }

    @Test
    public void matchingIntegerEqWithDoublePrecision() {
        doTest(sut.visitEq(7), JsonValue.of(7.0))
                .isTrue();
    }

    @Test
    public void nonMatchingIntegerEq() {
        doTest(sut.visitEq(7), JsonValue.of(8))
                .isFalse();
    }

    @Test
    public void matchingLongEq() {
        doTest(sut.visitEq(Long.MAX_VALUE), JsonValue.of(Long.MAX_VALUE))
                .isTrue();
    }

    @Test
    public void nonMatchingLongEq() {
        doTest(sut.visitEq(Long.MAX_VALUE), JsonValue.of(Long.MAX_VALUE - 1))
                .isFalse();
    }

    @Test
    public void matchingDoubleEq() {
        doTest(sut.visitEq(0.123), JsonValue.of(0.123))
                .isTrue();
    }

    @Test
    public void nonMatchingDoubleEq() {
        doTest(sut.visitEq(0.123), JsonValue.of(0.1234))
                .isFalse();
    }

    @Test
    public void matchingBooleanGe() {
        doTest(sut.visitGe(true), JsonValue.of(true))
                .isTrue();
    }

    @Test
    public void nonMatchingBooleanGt() {
        doTest(sut.visitGt(true), JsonValue.of(true))
                .isFalse();
    }

    @Test
    public void matchingBooleanLe() {
        doTest(sut.visitLe(true), JsonValue.of(true))
                .isTrue();
    }

    @Test
    public void nonMatchingBooleanLt() {
        doTest(sut.visitLt(true), JsonValue.of(true))
                .isFalse();
    }

    @Test
    public void matchingIntegerGe() {
        doTest(sut.visitGe(6), JsonValue.of(7))
                .isTrue();
    }

    @Test
    public void nonMatchingIntegerGt() {
        doTest(sut.visitGt(6), JsonValue.of(7))
                .isTrue();
    }

    @Test
    public void matchingIntegerLe() {
        doTest(sut.visitLe(6), JsonValue.of(5))
                .isTrue();
    }

    @Test
    public void nonMatchingIntegerLt() {
        doTest(sut.visitLt(6), JsonValue.of(5))
                .isTrue();
    }

    @Test
    public void comparingJsonArrayNeverEvaluatesToTrue() {
        // but also does not throw an exception!
        doTest(sut.visitEq(Arrays.asList("one", "two")), JsonArray.of("one", "two"))
                .isFalse();
    }

    @Test
    public void comparingJsonObjectNeverEvaluatesToTrue() {
        // but also does not throw an exception!
        doTest(sut.visitEq(new Object()), JsonObject.newBuilder().set("one", "two").build())
                .isFalse();
    }

    private static AbstractBooleanAssert<?> doTest(final Function<String, Predicate<Thing>> functionToTest,
            final JsonValue actualValue) {

        final String attributeKey = "some-attr";
        final Thing thing = Thing.newBuilder()
                .setAttribute(JsonPointer.of(attributeKey), JsonValue.of(actualValue))
                .build();

        return assertThat(functionToTest.apply("attributes/" + attributeKey)
                .test(thing)
        );
    }

}
