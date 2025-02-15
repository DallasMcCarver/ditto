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
package org.eclipse.ditto.services.models.connectivity.placeholders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collections;

import org.eclipse.ditto.services.models.placeholders.ExpressionResolver;
import org.eclipse.ditto.services.models.placeholders.PlaceholderFactory;
import org.eclipse.ditto.services.models.placeholders.PlaceholderFilter;
import org.eclipse.ditto.services.models.placeholders.PlaceholderResolver;
import org.eclipse.ditto.services.models.placeholders.UnresolvedPlaceholderException;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.junit.Test;

public final class TopicPlaceholderTest {


    @Test
    public void testExpressionResolver() {
        final String thingName = "foobar199";
        final String knownTopic = "org.eclipse.ditto/" + thingName + "/things/twin/commands/modify";
        final TopicPath topic = ProtocolFactory.newTopicPath(knownTopic);

        final PlaceholderResolver<TopicPath> topicPathResolver = PlaceholderFactory.newPlaceholderResolver(
                ConnectivityPlaceholders.newTopicPathPlaceholder(), topic);

        final ExpressionResolver underTest =
                PlaceholderFactory.newExpressionResolver(Collections.singletonList(topicPathResolver));

        assertThat(underTest.resolve("{{ topic:full }}"))
                .contains(knownTopic);
        assertThat(underTest.resolve("{{ topic:entityName }}"))
                .contains(thingName);

        // verify different whitespace
        assertThat(underTest.resolve("{{topic:entityName }}"))
                .contains(thingName);
        assertThat(underTest.resolve("{{topic:entityName}}"))
                .contains(thingName);
        assertThat(underTest.resolve("{{        topic:entityName}}"))
                .contains(thingName);
    }

    @Test
    public void testPlaceholderResolver() {
        final String fullPath = "org.eclipse.ditto/foo23/things/twin/commands/modify";
        final TopicPath topic = ProtocolFactory.newTopicPath(fullPath);

        final PlaceholderResolver<TopicPath> underTest = PlaceholderFactory.newPlaceholderResolver(
                ConnectivityPlaceholders.newTopicPathPlaceholder(), topic);

        assertThat(underTest.resolve("full"))
                .contains(fullPath);
        assertThat(underTest.resolve("namespace"))
                .contains("org.eclipse.ditto");
        assertThat(underTest.resolve("entityName"))
                .contains("foo23");
        assertThat(underTest.resolve("group"))
                .contains("things");
        assertThat(underTest.resolve("channel"))
                .contains("twin");
        assertThat(underTest.resolve("criterion"))
                .contains("commands");
        assertThat(underTest.resolve("action"))
                .contains("modify");
    }

    @Test
    public void testPlaceholderFilter() {
        final String knownNamespace = "org.eclipse.ditto.test";
        final String knownId = "myThing";
        final String knownSubject = "mySubject";
        final String knownSubject2 = "$set.configuration/steps";

        final TopicPath knownTopicPath = TopicPath.newBuilder(ThingId.of(knownNamespace, knownId))
                .twin().things().commands().modify().build();
        final TopicPath knownTopicPathSubject1 = TopicPath.newBuilder(ThingId.of(knownNamespace, knownId))
                .live().things().messages().subject(knownSubject).build();
        final TopicPath knownTopicPathSubject2 = TopicPath.newBuilder(ThingId.of(knownNamespace, knownId))
                .live().things().messages().subject(knownSubject2).build();

        final TopicPathPlaceholder topicPlaceholder = ConnectivityPlaceholders.newTopicPathPlaceholder();


        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> topicPlaceholder.resolve(knownTopicPath, null));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> topicPlaceholder.resolve(knownTopicPath, ""));
        assertThatExceptionOfType(UnresolvedPlaceholderException.class).isThrownBy(
                () -> PlaceholderFilter.apply("{{ topic:unknown }}", knownTopicPath, topicPlaceholder));
        assertThatExceptionOfType(UnresolvedPlaceholderException.class).isThrownBy(
                () -> PlaceholderFilter.apply("{{ {{  topic:name  }} }}", knownTopicPath, topicPlaceholder));
        assertThat(PlaceholderFilter.apply("eclipse:ditto", knownTopicPath, topicPlaceholder)).isEqualTo(
                "eclipse:ditto");
        assertThat(PlaceholderFilter.apply("prefix:{{ topic:channel }}:{{ topic:group }}:suffix", knownTopicPath,
                topicPlaceholder)).isEqualTo("prefix:twin:things:suffix");

        assertThat(PlaceholderFilter.apply("{{topic:subject}}", knownTopicPathSubject1,
                topicPlaceholder)).isEqualTo(knownSubject);
        assertThat(PlaceholderFilter.apply("{{  topic:action-subject}}", knownTopicPathSubject2,
                topicPlaceholder)).isEqualTo(knownSubject2);
    }
}
